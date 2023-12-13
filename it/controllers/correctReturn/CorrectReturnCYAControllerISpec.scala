package controllers.correctReturn

import controllers.CorrectReturnBaseCYASummaryISpecHelper
import models.NormalMode
import models.SelectChange.CorrectReturn
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.correctReturn.PackAtBusinessAddressPage
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.WsTestClient
import testSupport.SDILBackendTestData.aSubscription

class CorrectReturnCYAControllerISpec extends CorrectReturnBaseCYASummaryISpecHelper {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> false
  )

  val route = "/correct-return/check-your-answers"

  "GET " + routes.CorrectReturnCYAController.onPageLoad.url - {
    "when the userAnswers contains no data for return to correct" - {
      "should redirect to Select Return controller" in {
        given
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForSelectChange(CorrectReturn).copy(correctReturnPeriod = None))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          }
        }
      }
    }

    "when the user has populated all pages including litres" - {
      "should render the check your answers page with only the required details" in {
        val userAnswers = userAnswerWithLitresForAllPagesNilSdilReturn
          .copy(packagingSiteList = packagingSitesFromSubscription, warehouseList = warehousesFromSubscription)
          .set(PackAtBusinessAddressPage, true).success.value
        given
          .commonPrecondition

        setUpForCorrectReturn(userAnswers)

        given.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            page.getElementsByClass("govuk-summary-list").size() mustBe 9

            val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(0)

            page.getElementsByTag("h2").get(0).text() mustBe "Own brands packaged at your own site"
            validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, true)

            val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)

            page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
            validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, true)

            val contractPackedForSmallProducers = page.getElementsByClass("govuk-summary-list").get(2)

            page.getElementsByTag("h2").get(2).text() mustBe "Contract packed for registered small producers"
            validateContractPackedForSmallProducersWithLitresSummaryList(contractPackedForSmallProducers, smallProducerLitres, true)

            val imports = page.getElementsByClass("govuk-summary-list").get(3)

            page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
            validateImportsWithLitresSummaryList(imports, importsLitres, true)

            val importsSmallProducers = page.getElementsByClass("govuk-summary-list").get(4)

            page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK from small producers"
            validateImportsFromSmallProducersWithLitresSummaryList(importsSmallProducers, importsLitres, true)

            val exported = page.getElementsByClass("govuk-summary-list").get(5)

            page.getElementsByTag("h2").get(5).text() mustBe "Exported"
            validateExportsWithLitresSummaryList(exported, importsLitres, true)

            val lostOrDamaged = page.getElementsByClass("govuk-summary-list").get(6)

            page.getElementsByTag("h2").get(6).text() mustBe "Lost or destroyed"
            validateLostOrDamagedWithLitresSummaryList(lostOrDamaged, importsLitres, true)

            val siteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(7)

            page.getElementsByTag("h2").get(7).text() mustBe "UK site details"
            validateSiteDetailsSummary(userAnswers, aSubscription, siteDetailsSummaryListItem, 1, 2, true)

            page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCYAController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Save and continue"
          }
        }
      }

      "and they have only populated the required pages and have no litres" - {
        "should render the check your answers page with expected summary items" in {
          val userAnswers = userAnswerWithAllNosWithOriginalSdilReturn
          given
            .commonPrecondition

          setUpForCorrectReturn(userAnswers, Some(populatedReturn))

          given.sdilBackend.balance(userAnswers.id, false)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 8

              val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(0)
              page.getElementsByTag("h2").get(0).text() mustBe "Own brands packaged at your own site"
              validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)

              page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
              validateContractPackingWithNoLitresSummaryList(contractPacking, true)

              val contractPackedForSmallProducers = page.getElementsByClass("govuk-summary-list").get(2)

              page.getElementsByTag("h2").get(2).text() mustBe "Contract packed for registered small producers"
              validateContractPackedForSmallProducersWithNoLitresSummaryList(contractPackedForSmallProducers, true)

              val imports = page.getElementsByClass("govuk-summary-list").get(3)

              page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
              validateImportsWithNoLitresSummaryList(imports, true)

              val importsFromSmallProducers = page.getElementsByClass("govuk-summary-list").get(4)

              page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK from small producers"
              validateImportsFromSmallProducersWithNoLitresSummaryList(importsFromSmallProducers, true)

              val exports = page.getElementsByClass("govuk-summary-list").get(5)

              page.getElementsByTag("h2").get(5).text() mustBe "Exported"
              validateExportsWithNoLitresSummaryList(exports, true)

              val lostOrDamaged = page.getElementsByClass("govuk-summary-list").get(6)

              page.getElementsByTag("h2").get(6).text() mustBe "Lost or destroyed"
              validateLostOrDamagedWithNoLitresSummaryList(lostOrDamaged, true)

              page.getElementsByTag("h2").get(7).text() mustBe "Balance"
              page.getElementsByClass("govuk-summary-list__key").get(7).text() mustBe "Original return total"
              page.getElementsByClass("govuk-summary-list__value  original-return-total sdil-right-align--desktop").get(0).text() mustBe "£229.80"
              page.getElementsByClass("govuk-summary-list__key").get(8).text() mustBe "New return total"
              page.getElementsByClass("govuk-summary-list__value  new-return-total sdil-right-align--desktop").get(0).text() mustBe "£0.00"
              page.getElementsByClass("govuk-summary-list__key").get(9).text() mustBe "Account balance"
              page.getElementsByClass("govuk-summary-list__value  balance-brought-forward sdil-right-align--desktop").get(0).text() mustBe "−£1,000.00"
              page.getElementsByClass("govuk-summary-list__key").get(10).text() mustBe "Net adjusted amount"
              page.getElementsByClass("govuk-summary-list__value  total sdil-right-align--desktop govuk-!-font-weight-bold").get(0).text() mustBe "−£1,000.00"

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Save and continue"
            }
          }
        }
      }
    }
    testRequiredCorrectReturnDataMissing(baseUrl + route)
    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }

  "POST " + routes.CorrectReturnCYAController.onPageLoad.url - {
    "should redirect to Correction Reason controller" in {
      given
        .commonPrecondition

      setUpForCorrectReturn(emptyUserAnswersForSelectChange(CorrectReturn))

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectionReasonController.onPageLoad(NormalMode).url)
        }
      }
    }

    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))
  }

}
