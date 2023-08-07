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
import errors.UnexpectedResponseFromSDIL
import models.{FinancialLineItem, RetrievedSubscription, ReturnPeriod, SdilReturn}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import repositories.{CacheMap, SDILSessionCache}
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val (host, localPort) = ("host", "123")

  val mockHttp = mock[HttpClient]
  val mockSDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http =mockHttp, frontendAppConfig, mockSDILSessionCache)

  val utr: String = "1234567891"

  "SoftDrinksIndustryLevyConnector" - {

    "when there is a subscription in cache" in {

      val identifierType: String = "sdil"
      val sdilNumber: String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[RetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(Some(aSubscription)))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res
      ) {
        response =>
          response mustEqual(Some(aSubscription))
      }
    }

    "when there is no subscription in cache" in {
      val identifierType: String = "sdil"
      val sdilNumber: String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[RetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(None))
      when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
      when(mockSDILSessionCache.save[RetrievedSubscription](any,any,any)(any())).thenReturn(Future.successful(CacheMap("test", Map("SUBSCRIPTION" -> Json.toJson(aSubscription)))))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(aSubscription)
      }
    }

    "when there is no subscription in cache and no subscription in the database" in {
      val identifierType: String = "sdil"
      val sdilNumber: String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[RetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(None))
      when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(None))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res
      ) {
        response =>
          response mustEqual None
      }
    }

    "return a small producer status successfully" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(mockHttp.GET[Option[Boolean]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(false)))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

      whenReady(
        res
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
        res
      ) {
        response =>
          response mustEqual None
      }

    }

    "return a oldest pending return period successfully" in {

      val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
      when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(List(returnPeriod)))
      val res = softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr)

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(returnPeriod)
      }
    }

    "return balance successfully" in {
      when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(BigDecimal(1000)))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

      whenReady(
        res
      ) {
        response =>
          response mustEqual BigDecimal(1000)
      }
    }

    "return balance history successfully" in {

      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialItemList))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(
        res
      ) {
        response =>
          response mustEqual financialItemList
      }
    }

    "returnsPending" - {
      "when the cache contains returns periods" - {
        "should return the pending returns from cache" in {
          when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](any(), any())(any())).thenReturn(Future.successful(Some(returnPeriods)))


          val res = softDrinksIndustryLevyConnector.returnsPending(utr, sdilReference)

          whenReady(res.value) {
            response =>
              response mustEqual Right(returnPeriods)
          }
        }
      }
      "when the cache does not contain return periods" - {
        "must call the backend and " - {
          "return returns-pending when the call was successful and there are pending returns" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))

            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(returnPeriods))

            val res = softDrinksIndustryLevyConnector.returnsPending(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Right(returnPeriods)
            }
          }

          "return error when unsuccessful" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))
            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.failed(new Exception("error")))

            val res = softDrinksIndustryLevyConnector.returnsPending(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Left(UnexpectedResponseFromSDIL)
            }
          }

          "return an empty list when no returns pending" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))
            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(List.empty))

            val res = softDrinksIndustryLevyConnector.returnsPending(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Right(List.empty)
            }
          }
        }
      }
    }


    "returnsVariable" - {
      "when the cache contains returns periods" - {
        "should return the variable returns from cache" in {
          when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](any(), any())(any())).thenReturn(Future.successful(Some(returnPeriods)))


          val res = softDrinksIndustryLevyConnector.returnsVariable(utr, sdilReference)

          whenReady(res.value) {
            response =>
              response mustEqual Right(returnPeriods)
          }
        }
      }
      "when the cache does not contain return periods" - {
        "must call the backend and " - {
          "return returns-variable when the call was successful and there are variable returns" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))

            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(returnPeriods))

            val res = softDrinksIndustryLevyConnector.returnsVariable(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Right(returnPeriods)
            }
          }

          "return error when unsuccessful" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))
            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.failed(new Exception("error")))

            val res = softDrinksIndustryLevyConnector.returnsVariable(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Left(UnexpectedResponseFromSDIL)
            }
          }

          "return an empty list when no returns variable" in {

            when(mockSDILSessionCache.fetchEntry[List[ReturnPeriod]](
              any(), any())(any())).thenReturn(Future.successful(None))
            when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(List.empty))

            val res = softDrinksIndustryLevyConnector.returnsVariable(utr, sdilReference)

            whenReady(res.value) {
              response =>
                response mustEqual Right(List.empty)
            }
          }
        }
      }
    }

    "post return succesfully" in {
      val period = ReturnPeriod(year = 2022, quarter = 3)
      val sdilReturn: SdilReturn =  SdilReturn(
        ownBrand = (1L, 1L ),
        packLarge = (1L, 1L ),
        packSmall =  List(),
        importLarge =  (1L, 1L ),
        importSmall =  (1L, 1L ),
        export =  (1L, 1L ),
        wastage =  (1L, 1L ),
        submittedOn =  None
      )

      when(mockHttp.POST[JsValue, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val res = softDrinksIndustryLevyConnector.returns_update(utr,period,sdilReturn )

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(OK)
      }
    }

  }

}
