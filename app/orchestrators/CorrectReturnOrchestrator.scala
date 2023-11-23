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

package orchestrators

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import connectors.SoftDrinksIndustryLevyConnector
import errors.{FailedToAddDataToUserAnswers, NoSdilReturnForPeriod, NoVariableReturns, UnexpectedResponseFromSDIL, VariationsErrors}
import models.backend.RetrievedSubscription
import models.correctReturn.CorrectReturnUserAnswersData
import models.submission.ReturnVariationData
import models.{ReturnPeriod, SdilReturn, UserAnswers}
import pages.correctReturn.{CorrectionReasonPage, RepaymentMethodPage}
import play.api.mvc.Results.Redirect
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrectReturnOrchestrator @Inject()(connector: SoftDrinksIndustryLevyConnector,
                                          sessionService: SessionService){

  def submitVariation(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    val optReturnVariation = for {
      originalReturn <- userAnswers.getCorrectReturnOriginalSDILReturnData
      returnPeriod <- userAnswers.correctReturnPeriod
      revisedReturn <- userAnswers.getCorrectReturnData
    } yield {
      getReturnsVariationToBeSubmitted(
        subscription = subscription,
        userAnswers = userAnswers,
        originalReturn = originalReturn,
        returnPeriod = returnPeriod,
        revisedReturn = SdilReturn(
          ownBrand = revisedReturn.howManyOperatePackagingSiteOwnBrands.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          packLarge = revisedReturn.howManyPackagedAsContractPacker.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          packSmall = userAnswers.smallProducerList,
          importLarge = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          importSmall = revisedReturn.howManyBroughtIntoUkFromSmallProducers.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          export = revisedReturn.howManyClaimCreditsForExports.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          wastage = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
          submittedOn = Some(Instant.now())
        )
      )
    }
    optReturnVariation.map(connector.submitReturnsVariation(subscription.sdilRef, _))
      .getOrElse(UnexpectedResponseFromSDIL)
  }

  private def getReturnsVariationToBeSubmitted(subscription: RetrievedSubscription,
                                               userAnswers: UserAnswers,
                                               originalReturn: SdilReturn,
                                               returnPeriod: ReturnPeriod,
                                               revisedReturn: SdilReturn): ReturnVariationData  = {
    ReturnVariationData(
      original = originalReturn,
      revised = revisedReturn,
      period = returnPeriod,
      orgName = subscription.orgName,
      address = subscription.address,
      reason = userAnswers.get(CorrectionReasonPage).get,
      repaymentMethod = Some(userAnswers.get(RepaymentMethodPage).toString)
    )
  }

  def getReturnPeriods(retrievedSubscription: RetrievedSubscription)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[List[ReturnPeriod]] = EitherT {
    connector.getVariableReturnsFromCache(retrievedSubscription.utr).value.map{
      case Right(returnPeriods) if returnPeriods.nonEmpty => Right(returnPeriods)
      case Right(_) => Left(NoVariableReturns)
      case Left(error) => Left(error)
    }
  }

  def setupUserAnswersForCorrectReturn(retrievedSubscription: RetrievedSubscription,
                                       userAnswers: UserAnswers,
                                       selectedReturnPeriod: ReturnPeriod)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {

    for {
      sdilReturn <- getSdilReturn(retrievedSubscription, selectedReturnPeriod)
      userAnswersWithOriginalSdilReturn <- generateUAsWithOriginalSdilReturnSaved(userAnswers, sdilReturn)
      updatedUserAnswers <- generateUserAnswersWithSdilReturn(userAnswersWithOriginalSdilReturn, sdilReturn, selectedReturnPeriod)
      _ <- EitherT(sessionService.set(updatedUserAnswers))
    } yield (): Unit

  }

  def separateReturnPeriodsByYear(returnPeriods: List[ReturnPeriod]): Map[Int, List[ReturnPeriod]] = {
    val orderReturnPeriods = returnPeriods.distinct.sortBy(_.start).reverse

    orderReturnPeriods.foldLeft(Map.empty[Int, List[ReturnPeriod]]) {
      (returnPeriodsForYears, returnPeriod) =>
        val year = returnPeriod.year
        val returnPeriodsForYear = returnPeriodsForYears.get(year)
          .fold(List(returnPeriod))(_ ++ List(returnPeriod))
        returnPeriodsForYears ++ Map(year -> returnPeriodsForYear)
    }
  }

  def getSdilReturn(retrievedSubscription: RetrievedSubscription,
                            selectedReturnPeriod: ReturnPeriod)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[SdilReturn] = EitherT {
    connector.getReturn(retrievedSubscription.utr, selectedReturnPeriod).value.map {
      case Right(Some(sdilReturn)) => Right(sdilReturn)
      case Right(_) => Left(NoSdilReturnForPeriod)
      case Left(error) => Left(error)
    }
  }

  private def generateUserAnswersWithSdilReturn(userAnswers: UserAnswers, sdilReturn: SdilReturn, selectedReturnPeriod: ReturnPeriod)
                                             (implicit ec: ExecutionContext): VariationResult[UserAnswers] = EitherT {
    val correctReturnUAData = CorrectReturnUserAnswersData.fromSdilReturn(sdilReturn)
    Future.fromTry(userAnswers
      .setForCorrectReturn(correctReturnUAData, sdilReturn.packSmall, selectedReturnPeriod)
    ).map(Right(_))
      .recover {
        case _ => Left(FailedToAddDataToUserAnswers)
      }
  }

  private def generateUAsWithOriginalSdilReturnSaved(userAnswers: UserAnswers, sdilReturn: SdilReturn)
                                               (implicit ec: ExecutionContext): VariationResult[UserAnswers] = EitherT {
    Future.fromTry(userAnswers
      .setOriginalSDILReturn(sdilReturn)
    ).map(Right(_))
      .recover {
        case _ => Left(FailedToAddDataToUserAnswers)
      }
  }
}
