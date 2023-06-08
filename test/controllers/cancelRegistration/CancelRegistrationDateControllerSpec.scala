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

package controllers.cancelRegistration

import java.time.{LocalDate, ZoneOffset}
import base.SpecBase
import config.FrontendAppConfig
import forms.cancelRegistration.CancelRegistrationDateFormProvider
import models.NormalMode
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.cancelRegistration.CancelRegistrationDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.cancelRegistration.CancelRegistrationDateView

import scala.concurrent.Future
import org.jsoup.Jsoup
import utilities.GenericLogger
import errors.SessionDatabaseInsertError

class CancelRegistrationDateControllerSpec extends SpecBase with MockitoSugar {
  val appConfig = application.injector.instanceOf[FrontendAppConfig]
  val formProvider = new CancelRegistrationDateFormProvider(appConfig)
  private def form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val cancelRegistrationDateRoute = routes.CancelRegistrationDateController.onPageLoad(NormalMode).url


  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, cancelRegistrationDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, cancelRegistrationDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "CancelRegistrationDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CancelRegistrationDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(getRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCancelRegistration.set(CancelRegistrationDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[CancelRegistrationDateView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration))
          .overrides(
            bind[NavigatorForCancelRegistration].toInstance(new FakeNavigatorForCancelRegistration(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).build()

      val request =
        FakeRequest(POST, cancelRegistrationDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CancelRegistrationDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must fail if the setting of userAnswers fails" in {
      val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

        val application =
          applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure))
            .overrides(
              bind[NavigatorForCancelRegistration].toInstance(new FakeNavigatorForCancelRegistration(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val result = route(application, postRequest).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration))
          .overrides(
            bind[NavigatorForCancelRegistration].toInstance(new FakeNavigatorForCancelRegistration(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>

          await(route(application, postRequest).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on cancelRegistrationDate"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
