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

import akka.http.scaladsl.model.DateTime.month
import base.SpecBase
import com.typesafe.config.ConfigFactory
import models.backend.{Contact, Site}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod, ReturnVariationData, ReturnsVariation, SdilReturn, SmallProducer, UkAddress, backend}
import org.joda.time.DateTimeFieldType.{dayOfMonth, year}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.http.Status.OK
import repositories.SDILSessionCache
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience  {

  val (host, port) = ("host", "123")

  val localConfig = Configuration(
    ConfigFactory.parseString(s"""
                                 | microservice.services.soft-drinks-industry-levy {
                                 |    host     = "$host"
                                 |    port     = $port
                                 |  }
                                 |""".stripMargin)
  )

  val mockHttp = mock[HttpClient]
  val mockSDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http = mockHttp, localConfig, mockSDILSessionCache)


  val sdilReturn = SdilReturn(
    (1L, 1L),
    (1L,1L),
    List(SmallProducer("test", "test", (15,14))),
    (1L, 1L),
    (1L, 1L),
    (1L, 1L),
    (1L, 1L)
    //submittedOn: Option[LocalDateTime] = None
  )

  val revisedSdilReturn = SdilReturn(
    (2L, 2L),
    (2L,2L),
    List(SmallProducer("test2", "test2", (16,15))),
    (2L, 2L),
    (2L, 2L),
    (2L, 2L),
    (2L, 2L)
    //submittedOn: Option[LocalDateTime] = None
  )

  val emptySdilReturn: SdilReturn = SdilReturn((0L,0L),(0L, 0L),List.empty,(100L, 100L),(0L,0L),(0L,0L),(0L,0L))

  val utr: String = "1234567891"
  val returnPeriodList: Seq[ReturnPeriod] = List(ReturnPeriod(year = 2022, quarter = 3),
    ReturnPeriod(year = 2021, quarter = 3), ReturnPeriod(year = 2020, quarter = 3))

  implicit val hc = HeaderCarrier()

  "SoftDrinksIndustryLevyConnector" - {

    "return a subscription from cache Successfully" in {

      val identifierType: String = "sdil"

      when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(aSubscription)
      }
    }

    "return a small producer status successfully" in {

      when(mockHttp.GET[Option[Boolean]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(Some(false)))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, returnPeriod)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(false)
      }
    }

    "return a oldest pending return period successfully" in {

      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(List(returnPeriod)))
      val res = softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(returnPeriod)
      }
    }

    "returns pending should return a list of return periods successfully" in {

      val returnPeriodList = List(ReturnPeriod(year = 2022, quarter = 3), ReturnPeriod(year = 2021, quarter = 3), ReturnPeriod(year = 2020, quarter = 3))
      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(returnPeriodList))
      val res = softDrinksIndustryLevyConnector.returns_pending(utr)

      whenReady (
      res
      ) {
      response =>
        response mustBe Some(returnPeriodList)
      }
    }

//    "returns variable should return a list of return periods successfully" in {
//
//      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(returnPeriodList))
//      val res = softDrinksIndustryLevyConnector.returns_variable(utr)
//
//      whenReady(
//        res
//      ) {
//        response =>
//          response mustBe Some(returnPeriodList)
//      }
//
//    }

    "returns vary should post return variation data successfully" in {

      val returnVariationData = ReturnVariationData(sdilReturn,
        revisedSdilReturn,
        ReturnPeriod(year = 2021, quarter = 3),
        "Highly Addictive Drinks Plc",
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        "reason",
        None)

      when(mockHttp.POST[ReturnVariationData, HttpResponse](any(), any(), any())(any(), any(), any(), any())).thenReturn(
        Future.successful(
          HttpResponse.apply(OK)
        )
      )
      val res = softDrinksIndustryLevyConnector.returns_vary(sdilNumber, returnVariationData)

      whenReady(
        res
      ) {
        response =>
          response mustBe HttpResponse(OK)
      }
    }

    "returns get should return a sdil return successfully" in {

      when(mockHttp.GET[Option[SdilReturn]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(emptySdilReturn)))
      val res = softDrinksIndustryLevyConnector.returns_get(utr,returnPeriod)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(emptySdilReturn)
      }
    }

    "returns variation should post a returns variation successfully" in {

      val returnsVariation = ReturnsVariation("Super Lemonade Plc",
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        (false, (0, 0)),
        (false, (0, 0)),
        List(),
        List(),
        "07942009503",
        "Adeline.Greene@gmail.com",
        0
      )

      when(mockHttp.GET[Option[SdilReturn]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(emptySdilReturn)))
      val res = softDrinksIndustryLevyConnector.returns_variation(returnsVariation, sdilNumber)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(emptySdilReturn)
      }
    }

    "balance should return a big decimal successfully" in {
      val withAssesment = true
      when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(0))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber,withAssesment)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(emptySdilReturn)
      }
    }

    "balance history should return a big decimal successfully" in {
      val withAssesment = true

      val date: LocalDate = LocalDate.of(2022,10,10)
      val bigDecimal: BigDecimal = 1000
      val returnCharge: FinancialLineItem = ReturnCharge(ReturnPeriod(date), bigDecimal)

      val financialLineItemList = List(returnCharge)
      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialLineItemList))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber,withAssesment)

      whenReady(
        res
      ) {
        response =>
          response mustBe Some(emptySdilReturn)
      }
    }

  }
}
