package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.backend.{FinancialLineItem, RetrievedSubscription}
import models.{ReturnPeriod, SdilReturn}
import play.api.libs.json.Json
import testSupport.SDILBackendTestData._

case class SdilBackendStub()
                          (implicit builder: PreconditionBuilder)
{
  def returns_pending (utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.toJson(returnPeriods).toString())))
    builder
  }

  def no_returns_pending (utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          ok(Json.arr().toString())))
    builder
  }

  def returns_pending_error(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/pending"))
        .willReturn(
          serverError()))
    builder
  }

  def returns_variable(utr: String, returnPeriods: List[ReturnPeriod] = returnPeriodList) = {
    stubFor(
      get(
        urlPathMatching(s"/returns/$utr/variable"))
        .willReturn(
          ok(Json.toJson(returnPeriods).toString())))
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

  def retrieveSubscription(identifier: String, refNum: String, subscription: RetrievedSubscription = aSubscription) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(subscription).toString())))
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

  def retrieveSubscriptionError(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathEqualTo(s"/subscription/$identifier/$refNum"))
        .willReturn(
          serverError()))
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

  def checkSmallProducerStatusNone(sdilRef: String, period: ReturnPeriod): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"))
        .willReturn(notFound()))
    builder
  }

  def checkSmallProducerStatusError(sdilRef: String, period: ReturnPeriod): PreconditionBuilder = {
    stubFor(
      get(
        urlPathMatching(s"/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"))
        .willReturn(serverError()))
    builder
  }

  def retrieveReturn(utr: String, period: ReturnPeriod, resp: Option[SdilReturn]) = {
    val uri = s"/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    val response = resp match {
      case Some(sdilReturn) => ok(Json.toJson(sdilReturn).toString())
      case None => notFound()
    }
    stubFor(
      get(
        urlPathEqualTo(uri))
        .willReturn(
          response))
    builder
  }

  def retrieveReturnError(utr: String, period: ReturnPeriod) = {
    val uri = s"/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    stubFor(
      get(
        urlPathEqualTo(uri))
        .willReturn(
          serverError()))
    builder
  }

  def balance(sdilRef: String, withAssessment: Boolean, balance: BigDecimal = BigDecimal(1000)) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          ok(Json.toJson(balance).toString())))
    builder
  }

  def balancefailure(sdilRef: String, withAssessment: Boolean) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/$withAssessment"))
        .willReturn(
          serverError()))
    builder
  }

  def balanceHistory(sdilRef: String, withAssessment: Boolean, finincialItems: List[FinancialLineItem]) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/$withAssessment"))
        .willReturn(
          ok(Json.toJson(finincialItems).toString())))
    builder
  }

  def balanceHistoryfailure(sdilRef: String, withAssessment: Boolean) = {
    stubFor(
      get(
        urlPathMatching(s"/balance/$sdilRef/history/all/$withAssessment"))
        .willReturn(
          serverError()))
    builder
  }
}

