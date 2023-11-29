package controllers.correctReturn

import controllers.CorrectReturnBaseCYASummaryISpecHelper
import models.LitresInBands
import models.SelectChange.CorrectReturn
import models.correctReturn.RepaymentMethod
import models.correctReturn.RepaymentMethod.BankAccount
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import pages.correctReturn._
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.await
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import testSupport.SDILBackendTestData.aSubscription
import testSupport.helpers.checkReturnsVariationSubmission

class CorrectReturnCheckChangesCYAControllerISpec extends CorrectReturnBaseCYASummaryISpecHelper {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> false
  )

  val route = "/correct-return/check-changes"

  "GET " + routes.CorrectReturnCheckChangesCYAController.onPageLoad.url - {

    "when the userAnswers contains no data for correct return " - {
      "should redirect to select return controller" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForSelectChange(CorrectReturn))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          }
        }
      }
    }

    "when the user has changed all pages including litres" - {
      "should render the check changes page with all sections" in {
        val userAnswers = userAnswerWithLitresForAllPagesNilSdilReturn
          .set(CorrectionReasonPage, "I forgot something").success.value
          .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
        given
          .commonPrecondition

        setAnswers(userAnswers)

        given.sdilBackend.balance(userAnswers.id, false)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
            page.getElementsByClass("govuk-summary-list").size() mustBe 10

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
            validateSiteDetailsSummary(userAnswers, aSubscription, siteDetailsSummaryListItem, 0, 2, true)

            page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
          }
        }
      }

      "and they have changed all answers to no and have no litres" - {
        "should render the check changes page with all summary items" in {
          val userAnswers = userAnswerWithAllNosWithOriginalSdilReturn
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 9

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

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }
    }

    "when the user has changed answers on select pages should render only the changed section(s)" - {
      s"when the user has changed answers on $OperatePackagingSiteOwnBrandsPage" - {
        "should render the check changes page with only the own brands section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(OperatePackagingSiteOwnBrandsPage, HowManyOperatePackagingSiteOwnBrandsPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Own brands packaged at your own site"
              validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $PackagedAsContractPackerPage" - {
        "should render the check changes page with only the packaged as contract packer section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(PackagedAsContractPackerPage, HowManyPackagedAsContractPackerPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Contract packed at your own site"
              validateContractPackingWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $BroughtIntoUKPage" - {
        "should render the check changes page with only the Brought into the UK section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(BroughtIntoUKPage, HowManyBroughtIntoUKPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPreconditionChangeSubscription(diffSubscriptionWithWarehouses)

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK

              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Brought into the UK"
              validateImportsWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $BroughtIntoUkFromSmallProducersPage" - {
        "should render the check changes page with only the Brought into the UK from small producers section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(BroughtIntoUkFromSmallProducersPage, HowManyBroughtIntoUkFromSmallProducersPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPreconditionChangeSubscription(diffSubscriptionWithWarehouses)

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Brought into the UK from small producers"
              validateImportsFromSmallProducersWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $ClaimCreditsForExportsPage" - {
        "should render the check changes page with only the Exports section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(ClaimCreditsForExportsPage, HowManyClaimCreditsForExportsPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Exported"
              validateExportsWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $ClaimCreditsForLostDamagedPage" - {
        "should render the check changes page with only the Lost Destroyed section" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(ClaimCreditsForLostDamagedPage, HowManyCreditsForLostDamagedPage)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          given.sdilBackend.balance(userAnswers.id, false)
          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Lost or destroyed"
              validateLostOrDamagedWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $ExemptionsForSmallProducersPage" - {
        "should render the check changes page with only the exemptions from small producers section" in {
          val userAnswers = userAnswerWithExemptionSmallProducerPageUpdatedAndNilSdilReturn
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 3

              val contractPackedForSmallProducers = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Contract packed for registered small producers"
              validateContractPackedForSmallProducersWithLitresSummaryList(contractPackedForSmallProducers, smallProducerLitres, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has changed answers on $BroughtIntoUKPage, activity of Importer is false, and they have no warehouses " - {
        "should render the check changes page with the Brought into the UK and UK site sections" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(BroughtIntoUKPage, HowManyBroughtIntoUKPage)
            .copy(warehouseList = warehousesFromSubscription)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK

              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 4

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(0)

              page.getElementsByTag("h2").get(0).text() mustBe "Brought into the UK"
              validateImportsWithLitresSummaryList(contractPacking, LitresInBands(1000, 2000), true)

              val siteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(1)

              page.getElementsByTag("h2").get(1).text() mustBe "UK site details"
              validateSiteDetailsSummary(userAnswers, aSubscription, siteDetailsSummaryListItem, 0, numberOfWarehouses = 2, isCheckAnswers = true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CorrectReturnCheckChangesCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the user has made their changes" - {
        "should render the check changes page with balance" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(BroughtIntoUKPage, HowManyBroughtIntoUKPage)
            .copy(warehouseList = warehousesFromSubscription)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance(userAnswers.id, false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK

              val page = Jsoup.parse(res.body)
              page.title mustBe "Check your answers before sending your correction - Soft Drinks Industry Levy - GOV.UK"
              page.getElementsByClass("govuk-summary-list").size() mustBe 4

              page.getElementsByTag("h2").get(3).text() mustBe "Balance"
              page.getElementsByClass("govuk-summary-list__key").get(8).text() mustBe "Original return total"
              page.getElementsByClass("govuk-summary-list__value  original-return-total sdil-right-align--desktop").get(0).text() mustBe "£0.00"
              page.getElementsByClass("govuk-summary-list__key").get(9).text() mustBe "New return total"
              page.getElementsByClass("govuk-summary-list__value  new-return-total sdil-right-align--desktop").get(0).text() mustBe "£660.00"
              page.getElementsByClass("govuk-summary-list__key").get(10).text() mustBe "Account balance"
              page.getElementsByClass("govuk-summary-list__value  balance-brought-forward sdil-right-align--desktop").get(0).text() mustBe "−£1,000.00"
              page.getElementsByClass("govuk-summary-list__key").get(11).text() mustBe "Net adjusted amount"
              page.getElementsByClass("govuk-summary-list__value  total sdil-right-align--desktop govuk-!-font-weight-bold").get(0).text() mustBe "−£340.00"

              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and send correction"
            }
          }
        }
      }

      s"when the balance has failed" - {
        "the user should be redirected to select change and the error should be logged" in {
          val userAnswers = userAnswerWithOnePageChangedAndNilSdilReturn(BroughtIntoUKPage, HowManyBroughtIntoUKPage)
            .copy(warehouseList = warehousesFromSubscription)
            .set(CorrectionReasonPage, "I forgot something").success.value
            .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value
          given
            .commonPrecondition

          setAnswers(userAnswers)

          given.sdilBackend.balance("", false)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SelectChangeController.onPageLoad.url)
            }
          }
        }
      }

    }

    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }

  "POST " + routes.CorrectReturnCheckChangesCYAController.onSubmit.url - {
    "should redirect to select return to correct page" in {
      given
        .commonPrecondition
        .sdilBackend.submitReturnsVariation("XKSDIL000000022")

      setAnswers(userAnswerWithLitresForAllPagesNilSdilReturn
        .set(CorrectionReasonPage, "No longer sell drinks").success.value
        .set(RepaymentMethodPage, BankAccount).success.value)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnUpdateDoneController.onPageLoad.url)
          checkReturnsVariationSubmission.checkReturnVariationSubmissionSent(wireMockServer, populatedReturn)
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, Some(Json.obj()))
  }
}