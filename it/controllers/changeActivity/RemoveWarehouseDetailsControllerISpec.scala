package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.{CheckMode, NormalMode}
import models.SelectChange.ChangeActivity
import models.backend.Site
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.changeActivity._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class RemoveWarehouseDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/secondary-warehouse-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-secondary-warehouse-details/remove/$index"

  val indexOfWarehouseToBeRemoved: String = "warehouseUNO"
  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForChangeActivityRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Are you sure you want to remove this warehouse? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            }
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
  }

  "GET " + checkRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForChangeActivityRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Are you sure you want to remove this warehouse? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            }
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
  }

  "POST " + normalRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForChangeActivityRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        s"should update the session with the new value and redirect to the $SecondaryWarehouseDetailsPage controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
                dataStoredForPage.nonEmpty mustBe true
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

        setAnswers(
          emptyUserAnswersForChangeActivity
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForChangeActivity.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Are you sure you want to remove this warehouse? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to remove this warehouse"
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForChangeActivity.id).get.warehouseList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved),
      Some(Json.obj("value" -> "true")))
  }

  "POST " + checkRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForChangeActivityRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        s"should update the session with the new value and redirect to the $SecondaryWarehouseDetailsPage controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                if (yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
                dataStoredForPage.nonEmpty mustBe true
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

        setAnswers(
          emptyUserAnswersForChangeActivity
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForChangeActivity.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Are you sure you want to remove this warehouse? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to remove this warehouse"
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForChangeActivity.id).get.warehouseList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved),
      Some(Json.obj("value" -> "true")))
  }

}
