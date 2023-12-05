package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.{CheckMode, NormalMode, SmallProducer}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.RemoveSmallProducerConfirmPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class RemoveSmallProducerConfirmControllerISpec extends ControllerITTestHelper {

  val sdilRefPartyDrinks = "XZSDIL000000234"
  val sdilRefSuperCola = "XZSDIL000000235"
  val anotherSdilRef = "XZSDIL000000236"
  val aliasPartyDrinks = "Party Drinks Group"
  val aliasSuperCola = "Super Cola"
  val litreMax: Long = 100000000000000L
  val smallLitre: Long = litreMax - 1
  val largeLitre: Long = litreMax - 1

  val normalRoutePath = s"/remove-small-producer-confirm/$sdilRefPartyDrinks"
  val checkRoutePath = s"/change-remove-small-producer-confirm/$sdilRefPartyDrinks"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the RemoveSmallProducerConfirm page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
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

    userAnswersForCorrectReturnRemoveSmallProducerConfirmPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        "should return OK and render the page without radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
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
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForCorrectReturnRemoveSmallProducerConfirmPage.foreach { case (key, userAnswers) =>
      val userAnswersWithTwoSmallProducers = userAnswers
        .copy(smallProducerList = List(
          SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)),
          SmallProducer(aliasPartyDrinks, anotherSdilRef, (smallLitre, largeLitre))
        ))
      "when the user selects " + key + "and initially has more than one small producer" - {
        "should update the session with the new value and redirect to the small producer details" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithTwoSmallProducers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SmallProducerDetailsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithTwoSmallProducers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SmallProducerDetailsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
      val userAnswersWithOneSmallProducer = userAnswers.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre))))
      "when the user selects " + key + "and initially has one small producer" - {
        "should update the session with the new value and redirect to the small producer details page for false and exemptions for small producers for true" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithOneSmallProducer)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val onwardRoute = if (yesSelected) {
                  routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode).url
                } else {
                  routes.SmallProducerDetailsController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(onwardRoute)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithOneSmallProducer)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val onwardRoute = if (yesSelected) {
                  routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode).url
                } else {
                  routes.SmallProducerDetailsController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(onwardRoute)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
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

        setAnswers(emptyUserAnswersForCorrectReturn.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.removeSmallProducerConfirm" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the RemoveSmallProducerConfirm page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
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

    userAnswersForCorrectReturnRemoveSmallProducerConfirmPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        "should return OK and render the page without radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
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
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }

  s"POST " + checkRoutePath - {
    userAnswersForCorrectReturnRemoveSmallProducerConfirmPage.foreach { case (key, userAnswers) =>
      val userAnswersWithTwoSmallProducers = userAnswers
        .copy(smallProducerList = List(
          SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)),
          SmallProducer(aliasPartyDrinks, anotherSdilRef, (smallLitre, largeLitre))
        ))
      "when the user selects " + key + "and initially has more than one small producer" - {
        "should update the session with the new value and redirect to the small producer details" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithTwoSmallProducers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithTwoSmallProducers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
      val userAnswersWithOneSmallProducer = userAnswers.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre))))
      "when the user selects " + key + "and initially has one small producer" - {
        "should update the session with the new value and redirect to the small producer details page for false and exemptions for small producers for true" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithOneSmallProducer)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val onwardRoute = if (yesSelected) {
                  routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
                } else {
                  routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(onwardRoute)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswersWithOneSmallProducer)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val onwardRoute = if (yesSelected) {
                  routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
                } else {
                  routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(onwardRoute)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(RemoveSmallProducerConfirmPage))
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

        setAnswers(emptyUserAnswersForCorrectReturn.copy(smallProducerList = List(SmallProducer(aliasPartyDrinks, sdilRefPartyDrinks, (smallLitre, largeLitre)))))
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.removeSmallProducerConfirm" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.removeSmallProducerConfirm" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}