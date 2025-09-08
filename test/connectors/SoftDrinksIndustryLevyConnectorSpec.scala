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

package connectors

import models.backend.{FinancialLineItem, OptRetrievedSubscription, RetrievedSubscription}
import models.correctReturn.ReturnsVariation
import models.submission.{Litreage, ReturnVariationData}
import models.{DataHelper, ReturnPeriod, VariationsSubmissionDataHelper}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import repositories.{CacheMap, SDILSessionCache}
import uk.gov.hmrc.http.HttpResponse
import utilities.GenericLogger

import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends HttpClientV2Helper with DataHelper with VariationsSubmissionDataHelper {

  val (host, localPort) = ("host", "123")

  val mockSDILSessionCache = mock[SDILSessionCache]
  val logger = application.injector.instanceOf[GenericLogger]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http =mockHttp, frontendAppConfig, mockSDILSessionCache, logger)

  val utr: String = "1234567891"

  "SoftDrinksIndustryLevyConnector" - {

      "when there is a subscription in cache" in {

        val identifierType: String = "sdil"
        val sdilNumber: String = "XKSDIL000000022"
        when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(Some(OptRetrievedSubscription(Some(aSubscription)))))
        val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual (Some(aSubscription))
        }
      }

      "when there is no subscription in cache" in {
      val identifierType: String = "sdil"
      val sdilNumber: String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(None))
      when(requestBuilderExecute[Option[RetrievedSubscription]]).thenReturn(Future.successful(Some(aSubscription)))
      when(mockSDILSessionCache.save[OptRetrievedSubscription](any(), any(),any())(any())).thenReturn(Future.successful(CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang2")))))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res.getOrElse(None)
      ) {
        response =>
          response mustEqual Some(aSubscription)
      }
    }

      "when there is no subscription in cache and no subscription in the database" in {
        val identifierType: String = "sdil"
        val sdilNumber: String = "XKSDIL000000022"
        when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(None))
        when(requestBuilderExecute[Option[RetrievedSubscription]]).thenReturn(Future.successful(None))
        when(mockSDILSessionCache.save[OptRetrievedSubscription](any,any,any)(any())).thenReturn(Future.successful(CacheMap("foo", Map("bar" -> Json.obj("wizz" -> "bang2")))))
        val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual None
        }
      }

      "return a small producer status successfully" in {
        val sdilNumber: String = "XKSDIL000000022"
        val period = ReturnPeriod(year = 2022, quarter = 3)
        when(requestBuilderExecute[Option[Boolean]]).thenReturn(Future.successful(Some(false)))
        val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual Some(false)
        }

      }

      "return none if no small producer status" in {
        val sdilNumber: String = "XKSDIL000000022"
        val period = ReturnPeriod(year = 2022, quarter = 3)
        when(requestBuilderExecute[Option[Boolean]]).thenReturn(Future.successful(None))
        val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual None
        }

      }

      "return balance successfully" in {
        when(requestBuilderExecute[BigDecimal]).thenReturn(Future.successful(BigDecimal(1000)))
        val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right(BigDecimal(1000))
        }
      }

      "return balance history successfully" in {
      when(requestBuilderExecute[List[FinancialLineItem]]).thenReturn(Future.successful(financialItemList))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(
        res.value
      ) {
        response =>
          response mustEqual Right(financialItemList)
      }
    }

      "POST submitSdilReturnsVary successfully when valid data is given" - {
      "for no change" in{

        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitSdilReturnsVary(
          sdilNumber = aSubscription.sdilRef,
          variation = ReturnVariationData(
            original = emptySdilReturn,
            revised = emptySdilReturn,
            period = returnPeriodsFor2022.head,
            orgName = aSubscription.orgName,
            address = aSubscription.address,
            reason = "N/A",
            repaymentMethod = Some("bankAccount")))

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }

      "for a single change" in{
        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitSdilReturnsVary(
          sdilNumber = aSubscription.sdilRef,
          variation = ReturnVariationData(
            original = emptySdilReturn,
            revised = emptySdilReturn.copy(ownBrand = Litreage(100L, 100L)),
            period = returnPeriodsFor2022.head,
            orgName = aSubscription.orgName,
            address = aSubscription.address,
            reason = "N/A",
            repaymentMethod = Some("bankAccount")))

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }

      "for multiple changes" in {
        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitSdilReturnsVary(
          sdilNumber = aSubscription.sdilRef,
          variation = ReturnVariationData(
            original = emptySdilReturn,
            revised = emptySdilReturn.copy(ownBrand = Litreage(100L, 100L), packLarge = Litreage(100L, 100L)),
            period = returnPeriodsFor2022.head,
            orgName = aSubscription.orgName,
            address = aSubscription.address,
            reason = "N/A",
            repaymentMethod = Some("bankAccount")))

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }
    }

      "POST submitReturnVariation successfully when valid data is given" in {
      when(requestBuilderExecute[HttpResponse])
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitReturnVariation(
          aSubscription.sdilRef,
          variation = ReturnsVariation(
            orgName = aSubscription.orgName,
            ppobAddress = aSubscription.address,
            importer = (false, Litreage(0,0)),
            packer = (false, Litreage(0,0)),
            warehouses = List.empty,
            packingSites = List.empty,
            phoneNumber = "0800323984",
            email = "test@email.com",
            taxEstimation = 0)
        )

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
    }

       "POST variation successfully when valid data is given" - {
      "for a user who has updated registered details" in {
        val variationsSubmission = testVariationSubmission(variationContact = Some(updatedVariationsContact),
          variationsPersonalDetails = Some(updatedPersonalDetails),
          newSites = List(NEW_VARITION_SITE),
          closeSites = List(CLOSED_SITE)
        )

        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitVariation(variationsSubmission, aSubscription.sdilRef)

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }

      "for a user who has updated activity" in {
        val variationsSubmission = testVariationSubmission(
          sdilActivity = Some(UPDATED_SDIL_ACTIVITY),
          newSites = List(NEW_VARITION_SITE),
          closeSites = List(CLOSED_SITE)
        )

        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitVariation(variationsSubmission, aSubscription.sdilRef)

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }


      "for a user who has deregistered" in {
        val variationsSubmission = testVariationSubmission(
          isDeregistered = true
        )

        when(requestBuilderExecute[HttpResponse])
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val res = softDrinksIndustryLevyConnector.submitVariation(variationsSubmission, aSubscription.sdilRef)

        whenReady(
          res.value
        ) {
          response =>
            response mustEqual Right((): Unit)
        }
      }
    }
  }

}
