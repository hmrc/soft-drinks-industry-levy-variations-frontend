package controllers.correctReturn

import controllers.CorrectReturnBaseCYASummaryISpecHelper
import models.NormalMode
import models.SelectChange.CorrectReturn
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
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
        build
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
        build
          .commonPrecondition

        setUpForCorrectReturn(userAnswers)

        build.sdilBackend.balance(userAnswers.id, withAssessment = false)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
            page.getElementsByClass("govuk-summary-list").size() mustBe 9

            val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(0)

            page.getElementsByTag("h2").get(0).text() mustBe "Own brands packaged at your own site"
            validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, isCheckAnswers = true)

            val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)

            page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
            validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, isCheckAnswers = true)

            val contractPackedForSmallProducers = page.getElementsByClass("govuk-summary-list").get(2)

            page.getElementsByTag("h2").get(2).text() mustBe "Contract packed for registered small producers"
            validateContractPackedForSmallProducersWithLitresSummaryList(contractPackedForSmallProducers, smallProducerLitres, isCheckAnswers = true)

            val imports = page.getElementsByClass("govuk-summary-list").get(3)

            page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
            validateImportsWithLitresSummaryList(imports, importsLitres, isCheckYourAnswers = true)

            val importsSmallProducers = page.getElementsByClass("govuk-summary-list").get(4)

            page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK from small producers"
            validateImportsFromSmallProducersWithLitresSummaryList(importsSmallProducers, importsLitres, isCheckYourAnswers = true)

            val exported = page.getElementsByClass("govuk-summary-list").get(5)

            page.getElementsByTag("h2").get(5).text() mustBe "Exported"
            validateExportsWithLitresSummaryList(exported, importsLitres, isCheckAnswers = true)

            val lostOrDamaged = page.getElementsByClass("govuk-summary-list").get(6)

            page.getElementsByTag("h2").get(6).text() mustBe "Lost or destroyed"
            validateLostOrDamagedWithLitresSummaryList(lostOrDamaged, importsLitres, isCheckAnswers = true)

            val siteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(7)

            page.getElementsByTag("h2").get(7).text() mustBe "UK site details"
            validateSiteDetailsSummary(userAnswers, aSubscription, siteDetailsSummaryListItem, 1, 2)

            page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCYAController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Save and continue"
          }
        }
      }

      "and they have only populated the required pages and have no litres" - {
        "should render the check your answers page with expected summary items" in {
          val userAnswers = userAnswerWithAllNosWithOriginalSdilReturn
          build
            .commonPrecondition

          setUpForCorrectReturn(userAnswers, Some(populatedReturn))

          build.sdilBackend.balance(userAnswers.id, withAssessment = false, 500)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your update - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 8

              val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(0)
              page.getElementsByTag("h2").get(0).text() mustBe "Own brands packaged at your own site"
              validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, isCheckAnswers = true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)

              page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
              validateContractPackingWithNoLitresSummaryList(contractPacking, isCheckYourAnswers = true)

              val contractPackedForSmallProducers = page.getElementsByClass("govuk-summary-list").get(2)

              page.getElementsByTag("h2").get(2).text() mustBe "Contract packed for registered small producers"
              validateContractPackedForSmallProducersWithNoLitresSummaryList(contractPackedForSmallProducers, isCheckYourAnswers = true)

              val imports = page.getElementsByClass("govuk-summary-list").get(3)

              page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
              validateImportsWithNoLitresSummaryList(imports, isCheckYourAnswers = true)

              val importsFromSmallProducers = page.getElementsByClass("govuk-summary-list").get(4)

              page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK from small producers"
              validateImportsFromSmallProducersWithNoLitresSummaryList(importsFromSmallProducers, isCheckYourAnswers = true)

              val exports = page.getElementsByClass("govuk-summary-list").get(5)

              page.getElementsByTag("h2").get(5).text() mustBe "Exported"
              validateExportsWithNoLitresSummaryList(exports, isCheckYourAnswers = true)

              val lostOrDamaged = page.getElementsByClass("govuk-summary-list").get(6)

              page.getElementsByTag("h2").get(6).text() mustBe "Lost or destroyed"
              validateLostOrDamagedWithNoLitresSummaryList(lostOrDamaged, isCheckYourAnswers = true)

              page.getElementsByTag("h2").get(7).text() mustBe "Summary"
              page.getElementsByClass("govuk-summary-list__key").get(7).text() mustBe "Total this quarter"
              page.getElementsByClass("govuk-summary-list__value  total-for-quarter sdil-right-align--desktop").get(0).text() mustBe "£0.00"
              page.getElementsByClass("govuk-summary-list__key").get(8).text() mustBe "Balance brought forward"
              page.getElementsByClass("govuk-summary-list__value  balance-brought-forward sdil-right-align--desktop").get(0).text() mustBe "−£500.00"
              page.getElementsByClass("govuk-summary-list__key").get(9).text() mustBe "Total"
              page.getElementsByClass("govuk-summary-list__value  total sdil-right-align--desktop govuk-!-font-weight-bold").get(0).text() mustBe "−£500.00"
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
      val userAnswers = userAnswerWithAllNosWithOriginalSdilReturn
      build
        .commonPrecondition

      setUpForCorrectReturn(userAnswers, Some(populatedReturn))

      build.sdilBackend.balance(userAnswers.id, withAssessment = false)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectionReasonController.onPageLoad(NormalMode).url)
        }
      }
    }

    "when the balance has failed" - {
      "the user should be redirected to Correct Return CYA and the error should be logged" in {
        val correctReturnData = nilCorrectReturnUAData
          .copy(broughtIntoUK = true, howManyBroughtIntoUK = Some(operatePackagingSiteLitres))
        val userAnswers = userAnswerWithAllNosWithOriginalSdilReturn
        build
          .commonPrecondition

        setUpForCorrectReturn(userAnswers)

        build.sdilBackend.balance("", false)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

          whenReady(result) { res =>
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))
  }

}
