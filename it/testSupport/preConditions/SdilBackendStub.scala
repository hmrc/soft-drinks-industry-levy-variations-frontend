package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.backend.{Site, UkAddress}
import models.{Contact, RetrievedActivity, RetrievedSubscription, ReturnPeriod}
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                          (implicit builder: PreconditionBuilder)
{
  val aSubscription = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
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
        None)
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val returnPeriodList: List[ReturnPeriod] = List(ReturnPeriod(2020, 0), ReturnPeriod(2020, 1), ReturnPeriod(2020, 2), ReturnPeriod(2020, 3),
    ReturnPeriod(2021, 0), ReturnPeriod(2021, 1), ReturnPeriod(2021, 2), ReturnPeriod(2021, 3),
    ReturnPeriod(2022, 0), ReturnPeriod(2022, 1), ReturnPeriod(2022, 2), ReturnPeriod(2022, 3))

  val returnPeriods: List[ReturnPeriod] = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val emptyReturnPeriods: List[ReturnPeriod] = List()

  def returns_pending (utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.toJson(returnPeriods).toString())))
    builder
  }

  def returns_pending_empty(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.toJson(emptyReturnPeriods).toString())))
    builder
  }

  def returns_pending_not_found(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.arr().toString())))
    builder
  }

  def returns_variable(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/variable"))
        .willReturn(
          ok(Json.toJson(returnPeriodList).toString())))
    builder
  }

  def no_returns_variable(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/variable"))
        .willReturn(
          ok(Json.arr().toString())))
    builder
  }

  def returns_variable_error(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/variable"))
        .willReturn(
          serverError()))
    builder
  }

  def retrieveSubscription(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aSubscription).toString())))
    builder
  }

  def retrieveSubscriptionNone(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          notFound()))
    builder
  }

  def retrieveSubscriptionToModify(identifier: String, refNum: String, retrievedSubscription: RetrievedSubscription): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(retrievedSubscription).toString())))
    builder
  }

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod, smallProducerStatus: Boolean): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"))
        .willReturn(ok(Json.toJson(smallProducerStatus).toString())))
    builder
  }
}

