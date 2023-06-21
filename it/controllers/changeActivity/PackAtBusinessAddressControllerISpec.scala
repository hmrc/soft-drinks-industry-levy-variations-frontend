package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.backend.{Site, UkAddress}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.PackAtBusinessAddressPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class PackAtBusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/pack-at-business-address"
  val checkRoutePath = "/change-pack-at-business-address"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
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
              page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
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
              page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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
  }

  s"POST " + normalRoutePath - {
    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            val expectedPackingSiteListDB = Some(Map("1" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                if (key == "yes") {
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                } else {
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }
          }

          userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (previousKey, userAnswers1) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val expectedPackingSiteListDB = Some(Map("1" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)))
              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = expectedPackingSiteListDB.get)
              } else {
                userAnswers
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                   if (previousKey == "yes" && key == "yes") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "no" && key == "yes") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "yes" && key == "no") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "no" && key == "no") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  }
                }
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
            page.title must include("Error: " + Messages("changeActivity.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            val expectedPackingSiteListDB = Some(Map("1" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                if (key == "yes") {
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                } else {
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }
          }

          userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (previousKey, userAnswers1) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val expectedPackingSiteListDB = Some(Map("1" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)))
              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = expectedPackingSiteListDB.get)
              } else {
                userAnswers
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  if (previousKey == "yes" && key == "yes") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "no" && key == "yes") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe expectedPackingSiteListDB
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "yes" && key == "no") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  } else if (previousKey == "no" && key == "no") {
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                    getAnswers(userAnswers.id).map(data => data.packagingSiteList) mustBe Some(Map())
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe yesSelected
                  }
                }
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
            page.title must include("Error: " + Messages("changeActivity.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
