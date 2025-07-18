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

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.backend.{FinancialLineItem, RetrievedSubscription}
import models.correctReturn.{CorrectReturnUserAnswersData, ReturnsVariation}
import models.submission.ReturnVariationData
import models.{Amounts, ReturnPeriod, SdilReturn, UserAnswers}
import pages.correctReturn.{BalanceRepaymentRequired, CorrectionReasonPage, RepaymentMethodPage}
import service.VariationResult
import uk.gov.hmrc.http.HeaderCarrier
import utilities.UserTypeCheck

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ReturnService @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector)(implicit config: FrontendAppConfig) {
  private def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal = l.headOption.fold(BigDecimal(0))(_._2)

  private def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
  }
  def getBalanceBroughtForward(sdilRef: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[BigDecimal] = {
    if (config.balanceAllEnabled) {
      sdilConnector.balanceHistory(sdilRef, withAssessment = false).map { financialItem =>
        extractTotal(listItemsWithTotal(financialItem))
      }
    } else {
      sdilConnector.balance(sdilRef, withAssessment = false)
    }
  }

  def calculateAmounts(sdilRef: String,
                       userAnswers: UserAnswers,
                       returnPeriod: ReturnPeriod,
                       originalReturn: SdilReturn)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Amounts] = {
    for {
      isSmallProducer <- sdilConnector.checkSmallProducerStatus(sdilRef, returnPeriod)
      balanceBroughtForward <- getBalanceBroughtForward(sdilRef)
    } yield getAmounts(userAnswers, originalReturn, balanceBroughtForward, isSmallProducer.getOrElse(false))(returnPeriod)
  }

  def submitSdilReturnsVary(subscription: RetrievedSubscription,
                             userAnswers: UserAnswers,
                             originalReturn: SdilReturn,
                             returnPeriod: ReturnPeriod,
                             revisedReturn: SdilReturn)
                           (implicit hc: HeaderCarrier): VariationResult[Unit] = {
    val repaymentMethod = userAnswers.get(BalanceRepaymentRequired) match {
      case Some(true) => userAnswers.get(RepaymentMethodPage).map(_.toString)
      case _ => None
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

  def submitReturnVariation(subscription: RetrievedSubscription,
                            sdilReturn: SdilReturn,
                            userAnswers: UserAnswers,
                            correctReturnData: CorrectReturnUserAnswersData,
                            returnPeriod: ReturnPeriod)
                           (implicit hc: HeaderCarrier): VariationResult[Unit] = {
    implicit val rp: ReturnPeriod = returnPeriod
    val isNewImporter = UserTypeCheck.isNewImporter(userAnswers, subscription)
    val isNewPacker = UserTypeCheck.isNewPacker(userAnswers, subscription)
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
      taxEstimation = sdilReturn.taxEstimation
    )

    sdilConnector.submitReturnVariation(subscription.sdilRef, returnVariation)
  }

  private def getAmounts(userAnswers: UserAnswers, originalReturn: SdilReturn, balanceBroughtForward: BigDecimal, isSmallProducer: Boolean)
                        (implicit returnPeriod: ReturnPeriod): Amounts = {
    val originalReturnTotal: BigDecimal = originalReturn.total
    val totalForQuarter = SdilReturn.generateFromUserAnswers(userAnswers).total
    val totalForQuarterLessForwardBalance = totalForQuarter - balanceBroughtForward
    val netAdjustedAmount: BigDecimal = (totalForQuarter - originalReturnTotal) - balanceBroughtForward
    Amounts(originalReturnTotal, totalForQuarter, balanceBroughtForward, totalForQuarterLessForwardBalance, netAdjustedAmount)
  }

}
