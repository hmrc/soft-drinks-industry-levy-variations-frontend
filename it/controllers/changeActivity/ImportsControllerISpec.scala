package controllers.changeActivity

import controllers.{ControllerITTestHelper, LitresISpecHelper}
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyImportsPage, ImportsPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ImportsControllerISpec extends ControllerITTestHelper with LitresISpecHelper {

  val normalRoutePath = "/imports"
  val checkRoutePath = "/change-imports"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.imports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.imports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.imports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.imports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(NormalMode).url
                } else {
                  defaultCall.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(NormalMode).url
                } else {
                  defaultCall.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.imports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.imports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        val yesSelected = key == "yes"
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if(yesSelected) {
                  routes.HowManyImportsController.onPageLoad(CheckMode).url
                } else {
                  routes.ChangeActivityCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(CheckMode).url
                } else {
                  routes.ChangeActivityCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.imports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.imports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }

  "in normal mode when amount produced is none" - {
    "and client is a contract packer" - {
      "and user answers no to the imports question and submits their answer" - {
        "they should be redirected to packaging site details" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe false
            }
          }
        }
      }

      "and answers yes to the imports question and submits their answer" - {
        "they should be redirected to how many litres imported page" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)


          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> true)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyImportsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe true
            }
          }
        }
      }
    }

    "and client is NOT a contract packer" - {
      "and answers no to the imports question and submits their answer" - {
        "they should be redirected to suggest de-registration page if they have no pending returns" in {

          given.noReturnPendingPreCondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.SuggestDeregistrationController.onPageLoad().url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe false
            }
          }
        }

        "they should be redirected to packaging file return before de-registration page if they have pending returns" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad().url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe false
            }
          }
        }
      }

      "and answers yes to the imports question and submits their answer" - {
        "they should be redirected to how many litres imported page" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> true)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyImportsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe true
            }
          }
        }
      }
    }
  }


  ///////////////////////////

  "in check mode when amount produced is none" - {
    "and client is a contract packer" - {
      "and user answers changes a previous yes answer to imports to no" - {
        "they should be redirected to check your answers" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, litresInBands).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              val dataStoredForLitres = getAnswers(sdilNumber).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
              dataStoredForPage.get mustBe false
              dataStoredForLitres mustBe None
            }
          }
        }
      }

      "and answers yes to the imports question and submits their answer" - {
        "they should be redirected to how many litres imported page" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, true).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)


          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> true)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyImportsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe true
            }
          }
        }
      }
    }

    "and client is NOT a contract packer" - {
      "and answers no to the imports question and submits their answer" - {
        "they should be redirected to suggest de-registration page if they have no pending returns" in {

          given.noReturnPendingPreCondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.SuggestDeregistrationController.onPageLoad().url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe false
            }
          }
        }

        "they should be redirected to packaging file return before de-registration page if they have pending returns" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> false)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad().url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe false
            }
          }
        }
      }

      "and answers yes to the imports question and submits their answer" - {
        "they should be redirected to how many litres imported page" in {

          given.commonPrecondition
          setAnswers(emptyUserAnswersForChangeActivity
            .set(ContractPackingPage, false).success.value
            .set(AmountProducedPage, AmountProduced.None).success.value)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> true)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyImportsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.get mustBe true
            }
          }
        }
      }
    }
  }
}
