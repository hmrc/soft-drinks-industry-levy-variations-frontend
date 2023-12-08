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
import models.backend.{RetrievedSubscription, Site}
import models.correctReturn.CorrectReturnUserAnswersData
import models.enums.SiteTypes.{PRODUCTION_SITE, WAREHOUSE}
import models.submission.{ReturnVariationData, VariationsContact, VariationsSite, VariationsSubmission}
import models.{ReturnPeriod, SdilReturn, UserAnswers}
import pages.correctReturn.{CorrectionReasonPage, RepaymentMethodPage}
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class CorrectReturnOrchestrator @Inject()(connector: SoftDrinksIndustryLevyConnector,
                                          sessionService: SessionService,
                                          genericLogger: GenericLogger){

  def submitActivityVariation(userAnswers: UserAnswers,
                              subscription: RetrievedSubscription)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    connector.submitVariation(constructActivityVariation(userAnswers, subscription),subscription.sdilRef)
  }

  def newSites(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[VariationsSite] = {
    val newProductionSites =
      userAnswers.packagingSiteList.values.toSeq.diff(subscription.productionSites.filter(_.closureDate.forall(_.isAfter(LocalDate.now))))
    val newWarehouses =
      userAnswers.warehouseList.values.toSeq.diff(subscription.warehouseSites.filter(_.closureDate.forall(_.isAfter(LocalDate.now))))

    variationsSites(subscription,newProductionSites, newWarehouses)
  }

  def variationsSites(subscription: RetrievedSubscription, productionSites: Seq[Site], warehouses: Seq[Site]): List[VariationsSite] = {
    val contact = VariationsContact(
      None,
      Some(subscription.contact.phoneNumber),
      Some(subscription.contact.email)
    )

    val highestNum = { subscription.productionSites ++ subscription.warehouseSites }.foldLeft(0) { (id, site) =>
      Math.max(
        id,
        site.ref
          .flatMap { x =>
            Try(x.toInt).toOption
          }
          .getOrElse(0))
    }

    val ps = productionSites.zipWithIndex map {
      case (site, id) =>
        VariationsSite(
          tradingName = site.tradingName.getOrElse(""),
          siteReference = site.ref.getOrElse({ highestNum + id + 1 }.toString),
          variationsContact = contact.copy(address = Some(site.address)),
          typeOfSite = PRODUCTION_SITE
        )
    }

    val w = warehouses.zipWithIndex map {
      case (warehouse, id) =>
        VariationsSite(
          tradingName = warehouse.tradingName.getOrElse(""),
          siteReference = warehouse.ref.getOrElse({ highestNum + id + 1 + productionSites.size }.toString),
          variationsContact = contact.copy(address = Some(warehouse.address)),
          typeOfSite = WAREHOUSE
        )
    }

    (ps ++ w).toList
  }

   def constructActivityVariation(userAnswers: UserAnswers, subscription: RetrievedSubscription): VariationsSubmission ={

      VariationsSubmission(
        displayOrgName = subscription.orgName,
        ppobAddress = subscription.address,
        newSites = newSites(userAnswers, subscription),
        amendSites = Nil,
        closeSites = Nil
      )

  }


  def submitReturnVariation(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Option[VariationResult[Unit]] = {
    constructReturnVariationData(userAnswers, subscription).map(connector.submitReturnsVariation(subscription.sdilRef, _))
  }

  def constructReturnVariationData(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                     : Option[ReturnVariationData] = {
    for {
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
          submittedOn = None
        )
      )
    }
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

  def separateReturnPeriodsByYear(returnPeriods: List[ReturnPeriod]): List[List[ReturnPeriod]] = {
//    TODO: NEED TO SORT WITHIN EACH YEAR AS WELL
    returnPeriods.distinct.groupBy(_.year).values.toList.sortBy(_.head.year).reverse
  }

  def submitUserAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext):Future[Boolean] = {
    sessionService.set(userAnswers.copy(submittedOn = Some(Instant.now))).map {
      case Right(_) => true
      case Left(_) => genericLogger.logger.error(s"Failed to set value in session repository while attempting set on submittedOn")
        false
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