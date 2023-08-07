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
import com.google.inject.{Inject, Singleton}
import connectors.SoftDrinksIndustryLevyConnector
import errors.{ReturnsStillPending, VariationsErrors}
import models.backend.Site
import models.updateRegisteredDetails.UpdateContactDetails
import models.{RetrievedSubscription, SelectChange, UserAnswers, Warehouse}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelectChangeOrchestrator @Inject()(sessionService: SessionService,
                                         softDrinksIndustryLevyConnector: SoftDrinksIndustryLevyConnector){

  def hasReturnsToCorrect(subscription: RetrievedSubscription)
                         (implicit hc: HeaderCarrier, ex: ExecutionContext): VariationResult[Boolean] = {
    softDrinksIndustryLevyConnector.returnsVariable(subscription.utr, subscription.sdilRef).map(_.nonEmpty)
  }

  def createUserAnswersAndSaveToDatabase(value: SelectChange, subscription: RetrievedSubscription)
                                    (implicit hc: HeaderCarrier,
                                     ec: ExecutionContext): VariationResult[Unit] = {
    for {
      userAnswers <- EitherT.right[VariationsErrors](generateDefaultUserAnswers(value, subscription))
      _ <- EitherT(sessionService.set(userAnswers))
      _ <- hasNoReturnsPendingIfRequired(value, subscription)
    } yield (): Unit
  }

  private def hasNoReturnsPendingIfRequired(value: SelectChange, subscription: RetrievedSubscription)
                                             (implicit ec: ExecutionContext,
                                              hc: HeaderCarrier): VariationResult[Unit] = EitherT{
    if(value == SelectChange.CancelRegistration) {
      softDrinksIndustryLevyConnector.returnsPending(subscription.utr, subscription.sdilRef).value.map{
        case Right(pendingReturns) if pendingReturns.isEmpty => Right((): Unit)
        case Right(_) => Left(ReturnsStillPending)
        case Left(err) => Left(err)
      }
    } else {
      Future.successful(Right((): Unit))
    }

  }

  private def generateDefaultUserAnswers(value: SelectChange, subscription: RetrievedSubscription): Future[UserAnswers] = {
    value match {
      case SelectChange.UpdateRegisteredDetails => setupUserAnswersForUpdateRegisteredDetails(subscription)
      case _ => Future.successful(setupDefaultUserAnswers(subscription, value))
    }
  }


  private def setupDefaultUserAnswers(subscription: RetrievedSubscription, value: SelectChange): UserAnswers = {
    UserAnswers(id = subscription.sdilRef,
      journeyType = value,
      contactAddress = subscription.address,
      packagingSiteList = getNoneClosedPackagingSites(subscription.productionSites),
      warehouseList = getNoneClosedWarehouses(subscription.warehouseSites),
      lastUpdated = timeNow)
  }

  private def setupUserAnswersForUpdateRegisteredDetails(subscription: RetrievedSubscription): Future[UserAnswers] = {
    val contactDetails = UpdateContactDetails.fromContact(subscription.contact)
    Future.fromTry(UserAnswers(id = subscription.sdilRef,
      journeyType = SelectChange.UpdateRegisteredDetails,
      contactAddress = subscription.address,
      packagingSiteList = getNoneClosedPackagingSites(subscription.productionSites),
      warehouseList = getNoneClosedWarehouses(subscription.warehouseSites),
      lastUpdated = timeNow)
      .set(UpdateContactDetailsPage, contactDetails)
    )

  }

  private def getNoneClosedPackagingSites(productionSites: List[Site]): Map[String, Site] = {
    val productionSitesNotClosed = productionSites.filter(site => site.closureDate.forall(_.isAfter(LocalDate.now)))
    productionSitesNotClosed.zipWithIndex
      .map { case (site, index) => (index.toString, site) }
      .toMap[String, Site]
  }

  private def getNoneClosedWarehouses(warehouses: List[Site]): Map[String, Warehouse] = {
    val productionSitesNotClosed = warehouses.filter(site => site.closureDate.forall(_.isAfter(LocalDate.now)))
    productionSitesNotClosed.zipWithIndex
      .map { case (site, index) => (index.toString, Warehouse.fromSite(site)) }
      .toMap[String, Warehouse]
  }

  def timeNow: Instant = Instant.now()

}