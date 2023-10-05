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

import base.SpecBase
import models.{DataHelper, FinancialLineItem, Litreage, OptRetrievedSubscription, RetrievedSubscription, ReturnPeriod, SdilReturn, VariationsSubmission}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.JsValue
import repositories.{CacheMap, SDILSessionCache}
import uk.gov.hmrc.http.{HttpClient, HttpResponse}
import utilities.GenericLogger

import java.time.LocalDate
import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with DataHelper {

  val (host, localPort) = ("host", "123")

  val mockHttp = mock[HttpClient]
  val mockSDILSessionCache = mock[SDILSessionCache]
  val mockGenericLogger = mock[GenericLogger]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http =mockHttp, frontendAppConfig, mockSDILSessionCache, mockGenericLogger)

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

      "return a small producer status successfully" in {
        val sdilNumber: String = "XKSDIL000000022"
        val period = ReturnPeriod(year = 2022, quarter = 3)
        when(mockHttp.GET[Option[Boolean]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(false)))
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
        when(mockHttp.GET[Option[Boolean]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(None))
        val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual None
        }

      }

      "return balance successfully" in {
        when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(BigDecimal(1000)))
        val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

        whenReady(
          res.getOrElse(None)
        ) {
          response =>
            response mustEqual BigDecimal(1000)
        }
      }

    "return balance history successfully" in {

      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialItemList))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(
        res.getOrElse(None)
      ) {
        response =>
          response mustEqual financialItemList
      }
    }

    "post variation successfully" in {
      val retrievedActivityData = testRetrievedActivity()

      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )

      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        deregDate = Some(LocalDate.now()),
        packageOwn = Some(true),
        packageOwnVol= Some(Litreage(100, 100)),
        copackForOthers = true,
        copackForOthersVol = Some(Litreage(200, 200)),
        imports = true,
        importsVol = Some(Litreage(300, 300)),
      ))

      when(mockHttp.POST[JsValue, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val res = softDrinksIndustryLevyConnector.submitVariation(data, aSubscription.sdilRef)

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(OK)
      }
    }

  }

}
