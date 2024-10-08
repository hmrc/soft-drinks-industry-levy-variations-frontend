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

package controllers.updateRegisteredDetails

import base.SpecBase
import base.SpecBase.voluntarySubscription
import errors.SessionDatabaseInsertError
import forms.updateRegisteredDetails.ChangeRegisteredDetailsFormProvider
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.ChangeRegisteredDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.updateRegisteredDetails.ChangeRegisteredDetailsView

import scala.concurrent.Future

class ChangeRegisteredDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val changeRegisteredDetailsRoute: String = routes.ChangeRegisteredDetailsController.onPageLoad().url

  val formProvider = new ChangeRegisteredDetailsFormProvider()
  val isVoluntary: Boolean = false
  val form: Form[Seq[ChangeRegisteredDetails]] = formProvider(isVoluntary)

  "ChangeRegisteredDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails
      ) ).build()

      running(application) {
        val request = FakeRequest(GET, changeRegisteredDetailsRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ChangeRegisteredDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, isVoluntary)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when the user is voluntarily registered" in {
      val applicationVoluntarySetup = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails),
        subscription = Some(voluntarySubscription)).build()

      running(applicationVoluntarySetup) {
        val request = FakeRequest(GET, changeRegisteredDetailsRoute)
        val result = route(applicationVoluntarySetup, request).value
        val view = applicationVoluntarySetup.injector.instanceOf[ChangeRegisteredDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, isVoluntary = true)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails
      .set(ChangeRegisteredDetailsPage, ChangeRegisteredDetails.values).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, changeRegisteredDetailsRoute)
        val view = application.injector.instanceOf[ChangeRegisteredDetailsView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ChangeRegisteredDetails.values), isVoluntary)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, changeRegisteredDetailsRoute
        )
        .withFormUrlEncodedBody(("value[0]", ChangeRegisteredDetails.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, changeRegisteredDetailsRoute)
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[ChangeRegisteredDetailsView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isVoluntary)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, changeRegisteredDetailsRoute)
    testRedirectToPostSubmissionIfRequired(UpdateRegisteredDetails, changeRegisteredDetailsRoute)
    testNoUserAnswersError(changeRegisteredDetailsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(UpdateRegisteredDetails))).build()

      running(application) {
        val request =
          FakeRequest(POST, changeRegisteredDetailsRoute
        )
        .withFormUrlEncodedBody(("value[0]", ChangeRegisteredDetails.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }
    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, changeRegisteredDetailsRoute
            )
            .withFormUrlEncodedBody(("value[0]", ChangeRegisteredDetails.values.head.toString))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on changeRegisteredDetails"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}
