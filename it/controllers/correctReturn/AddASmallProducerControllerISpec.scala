package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SmallProducer
import models.correctReturn.AddASmallProducer
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.correctReturn.AddASmallProducerPage
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class AddASmallProducerControllerISpec extends ControllerITTestHelper {

  val sdilRefSparkyJuice = "XZSDIL000000234"
  val aliasSparkyJuice = "Sparky Juice"

  val sdilRefSuperCola = "XZSDIL000000235"
  val aliasSuperCola = "Super Cola"

  val litreMax: Long = 100000000000000L
  val litre: Long = litreMax - 1

  def exemptionsForSmallProducersFullAnswers = emptyUserAnswersForCorrectReturn
  def addASmallProducerPartialAnswers = emptyUserAnswersForCorrectReturn

  def addASmallProducerFullAnswers = emptyUserAnswersForCorrectReturn
    .set(AddASmallProducerPage, AddASmallProducer(Some(aliasSuperCola), sdilRefSuperCola, 10, 20)).success.value

    "Ask user to input a registered small producer's details" in {
      val userAnswers = addASmallProducerPartialAnswers
      setAnswers(userAnswers)
      given.commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$correctReturnBaseUrl/add-small-producer")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the new form data and navigate to small producer details page " in {

      val expectedResult: Some[List[SmallProducer]] = Some(List(SmallProducer(alias = "Super Cola Ltd" ,
        sdilRef = "XZSDIL000000234", litreage = (1000L,1000L))))

      given
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = addASmallProducerFullAnswers
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$correctReturnBaseUrl/add-small-producer")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("producerName" -> "Super Cola Ltd", "referenceNumber" -> "XZSDIL000000234", "lowBand" -> "1000", "highBand" -> "1000"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList) mustBe expectedResult
        }

      }

    }

    testUnauthorisedUser(correctReturnBaseUrl + "/add-small-producer")
}

