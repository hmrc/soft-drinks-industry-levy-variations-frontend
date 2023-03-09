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

import com.typesafe.config.ConfigFactory
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import models.{Contact, FinancialLineItem, ReturnCharge, ReturnPeriod, ReturnVariationData, ReturnsVariation, SdilReturn, Site, SmallProducer, UkAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class SoftDrinksIndustryLevyConnectorSpec extends PlaySpec with MockitoSugar with ScalaFutures with IntegrationPatience  {

  val (host, port) = ("host", "123")

  val config = Configuration(
    ConfigFactory.parseString(s"""
                                 | microservice.services.soft-drinks-industry-levy {
                                 |    host     = "$host"
                                 |    port     = $port
                                 |  }
                                 |""".stripMargin)
  )
  val mockHttp = mock[HttpClient]


  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http =mockHttp, config)

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

  val aSubscription = RetrievedSubscription(
    "0000000022",
    "XKSDIL000000022",
    "Super Lemonade Plc",
    UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    RetrievedActivity(false, true, false, false, false),
    LocalDate.of(2018, 4, 19),
    List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    List(),
    Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    None
  )

  val emptySdilReturn = SdilReturn((0L,0L),(0L, 0L),List.empty,(100L, 100L),(0L,0L),(0L,0L),(0L,0L))

  val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
  val sdilNumber: String = "XKSDIL000000022"
  val utr: String = "1234567891"
  val returnPeriodList = List(ReturnPeriod(year = 2022, quarter = 3), ReturnPeriod(year = 2021, quarter = 3), ReturnPeriod(year = 2020, quarter = 3))

  implicit val hc = HeaderCarrier()

  "SoftDrinksIndustryLevyConnector" must {

    "return a subscription Successfully" in {

      val identifierType: String = "0000000022"

      when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
      Await.result(softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber,identifierType), 4.seconds) mustBe  Some(aSubscription)

    }

    "return a small producer status successfully" in {

      when(mockHttp.GET[Option[Boolean]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(Some(false)))
      Await.result(softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, returnPeriod), 4.seconds) mustBe Some(false)

    }

    "return a oldest pending return period successfully" in {

      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(List(returnPeriod)))
      Await.result(softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr), 4.seconds) mustBe Some(returnPeriod)

    }

    "returns pending should return a list of return periods successfully" in {

      val returnPeriodLiist = List(ReturnPeriod(year = 2022, quarter = 3), ReturnPeriod(year = 2021, quarter = 3), ReturnPeriod(year = 2020, quarter = 3))
      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(returnPeriodLiist))
      Await.result(softDrinksIndustryLevyConnector.returns_pending(utr), 4.seconds) mustBe Some(returnPeriodLiist)

    }

    "returns variable should return a list of return periods successfully" in {

      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(returnPeriodList))
      Await.result(softDrinksIndustryLevyConnector.returns_variable(utr), 4.seconds) mustBe Some(returnPeriodList)

    }

    "returns vary should post return variation data successfully" in {

      val returnVariationData = ReturnVariationData(  sdilReturn,
        revisedSdilReturn,
        ReturnPeriod(year = 2021, quarter = 3),
        "Highly Addictive Drinks Plc",
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        "reason",
        None)

      when(mockHttp.POST[ReturnVariationData, HttpResponse](any(),any(),any())(any(),any(),any(),any())).thenReturn(
        Future.successful(
          HttpResponse.apply(OK)
        )
      )
      Await.result(softDrinksIndustryLevyConnector.returns_vary(sdilNumber,returnVariationData), 4.seconds) mustBe HttpResponse(OK)
    }

    "returns get should return a sdil return successfully" in {

      when(mockHttp.GET[Option[SdilReturn]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(emptySdilReturn)))
      Await.result(softDrinksIndustryLevyConnector.returns_get(utr,returnPeriod), 4.seconds) mustBe Some(emptySdilReturn)
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
      Await.result(softDrinksIndustryLevyConnector.returns_variation(returnsVariation, sdilNumber), 4.seconds) mustBe Some(emptySdilReturn)
    }

    "balance should return a big decimal successfully" in {
      val withAssesment = true
      when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(0))
      Await.result(softDrinksIndustryLevyConnector.balance(sdilNumber,withAssesment), 4.seconds) mustBe Some(emptySdilReturn)
    }

    "balance history should return a big decimal successfully" in {
      val withAssesment = true

      val date: LocalDate = LocalDate.of(2022,10,10)
      val bigDecimal: BigDecimal = 1000
      val returnCharge: FinancialLineItem = ReturnCharge(ReturnPeriod(date), bigDecimal)

      val financialLineItemList = List(returnCharge)
      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialLineItemList))
      Await.result(softDrinksIndustryLevyConnector.balance(sdilNumber,withAssesment), 4.seconds) mustBe Some(emptySdilReturn)
    }

  }

}