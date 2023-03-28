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
import com.typesafe.config.ConfigFactory
import models.retrieved.RetrievedSubscription
import models.{FinancialLineItem, ReturnCharge, ReturnPeriod, SdilReturn, SmallProducer}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import repositories.SDILSessionCache
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

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

      when(mockSDILSessionCache.fetchEntry[RetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(Some(largePackerSubscription)))

      //when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(largePackerSubscription)
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

    "balance should return a big decimal successfully" in {
      val withAssessment = true
      when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(0))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber,withAssessment)

      whenReady(
        res
      ) {
        response =>
          response mustBe 0
      }
    }

    "balance history should return a big decimal successfully" in {
      val withAssessment = true

      val date: LocalDate = LocalDate.of(2022,10,10)
      val bigDecimal: BigDecimal = 1000
      val returnCharge: FinancialLineItem = ReturnCharge(ReturnPeriod(date), bigDecimal)

      val financialLineItemList = List(returnCharge)
      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialLineItemList))
      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber,withAssessment)

      whenReady(
        res
      ) {
        response =>
          response mustBe financialLineItemList
      }

      val utr: String = "1234567891"
      val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(List(returnPeriod)))
      Await.result(softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr), 4.seconds) mustBe Some(returnPeriod)

    }

  }
}
