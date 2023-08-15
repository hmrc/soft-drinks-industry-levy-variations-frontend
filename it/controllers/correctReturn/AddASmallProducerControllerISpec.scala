package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.{ReturnPeriod, SmallProducer}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
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
  private def userAnswersWithSmallProducersSet = emptyUserAnswersForCorrectReturn
    .copy(smallProducerList = List(SmallProducer(aliasSuperCola, sdilRefSuperCola, (100, 200))))

  def testReturnPeriodNotSetForCorrectReturn(url: String): Unit = {
    "the return period has not been selected" - {
      "redirect to Select controller" in {
        setAnswers(emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None))
        given.commonPreconditionChangeSubscription(diffSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(url)
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe routes.SelectController.onPageLoad.url
          }
        }
      }
    }
  }

  "GET " + normalRoutePath - {
    "Ask user to input a registered small producer's details" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
      setAnswers(userAnswers)
      given.commonPreconditionChangeSubscription(diffSubscription)

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
    testReturnPeriodNotSetForCorrectReturn(correctReturnBaseUrl + normalRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }
  "GET " + checkRoutePath - {
    "Ask user to input a registered small producer's details" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
      setAnswers(userAnswers)
      given.commonPreconditionChangeSubscription(diffSubscription)

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
    testReturnPeriodNotSetForCorrectReturn(correctReturnBaseUrl + checkRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }
  "POST " + normalRoutePath - {
    "Post the new form data and return form with error if SDIL reference number already exists as a small producer" in {
      given
        .commonPreconditionChangeSubscription(diffSubscription)

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

    "Redirect to Select controller if return period has not been set" in {
      given
        .commonPreconditionChangeSubscription(diffSubscription)

      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None)
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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe Some(List())
        }

      }

    }

    "Post the new form data and return form with error if SDIL reference number is not a valid small producer" in {
      given
        .commonPreconditionChangeSubscription(diffSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = false)

      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod))
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
        .commonPreconditionChangeSubscription(diffSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = true)

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
        .commonPreconditionChangeSubscription(diffSubscription)

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

    "Redirect to select controller if return period has not been set" in {
      given
        .commonPreconditionChangeSubscription(diffSubscription)

      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None)
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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.SelectController.onPageLoad.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe Some(List())
        }

      }

    }

    "Post the new form data and return form with error if SDIL reference number is not a valid small producer" in {
      given
        .commonPreconditionChangeSubscription(diffSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = false)

      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod))
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
        .commonPreconditionChangeSubscription(diffSubscription)
        .smallProducerStatus(sdilRefSuperCola, returnPeriod, smallProducerStatus = true)

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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe expectedResult
        }
      }
    }

    "render the error page when the call to get small producer status fails" in {

      given
        .commonPreconditionChangeSubscription(diffSubscription)
        .smallProducerStatusError(sdilRefSuperCola, returnPeriod)

      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod))
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
          res.status mustBe 500
          val page = Jsoup.parse(res.body)
          page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(validAddASmallProducer))
  }
}

