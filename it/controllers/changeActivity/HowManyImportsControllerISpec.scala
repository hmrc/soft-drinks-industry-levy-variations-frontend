package controllers.changeActivity

import controllers.LitresISpecHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyImportsPage, ImportsPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient


class HowManyImportsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-imports-next-12-months"
  val checkRoutePath = "/change-how-many-imports-next-12-months"

  val userAnswers: UserAnswers = emptyUserAnswersForChangeActivity.set(HowManyImportsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val path = if(mode == NormalMode) {
      normalRoutePath
    } else {
      checkRoutePath
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for Imports with no data populated" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("howManyImports" + ".title"))
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("howManyImports" + ".title"))
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testUnauthorisedUser(changeActivityBaseUrl + path)
      testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + path)
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + path)
    }

    s"POST " + path - {

      "should return 400 with required error" - {
        val errorTitle = "Error: " + Messages("howManyImports.title")

        "when no questions are answered" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + path, emptyJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testEmptyFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with no numeric answers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + path, jsonWithNoNumeric
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNoNumericFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with negative numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + path, jsonWithNegativeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNegativeFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with decimal numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + path, jsonWithDecimalNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testDecimalFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with out of max range numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + path, jsonWithOutOfRangeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testOutOfMaxValFormErrors(page, errorTitle)
            }
          }
        }
      }

      testUnauthorisedUser(changeActivityBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
    }
  }

  "in normal mode when amount produced is none" - {
    "and client is a contract packer" - {
      "and adds import litres and submits their answer" - {
        "they should be redirected to packaging site details" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }

    "and client is a contract packer but has no packaging sites" - {
      "and adds import litres and submits their answer" - {
        "they should be redirected to pack at business address" in {
          given.commonPreconditionChangeSubscription(aSubscription)
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }

    "and client is NOT a contract packer" - {
      "and adds import litres and submits their answer" - {
        "they should be redirected to secondary warehouse details" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }
  }

  "in check mode when amount produced is none" - {
    "and client is a contract packer" - {
      "and changes existing import litres and submits their answer" - {
        "they should be redirected to packaging site details" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(HowManyImportsPage, LitresInBands(2L,2L)).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }

      "and adds import litres (with no previous data) and submits their answer" - {
        "they should be redirected to packaging site details" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }

    "and client is a contract packer but has no packaging sites" - {
      "and changes existing import litres and submits their answer" - {
        "they should be redirected to check your answers" in {
          given.commonPreconditionChangeSubscription(aSubscription)
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(HowManyImportsPage, LitresInBands(2L, 2L)).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }

      "and adds import litres (with no previous data) and submits their answer" - {
        "they should be redirected to check your answers" in {
          given.commonPreconditionChangeSubscription(aSubscription)
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }

    "and client is NOT a contract packer" - {
      "and changes existing import litres and submits their answer" - {
        "they should be redirected to check your answers page" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(HowManyImportsPage, LitresInBands(2L, 2L)).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }

      "and adds import litres (with no previous data) and submits their answer" - {
        "they should be redirected to check your answers page" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe litresInBands
            }
          }
        }
      }
    }
  }
}
