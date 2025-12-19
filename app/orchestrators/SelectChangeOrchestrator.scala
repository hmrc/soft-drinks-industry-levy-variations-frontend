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
import cats.implicits._
import com.google.inject.{ Inject, Singleton }
import connectors.SoftDrinksIndustryLevyConnector
import errors.{ ReturnsStillPending, VariationsErrors }
import models.backend.{ RetrievedSubscription, Site }
import models.updateRegisteredDetails.ContactDetails
import models.{ SelectChange, UserAnswers }
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{ Instant, LocalDate }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SelectChangeOrchestrator @Inject() (
  sessionService: SessionService,
  softDrinksIndustryLevyConnector: SoftDrinksIndustryLevyConnector
) {

  def hasReturnsToCorrect(
    subscription: RetrievedSubscription
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): VariationResult[Boolean] =
    softDrinksIndustryLevyConnector.returnsVariable(subscription.utr).map(_.nonEmpty)

  def createUserAnswersAndSaveToDatabase(value: SelectChange, subscription: RetrievedSubscription)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): VariationResult[Unit] =
    for {
      userAnswers <- EitherT.right[VariationsErrors](generateDefaultUserAnswers(value, subscription))
      _           <- EitherT(sessionService.set(userAnswers))
      _           <- hasNoReturnsPendingIfRequired(value, subscription)
    } yield (): Unit

  def createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(
    subscription: RetrievedSubscription
  )(implicit ec: ExecutionContext): VariationResult[UserAnswers] =
    for {
      userAnswers <-
        EitherT.right[VariationsErrors](generateDefaultUserAnswers(SelectChange.CorrectReturn, subscription))
      _ <- EitherT(sessionService.set(userAnswers))
    } yield userAnswers

  private def hasNoReturnsPendingIfRequired(value: SelectChange, subscription: RetrievedSubscription)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): VariationResult[Unit] = EitherT {
    val requiredToHaveNoPendingReturns =
      value == SelectChange.CancelRegistration && !subscription.activity.voluntaryRegistration
    if (requiredToHaveNoPendingReturns) {
      softDrinksIndustryLevyConnector.returnsPending(subscription.utr).value.map {
        case Right(pendingReturns) if pendingReturns.nonEmpty => Left(ReturnsStillPending)
        case Right(_)                                         => Right((): Unit)
        case Left(err)                                        => Left(err)
      }
    } else {
      Future.successful(Right((): Unit))
    }
  }

  private def generateDefaultUserAnswers(
    value: SelectChange,
    subscription: RetrievedSubscription
  ): Future[UserAnswers] =
    value match {
      case SelectChange.UpdateRegisteredDetails => setupUserAnswersForUpdateRegisteredDetails(subscription)
      case SelectChange.CorrectReturn           => setupDefaultUserAnswersForCorrectReturn(subscription)
      case _                                    => Future.successful(setupDefaultUserAnswers(subscription, value))
    }

  private def setupDefaultUserAnswers(subscription: RetrievedSubscription, value: SelectChange): UserAnswers =
    UserAnswers(
      id = subscription.sdilRef,
      journeyType = value,
      contactAddress = subscription.address,
      packagingSiteList = getNoneClosedSites(subscription.productionSites),
      warehouseList = getNoneClosedSites(subscription.warehouseSites),
      lastUpdated = timeNow
    )

  private def setupDefaultUserAnswersForCorrectReturn(subscription: RetrievedSubscription): Future[UserAnswers] =
    Future.successful(
      UserAnswers(
        id = subscription.sdilRef,
        journeyType = SelectChange.CorrectReturn,
        contactAddress = subscription.address,
        lastUpdated = timeNow
      )
    )

  private def setupUserAnswersForUpdateRegisteredDetails(subscription: RetrievedSubscription): Future[UserAnswers] = {
    val contactDetails = ContactDetails.fromContact(subscription.contact)
    Future.fromTry(
      UserAnswers(
        id = subscription.sdilRef,
        journeyType = SelectChange.UpdateRegisteredDetails,
        contactAddress = subscription.address,
        packagingSiteList = getNoneClosedSites(subscription.productionSites),
        warehouseList = getNoneClosedSites(subscription.warehouseSites),
        lastUpdated = timeNow
      )
        .set(UpdateContactDetailsPage, contactDetails)
    )

  }

  private def getNoneClosedSites(sites: List[Site]): Map[String, Site] = {
    val sitesNotClosed = sites.filter(site => site.closureDate.forall(_.isAfter(LocalDate.now)))
    sitesNotClosed.zipWithIndex
      .map { case (site, index) => (index.toString, site) }
      .toMap[String, Site]
  }

  def timeNow: Instant = Instant.now()

}
