/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{ NormalMode, UserAnswers }
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.changeActivity.*
import play.api.http.HeaderNames
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, WsTestClient }

class ThirdPartyPackagersControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/third-party-packagers"
  val checkRoutePath = "/change-third-party-packagers"
  val thirdPartyPackagersJourneyUserAnswers: UserAnswers = emptyUserAnswersForChangeActivity
    .set(AmountProducedPage, AmountProduced.Small)
    .success
    .value

  override val userAnswersForChangeActivityThirdPartyPackagersPage: Map[String, UserAnswers] = {
    val yesSelected = thirdPartyPackagersJourneyUserAnswers.set(ThirdPartyPackagersPage, true).success.value
    val noSelected = thirdPartyPackagersJourneyUserAnswers.set(ThirdPartyPackagersPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data for the page" - {
      "should return OK and render the ThirdPartyPackagers page with no data populated" in {
        build.commonPrecondition

        setAnswers(thirdPartyPackagersJourneyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
            )
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

    "when the userAnswers contains no data for the previous page" - {
      "should redirect to the Amount Produced page" in {
        build.commonPrecondition

        setAnswers(thirdPartyPackagersJourneyUserAnswers.remove(AmountProducedPage).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AmountProducedController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForChangeActivityThirdPartyPackagersPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build.commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(
                messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
              )
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
      "should return OK and render the ThirdPartyPackagers page with no data populated" in {
        build.commonPrecondition

        setAnswers(thirdPartyPackagersJourneyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
            )
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

    userAnswersForChangeActivityThirdPartyPackagersPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build.commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(
                messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
              )
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
    userAnswersForChangeActivityThirdPartyPackagersPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setAnswers(thirdPartyPackagersJourneyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + normalRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
                )
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ThirdPartyPackagersPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + normalRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )
              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
                )
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ThirdPartyPackagersPage))
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

        setAnswers(thirdPartyPackagersJourneyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + normalRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Error: " + messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
            )
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages(
              "Select yes if you use any third parties in the UK to package liable drinks on your behalf"
            )
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + normalRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }

  s"POST " + checkRoutePath - {
    userAnswersForChangeActivityThirdPartyPackagersPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setAnswers(thirdPartyPackagersJourneyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + checkRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad().url)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ThirdPartyPackagersPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + checkRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad().url)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ThirdPartyPackagersPage))
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

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + checkRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Error: " + messages("Do you use any third parties in the UK to package liable drinks on your behalf?")
            )
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages(
              "Select yes if you use any third parties in the UK to package liable drinks on your behalf"
            )
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + checkRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }
}
