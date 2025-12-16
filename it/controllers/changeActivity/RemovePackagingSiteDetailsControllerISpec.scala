package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.{ CheckMode, NormalMode }
import models.SelectChange.ChangeActivity
import models.backend.Site
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.changeActivity.RemovePackagingSiteDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, WsTestClient }

class RemovePackagingSiteDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/packaging-site-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-packaging-site-details/remove/$index"
  val indexOfPackingSiteToBeRemoved: String = "siteUNO"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build.commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(
              controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
            )
          }
        }
      }
    }

    userAnswersForChangeActivityRemovePackagingSiteDetailsPage(indexOfPackingSiteToBeRemoved).foreach {
      case (key, userAnswers) =>
        s"when the userAnswers contains data for the page with " + key + " selected" - {
          s"should return OK and render the page without the " + key + " radio checked" in {
            build.commonPrecondition

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result =
                createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(messages("changeActivity.removePackagingSiteDetails" + ".title"))
                val radioInputs = page.getElementsByClass("govuk-radios__input")
                radioInputs.size() mustBe 2
                radioInputs.get(0).attr("value") mustBe "true"
                radioInputs.get(0).hasAttr("checked") mustBe false
                radioInputs.get(1).attr("value") mustBe "false"
                radioInputs.get(1).hasAttr("checked") mustBe false
                page
                  .getElementById("value-hint")
                  .text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
              }
            }
          }
        }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved)
    )
  }

  s"GET " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build.commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(
              controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
            )
          }
        }
      }
    }

    userAnswersForChangeActivityRemovePackagingSiteDetailsPage(indexOfPackingSiteToBeRemoved).foreach {
      case (key, userAnswers) =>
        s"when the userAnswers contains data for the page with " + key + " selected" - {
          s"should return OK and render the page without the " + key + " radio checked" in {
            build.commonPrecondition

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result =
                createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(messages("changeActivity.removePackagingSiteDetails" + ".title"))
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

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved)
    )
  }

  s"POST " + normalRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForChangeActivityRemovePackagingSiteDetailsPage(indexOfPackingSiteToBeRemoved).foreach {
      case (key, userAnswers) =>
        "when the user selects " + key - {
          "should update the session with the new value and redirect to the index controller" - {
            "when the session contains no data for page" in {
              build.commonPrecondition

              setAnswers(emptyUserAnswersForChangeActivity)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(
                    controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
                  )
                }
              }
            }

            "when the session already contains data for page" in {
              build.commonPrecondition

              setAnswers(userAnswers)
              getAnswers(userAnswers.id).get.packagingSiteList.size mustBe 1
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303

                  val userAnswersAfterTest = getAnswers(userAnswers.id)
                  val dataStoredForPage =
                    userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemovePackagingSiteDetailsPage))
                  if (yesSelected) {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 0
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url
                    )
                  } else {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 1
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
                    )
                  }
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
        build.commonPrecondition

        setAnswers(
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None)))
        )
        getAnswers(emptyUserAnswersForChangeActivity.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("changeActivity.removePackagingSiteDetails" + ".title"))
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("changeActivity.removePackagingSiteDetails" + ".error.required")
            page.getElementById("value-hint").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForChangeActivity.id).get.packagingSiteList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(
      changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
    testAuthenticatedUserButNoUserAnswers(
      changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
  }

  s"POST " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForChangeActivityRemovePackagingSiteDetailsPage(indexOfPackingSiteToBeRemoved).foreach {
      case (key, userAnswers) =>
        "when the user selects " + key - {
          "should update the session with the new value and redirect to the site details controller" - {
            "when the session contains no data for page" in {
              build.commonPrecondition

              setAnswers(emptyUserAnswersForChangeActivity)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(
                    controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
                  )
                }
              }
            }

            "when the session already contains data for page with one packing site" in {
              build.commonPrecondition

              setAnswers(userAnswers)
              getAnswers(userAnswers.id).get.packagingSiteList.size mustBe 1
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  val userAnswersAfterTest = getAnswers(userAnswers.id)
                  val dataStoredForPage =
                    userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemovePackagingSiteDetailsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  if (yesSelected) {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 0
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
                    )
                  } else {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 1
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
                    )
                  }
                }
              }
            }
            "when the session already contains data for page with two packing sites" in {
              build.commonPrecondition

              setAnswers(userAnswers.copy(packagingSiteList = packAtBusinessAddressSites))
              getAnswers(userAnswers.id).get.packagingSiteList.size mustBe 2
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + checkRoutePath(packAtBusinessAddressSites.head._1),
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  val userAnswersAfterTest = getAnswers(userAnswers.id)
                  val dataStoredForPage =
                    userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemovePackagingSiteDetailsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  if (yesSelected) {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 1
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
                    )
                  } else {
                    userAnswersAfterTest.get.packagingSiteList.size mustBe 2
                    res.header(HeaderNames.LOCATION) mustBe Some(
                      routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
                    )
                  }
                }
              }
            }
          }
        }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build.commonPrecondition

        setAnswers(
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None)))
        )
        getAnswers(emptyUserAnswersForChangeActivity.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswersForChangeActivity.id).get.packagingSiteList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("changeActivity.removePackagingSiteDetails" + ".title"))
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("changeActivity.removePackagingSiteDetails" + ".error.required")
            page.getElementById("value-hint").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
          }
        }
      }
    }
    testUnauthorisedUser(
      changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
    testAuthenticatedUserButNoUserAnswers(
      changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved),
      Some(Json.obj("value" -> "true"))
    )
  }
}
