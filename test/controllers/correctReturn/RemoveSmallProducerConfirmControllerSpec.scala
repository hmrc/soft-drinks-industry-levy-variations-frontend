/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.correctReturn

import base.SpecBase
import errors.SessionDatabaseInsertError
import forms.correctReturn.RemoveSmallProducerConfirmFormProvider
import models.SelectChange.CorrectReturn
import models.{NormalMode, SelectChange, UserAnswers}
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.RemoveSmallProducerConfirmPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.RemoveSmallProducerConfirmView

import scala.concurrent.Future

class RemoveSmallProducerConfirmControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveSmallProducerConfirmFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val removeSmallProducerConfirmRoute = routes.RemoveSmallProducerConfirmController.onPageLoad(s"$sdilReferenceParty").url

  val smallProducerName = "Super Lemonade Plc"

  val userAnswersData: JsObject = Json.obj(
    RemoveSmallProducerConfirmPage.toString -> Json.obj(
      "producerName" -> producerName,
      "referenceNumber" -> sdilReference,
      "lowBand" -> litres,
      "highBand" -> litres
    )
  )

  "RemoveSmallProducerConfirm Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers: UserAnswers = UserAnswers(sdilNumber,  SelectChange.CorrectReturn, data = userAnswersData, smallProducerList = smallProducerList)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() must include(Messages("Are you sure you want to remove this small producer? - Soft Drinks Industry Levy - GOV.UK"))
        page.getElementsByTag("h1").text() mustEqual Messages("Are you sure you want to remove this small producer?")
        contentAsString(result) mustEqual view(form, NormalMode, sdilReferenceParty, producerNameParty)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers: UserAnswers = UserAnswers(sdilNumber,  SelectChange.CorrectReturn, data = userAnswersData, smallProducerList = smallProducerList)

      val application = applicationBuilder(userAnswers = Some(userAnswers.set(RemoveSmallProducerConfirmPage, true).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)

        val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, sdilReferenceParty, producerNameParty)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers: UserAnswers = UserAnswers(sdilNumber,  SelectChange.CorrectReturn, data = userAnswersData, smallProducerList = smallProducerList)

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers.set(RemoveSmallProducerConfirmPage, true).success.value))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeSmallProducerConfirmRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers: UserAnswers = UserAnswers(sdilNumber,  SelectChange.CorrectReturn, data = userAnswersData, smallProducerList = smallProducerList)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeSmallProducerConfirmRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sdilReferenceParty, producerNameParty)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, removeSmallProducerConfirmRoute)
    testNoUserAnswersError(removeSmallProducerConfirmRoute)

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList)))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, removeSmallProducerConfirmRoute)
          .withFormUrlEncodedBody(("value", "false"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on removeSmallProducerConfirm"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
