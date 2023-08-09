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
import models.correctReturn.CorrectReturnUserAnswersData
import models.{RetrievedSubscription, ReturnPeriod, SdilReturn, UserAnswers}
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrectReturnOrchestrator @Inject()(connector: SoftDrinksIndustryLevyConnector,
                                          sessionService: SessionService){

  def getReturnPeriods(retrievedSubscription: RetrievedSubscription)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[List[ReturnPeriod]] = EitherT {
    connector.returnsVariable(retrievedSubscription.utr).value.map{
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
      updatedUserAnswers <- generateUserAnswersWithSdilReturn(userAnswers, sdilReturn, selectedReturnPeriod)
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

  private def getSdilReturn(retrievedSubscription: RetrievedSubscription,
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

}
