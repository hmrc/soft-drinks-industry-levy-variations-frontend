/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import cats.data.EitherT
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import errors.VariationsErrors
import models.backend.{ FinancialLineItem, RetrievedSubscription }
import models.correctReturn.{ CorrectReturnUserAnswersData, ReturnsVariation }
import models.submission.ReturnVariationData
import models.{ Amounts, LevyCalculation, ReturnPeriod, SdilReturn, UserAnswers }
import pages.correctReturn.{ BalanceRepaymentRequired, CorrectionReasonPage, RepaymentMethodPage }
import service.VariationResult
import uk.gov.hmrc.http.HeaderCarrier
import utilities.UserTypeCheck

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ReturnService @Inject() (sdilConnector: SoftDrinksIndustryLevyConnector, config: FrontendAppConfig) {
  private def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal =
    l.headOption.fold(BigDecimal(0))(_._2)

  private def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
    }
  def getBalanceBroughtForward(
    sdilRef: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[BigDecimal] =
    if (config.balanceAllEnabled) {
      sdilConnector.balanceHistory(sdilRef, withAssessment = false).map { financialItem =>
        extractTotal(listItemsWithTotal(financialItem))
      }
    } else {
      sdilConnector.balance(sdilRef, withAssessment = false)
    }

  def calculateAmounts(
    sdilRef: String,
    userAnswers: UserAnswers,
    returnPeriod: ReturnPeriod,
    originalReturn: SdilReturn
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Amounts] =
    for {
      _                     <- sdilConnector.checkSmallProducerStatus(sdilRef, returnPeriod)
      balanceBroughtForward <- getBalanceBroughtForward(sdilRef)
      amounts <- EitherT.right[VariationsErrors](
        getAmounts(
          sdilRef,
          userAnswers,
          originalReturn,
          balanceBroughtForward,
          returnPeriod
        )
      )
    } yield amounts

  def submitSdilReturnsVary(
    subscription: RetrievedSubscription,
    userAnswers: UserAnswers,
    originalReturn: SdilReturn,
    returnPeriod: ReturnPeriod,
    revisedReturn: SdilReturn
  )(implicit hc: HeaderCarrier): VariationResult[Unit] = {
    val repaymentMethod = userAnswers.get(BalanceRepaymentRequired) match {
      case Some(true) => userAnswers.get(RepaymentMethodPage).map(_.toString)
      case _          => None
    }
    val returnsVariation = ReturnVariationData(
      original = originalReturn,
      revised = revisedReturn,
      period = returnPeriod,
      orgName = subscription.orgName,
      address = subscription.address,
      reason = userAnswers.get(CorrectionReasonPage).getOrElse(""),
      repaymentMethod = repaymentMethod
    )

    sdilConnector.submitSdilReturnsVary(subscription.sdilRef, returnsVariation)
  }

  def submitReturnVariation(
    subscription: RetrievedSubscription,
    sdilReturn: SdilReturn,
    userAnswers: UserAnswers,
    correctReturnData: CorrectReturnUserAnswersData,
    returnPeriod: ReturnPeriod
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    val isNewImporter = UserTypeCheck.isNewImporter(userAnswers, subscription)
    val isNewPacker = UserTypeCheck.isNewPacker(userAnswers, subscription)
    EitherT.right[VariationsErrors](sdilReturn.taxEstimation(subscription.sdilRef, sdilConnector, returnPeriod)).flatMap {
      taxEstimation =>
        val returnVariation = ReturnsVariation(
          orgName = subscription.orgName,
          ppobAddress = subscription.address,
          importer = (isNewImporter, correctReturnData.totalImported.combineN(4)),
          packer = (isNewPacker, correctReturnData.totalPacked(userAnswers.smallProducerList).combineN(4)),
          warehouses = if (isNewImporter) {
            userAnswers.warehouseList.values.toList
          } else {
            List()
          },
          packingSites = if (isNewPacker) {
            userAnswers.packagingSiteList.values.toList
          } else {
            List()
          },
          phoneNumber = subscription.contact.phoneNumber,
          email = subscription.contact.email,
          taxEstimation = taxEstimation
        )

        sdilConnector.submitReturnVariation(subscription.sdilRef, returnVariation)
    }
  }

  def calculateLevyCalculations(sdilRef: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Map[(Long, Long), LevyCalculation]] = {
    val correctReturnData = userAnswers.getCorrectReturnData
    val litresPairs: Set[(Long, Long)] = correctReturnData
      .map { data =>
        val basePairs = Set(
          (data.ownBrandsLitreage.lower, data.ownBrandsLitreage.higher),
          (data.contractPackerLitreage.lower, data.contractPackerLitreage.higher),
          (data.broughtIntoUkLitreage.lower, data.broughtIntoUkLitreage.higher),
          (data.broughtIntoUkFromSmallProducerLitreage.lower, data.broughtIntoUkFromSmallProducerLitreage.higher),
          (data.exportsLitreage.lower, data.exportsLitreage.higher),
          (data.lostDamagedLitreage.lower, data.lostDamagedLitreage.higher)
        )
        val smallProducerList = userAnswers.smallProducerList
        if (smallProducerList.nonEmpty)
          basePairs + ((smallProducerList.map(_.litreage.lower).sum, smallProducerList.map(_.litreage.higher).sum))
        else
          basePairs
      }
      .getOrElse(Set.empty)

    val returnPeriod = userAnswers.correctReturnPeriod.getOrElse(defaultReturnPeriod)

    Future
      .sequence(
        litresPairs.toSeq.map { case (low, high) =>
          sdilConnector.calculateLevy(sdilRef, low, high, returnPeriod).map(calc => (low, high) -> calc)
        }
      )
      .map(_.toMap)
  }

  private val defaultReturnPeriod: ReturnPeriod = ReturnPeriod(2022, 3)

  private def getAmounts(
    sdilRef: String,
    userAnswers: UserAnswers,
    originalReturn: SdilReturn,
    balanceBroughtForward: BigDecimal,
    returnPeriod: ReturnPeriod
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Amounts] =
    for {
      originalReturnTotal <- originalReturn.total(sdilRef, sdilConnector, returnPeriod)
      totalForQuarter     <- SdilReturn.generateFromUserAnswers(userAnswers).total(sdilRef, sdilConnector, returnPeriod)
    } yield {
      val totalForQuarterLessForwardBalance = totalForQuarter - balanceBroughtForward
      val netAdjustedAmount = (totalForQuarter - originalReturnTotal) - balanceBroughtForward
      Amounts(
        originalReturnTotal,
        totalForQuarter,
        balanceBroughtForward,
        totalForQuarterLessForwardBalance,
        netAdjustedAmount
      )
    }

}
