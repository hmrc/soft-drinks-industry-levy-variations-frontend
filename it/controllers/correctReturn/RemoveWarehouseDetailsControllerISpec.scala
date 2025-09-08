package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.backend.Site
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{WsTestClient, FakeRequest}

class RemoveWarehouseDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/remove-warehouse-confirm/$index"
  def checkRoutePath(index: String) = s"/change-remove-warehouse-confirm/$index"
  val indexOfWarehouseToBeRemoved: String = "warehouseUNO"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForSelectChange(CorrectReturn)
        .copy(correctReturnPeriod = None))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          }
        }
      }
    }

    userAnswersForCorrectReturnRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          build
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("correctReturn.removeWarehouseDetails" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementById("warehouseToRemove").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            }
          }
        }
      }
    }
    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
  }

  s"GET " + checkRoutePath(indexOfWarehouseToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForSelectChange(CorrectReturn)
          .copy(correctReturnPeriod = None))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          }
        }
      }
    }

    userAnswersForCorrectReturnRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          build
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("correctReturn.removeWarehouseDetails" + ".title"))
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
    }

    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
  }

  s"POST " + normalRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForCorrectReturnRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, _) =>
      "when the user selects " + key - {
        "should redirect to the Secondary warehouse details controller" - {
          "when the session contains no data for page" in {
            build
              .commonPrecondition

            setUpForCorrectReturn(completedUserAnswersForCorrectReturnNewPackerOrImporter.copy(warehouseList =
              Map("warehouseDOS" -> Site(ukAddress))))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
              }
            }
          }

          "when the session already contains data for page and there is only one warehouse" in {
            build
              .commonPrecondition
            val userAnswers = completedUserAnswersForCorrectReturnNewPackerOrImporter.copy(warehouseList =
              Map("warehouseUNO" -> Site(ukAddress)))
            setUpForCorrectReturn(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val userAnswersAfterTest = getAnswers(sdilNumber)
                if(yesSelected) {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition

        setUpForCorrectReturn(
          emptyUserAnswersForCorrectReturn
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForCorrectReturn.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("correctReturn.removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("correctReturn.removeWarehouseDetails" + ".error.required")
            page.getElementById("warehouseToRemove").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForCorrectReturn.id).get.warehouseList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl +
      normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForCorrectReturnRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, _) =>
      "when the user selects " + key - {
        "should redirect to the Secondary Warehouse controller " - {
          "when the session contains no data for page" in {
            build
              .commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.copy(warehouseList =
              Map("warehouseDOS" -> Site(ukAddress), "warehouseUNO" -> Site(ukAddress))))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
              }
            }
          }
        }
        "should redirect to the Secondary Warehouse Details controller when there are multiple warehouses" - {
          "when the session already contains data for page" in {
            build
              .commonPrecondition

            setUpForCorrectReturn(completedUserAnswersForCorrectReturnNewPackerOrImporter.copy(warehouseList =
              Map("warehouseUNO" -> Site(ukAddress), "warehouseTwo" -> Site(ukAddress))))
            getAnswers(completedUserAnswersForCorrectReturnNewPackerOrImporter.id).get.warehouseList.size mustBe 2
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
                val userAnswersAfterTest = getAnswers(completedUserAnswersForCorrectReturnNewPackerOrImporter.id)
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 2
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition

        setUpForCorrectReturn(
          emptyUserAnswersForCorrectReturn
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForCorrectReturn.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswersForCorrectReturn.id).get.warehouseList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("correctReturn.removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("correctReturn.removeWarehouseDetails" + ".error.required")
            page.getElementById("warehouseToRemove").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl +
      checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }
}
