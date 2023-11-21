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
import errors.{FailedToAddDataToUserAnswers, NoSdilReturnForPeriod, NoVariableReturns}
import models.backend.{RetrievedSubscription, Site, UkAddress}
import models.correctReturn.CorrectReturnUserAnswersData
import models.enums.SiteTypes.{PRODUCTION_SITE, WAREHOUSE}
import models.requests.DataRequest
import models.submission.VariationsSites.getHighestRefNumber
import models.submission.{ClosedSite, ReturnVariationData, VariationsPersonalDetails, VariationsSite, VariationsSites, VariationsSubmission}
import models.updateRegisteredDetails.ContactDetails
import models.{ReturnPeriod, SdilReturn, UserAnswers}
import pages.correctReturn.{CorrectionReasonPage, RepaymentMethodPage}
import play.api.mvc.AnyContent
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrectReturnOrchestrator @Inject()(connector: SoftDrinksIndustryLevyConnector,
                                          sessionService: SessionService){

  def submitVariation()
                     (implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val returnVariationData = for {
      originalReturn <- request.userAnswers.getCorrectReturnOriginalSDILReturnData
      returnPeriod <- request.userAnswers.correctReturnPeriod
      revisedReturn <- request.userAnswers.getCorrectReturnData
    } yield getReturnsVariationToBeSubmitted(
      subscription = request.subscription,
      userAnswers = request.userAnswers,
      originalReturn = originalReturn,
      returnPeriod = returnPeriod,
      revisedReturn = SdilReturn(
        ownBrand = revisedReturn.howManyOperatePackagingSiteOwnBrands.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        packLarge = revisedReturn.howManyPackagedAsContractPacker.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        packSmall = request.userAnswers.smallProducerList,
        importLarge = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        importSmall = revisedReturn.howManyBroughtIntoUkFromSmallProducers.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        export = revisedReturn.howManyClaimCreditsForExports.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        wastage = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
        submittedOn = Some(Instant.now())
      )
    )
    println(s"What is being Submitted -> $returnVariationData")
    Future.successful(connector.submitReturnsVariation(request.subscription.sdilRef, returnVariationData.get))
  }

  private def getReturnsVariationToBeSubmitted(subscription: RetrievedSubscription,
                                               userAnswers: UserAnswers,
                                               originalReturn: SdilReturn,
                                               returnPeriod: ReturnPeriod,
                                               revisedReturn: SdilReturn): ReturnVariationData = {
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
