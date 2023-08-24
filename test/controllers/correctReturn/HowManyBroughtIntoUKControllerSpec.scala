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
import forms.HowManyLitresFormProvider
import models.{NormalMode, LitresInBands}
import models.SelectChange.CorrectReturn
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.HowManyBroughtIntoUKPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.correctReturn.HowManyBroughtIntoUKView
import utilities.GenericLogger
import errors.SessionDatabaseInsertError
import scala.concurrent.Future
import org.jsoup.Jsoup

class HowManyBroughtIntoUKControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HowManyLitresFormProvider
  val form = formProvider()

  lazy val howManyBroughtIntoUKRoute = routes.HowManyBroughtIntoUKController.onPageLoad(NormalMode).url

  "HowManyBroughtIntoUK Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, howManyBroughtIntoUKRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowManyBroughtIntoUKView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn.set(HowManyBroughtIntoUKPage, LitresInBands(100, 200)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howManyBroughtIntoUKRoute)

        val view = application.injector.instanceOf[HowManyBroughtIntoUKView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LitresInBands(100, 200)), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUKRoute)
            .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUKRoute)
            .withFormUrlEncodedBody(("lowBand", ""), ("highBand", ""))

        val boundForm = form.bind(Map("lowBand" -> "", "highBand" -> ""))

        val view = application.injector.instanceOf[HowManyBroughtIntoUKView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, howManyBroughtIntoUKRoute)
    testNoUserAnswersError(howManyBroughtIntoUKRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(CorrectReturn))).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUKRoute)
        .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, howManyBroughtIntoUKRoute)
          .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on howManyBroughtIntoUK"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
