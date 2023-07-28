package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.{ReturnPeriod, SmallProducer}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.{SelectPage}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class AddASmallProducerControllerISpec extends ControllerITTestHelper {

  private val normalRoutePath = "/add-small-producer"
  private val checkRoutePath = "/change-add-small-producer"

  private val sdilRefSuperCola = "XZSDIL000000235"
  private val aliasSuperCola = "Super Cola"
  
  private val litres = 1000L

  private val validAddASmallProducer = Json.obj("producerName" -> aliasSuperCola, "referenceNumber" -> sdilRefSuperCola, "lowBand" -> litres.toString, "highBand" -> litres.toString)

  private val returnPeriod: ReturnPeriod = ReturnPeriod(2018, 1)

  private def userAnswersWithReturnPeriodSet = emptyUserAnswersForCorrectReturn
    .set(SelectPage, returnPeriod).success.value

  private def userAnswersWithSmallProducersSet = emptyUserAnswersForCorrectReturn
    .copy(smallProducerList = List(SmallProducer(aliasSuperCola, sdilRefSuperCola, (100, 200))))

  "GET " + normalRoutePath - {
    "Ask user to input a registered small producer's details" in {
      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)
      given.commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(correctReturnBaseUrl + normalRoutePath)
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }
  "GET " + checkRoutePath - {
    "Ask user to input a registered small producer's details" in {
      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)
      given.commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(correctReturnBaseUrl + checkRoutePath)
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }
  "POST " + normalRoutePath - {
    "Post the new form data and return form with error if SDIL reference number already exists as a small producer" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = userAnswersWithSmallProducersSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + normalRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Error: " + Messages("correctReturn.addASmallProducer.title"))
          val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#referenceNumber"
          errorSummary.text() mustBe Messages("correctReturn.addASmallProducer.error.referenceNumber.exists")
        }

      }

    }

    "Redirect to index controller if return period has not been set" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = emptyUserAnswersForCorrectReturn
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + normalRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe Some(List())
        }

      }

    }

    "Post the new form data and return form with error if SDIL reference number is not a valid small producer" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = false)

      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + normalRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Error: " + Messages("correctReturn.addASmallProducer.title"))
          val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#referenceNumber"
          errorSummary.text() mustBe Messages("correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer")
        }

      }

    }

    "Post the new form data and navigate to small producer details page" in {

      val expectedResult: Some[List[SmallProducer]] = Some(List(SmallProducer(alias = aliasSuperCola,
        sdilRef = sdilRefSuperCola, litreage = (litres, litres))))

      given
        .commonPreconditionChangeSubscription(aSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = true)

      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + normalRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe expectedResult
        }

      }

    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(validAddASmallProducer))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(validAddASmallProducer))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath, Some(validAddASmallProducer))
  }
  "POST " + checkRoutePath - {
    "Post the new form data and return form with error if SDIL reference number already exists as a small producer" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = userAnswersWithSmallProducersSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + checkRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Error: " + Messages("correctReturn.addASmallProducer.title"))
          val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#referenceNumber"
          errorSummary.text() mustBe Messages("correctReturn.addASmallProducer.error.referenceNumber.exists")
        }

      }

    }

    "Redirect to index controller if return period has not been set" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = emptyUserAnswersForCorrectReturn
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + checkRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe Some(List())
        }

      }

    }

    "Post the new form data and return form with error if SDIL reference number is not a valid small producer" in {
      given
        .commonPreconditionChangeSubscription(aSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = false)

      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + checkRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Error: " + Messages("correctReturn.addASmallProducer.title"))
          val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#referenceNumber"
          errorSummary.text() mustBe Messages("correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer")
        }

      }

    }

    "Post the new form data and navigate to small producer details page" in {

      val expectedResult: Some[List[SmallProducer]] = Some(List(SmallProducer(alias = aliasSuperCola,
        sdilRef = sdilRefSuperCola, litreage = (litres, litres))))

      given
        .commonPreconditionChangeSubscription(aSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = true)

      val userAnswers = userAnswersWithReturnPeriodSet
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(correctReturnBaseUrl + checkRoutePath)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(validAddASmallProducer)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe expectedResult
        }

      }

    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
  }
}

