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
import errors.{FailedToAddDataToUserAnswers, MissingRequiredAnswers, NoSdilReturnForPeriod, NoVariableReturns}
import models.backend.RetrievedSubscription
import models.correctReturn.CorrectReturnUserAnswersData
import models.{Amounts, ReturnPeriod, SdilReturn, UserAnswers}
import service.VariationResult
import services.{ReturnService, SessionService}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrectReturnOrchestrator @Inject()(returnService: ReturnService,
                                          connector: SoftDrinksIndustryLevyConnector,
                                          sessionService: SessionService){

  def submitReturn(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = EitherT {

    (userAnswers.correctReturnPeriod, userAnswers.getCorrectReturnOriginalSDILReturnData, userAnswers.getCorrectReturnData) match {
      case (Some(returnPeriod), Some(originalReturn), Some(correctReturnData)) =>
        submitReturnAndVariationAndUpdateSession(subscription, returnPeriod, originalReturn, userAnswers, correctReturnData).value
      case _ => Future.successful(Left(MissingRequiredAnswers))
    }
  }

  def submitReturnAndVariationAndUpdateSession(subscription: RetrievedSubscription,
                               returnPeriod: ReturnPeriod,
                               originalReturn: SdilReturn,
                               userAnswers: UserAnswers,
                               correctReturnData: CorrectReturnUserAnswersData)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    val revisedReturn = SdilReturn.generateFromUserAnswers(userAnswers, Some(instantNow))
    for {
      _ <- returnService.submitSdilReturnsVary(subscription, userAnswers, originalReturn, returnPeriod, revisedReturn)
      variation <- returnService.submitReturnVariation(subscription, revisedReturn, userAnswers, correctReturnData)
      _ <- EitherT(sessionService.set(userAnswers.copy(submittedOn = Some(instantNow))))
    } yield variation
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

  def separateReturnPeriodsByYear(returnPeriods: List[ReturnPeriod]): List[List[ReturnPeriod]] = {
    returnPeriods.distinct.groupBy(_.year).values.toList
      .map(_.sortBy(_.start).reverse)
      .sortBy(_.head.year).reverse
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

  def instantNow: Instant = Instant.now()

  def calculateAmounts(sdilRef: String,
                       userAnswers: UserAnswers,
                       returnPeriod: ReturnPeriod)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Amounts] = {
    returnService.calculateAmounts(sdilRef, userAnswers, returnPeriod)
  }
}
