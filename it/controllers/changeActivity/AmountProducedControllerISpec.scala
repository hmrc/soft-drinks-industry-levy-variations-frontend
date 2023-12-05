package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.NormalMode
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, Small}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.AmountProducedPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class AmountProducedControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/amount-produced"
  val checkRoutePath = "/change-amount-produced"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the AmountProduced page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.amountProduced" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 3

            AmountProduced.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    AmountProduced.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, radio).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.amountProduced" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe AmountProduced.values.size

              AmountProduced.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
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
      "should return OK and render the AmountProduced page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.amountProduced" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 3

            AmountProduced.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    AmountProduced.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, radio).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.amountProduced" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 3

              AmountProduced.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
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
    AmountProduced.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                if(radio == Large){
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                }else if (radio == Small) {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
                }else {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                }
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[AmountProduced]](None)(_.get(AmountProducedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, radio).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                if(radio == Large){
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                }else if (radio == Small) {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
                }else {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                }

                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[AmountProduced]](None)(_.get(AmountProducedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
      }
    }

    "when the user does not select an option" - {
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
            page.title must include("Error: " + Messages("changeActivity.amountProduced" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select how many litres of your own brands of liable drinks have been packaged globally in the past 12 months"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

    AmountProduced.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the corresponding next page" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                radio match {
                  case AmountProduced.Large => res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                  case AmountProduced.Small => res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
                  case AmountProduced.None => res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                }

                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[AmountProduced]](None)(_.get(AmountProducedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, radio).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[AmountProduced]](None)(_.get(AmountProducedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
        "should update the session with the new value and redirect check your answers controller" - {
          "when the session matches what the user has submitted" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity.set(AmountProducedPage, radio).success.value)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                radio match {
                  case AmountProduced.Large => res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  case AmountProduced.Small => res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  case AmountProduced.None => res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                }

                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[AmountProduced]](None)(_.get(AmountProducedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
      }
    }

    "when the user does not select an option" - {
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
            page.title must include("Error: " + Messages("changeActivity.amountProduced" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select how many litres of your own brands of liable drinks have been packaged globally in the past 12 months"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
