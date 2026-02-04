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
import errors.SessionDatabaseInsertError
import forms.updateRegisteredDetails.UpdateContactDetailsFormProvider
import models.NormalMode
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ContactDetails
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.updateRegisteredDetails.UpdateContactDetailsView

import scala.concurrent.Future

class UpdateContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new UpdateContactDetailsFormProvider()
  val form = formProvider()

  lazy val updateContactDetailsRoute = routes.UpdateContactDetailsController.onPageLoad(NormalMode).url

  val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(
    data = Json.obj(
      "updateRegisteredDetails" -> Json.obj(
        UpdateContactDetailsPage.toString -> Json.obj(
          "fullName"    -> "Testing Example",
          "position"    -> "Job Position",
          "phoneNumber" -> "080073282942",
          "email"       -> "email@test.com"
        )
      )
    )
  )

  "UpdateContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(GET, updateContactDetailsRoute)

        val view = application.injector.instanceOf[UpdateContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(using request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, updateContactDetailsRoute)

        val view = application.injector.instanceOf[UpdateContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ContactDetails("Testing Example", "Job Position", "080073282942", "email@test.com")),
          NormalMode
        )(using request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails]
              .toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, updateContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("fullName", "Testing Example"),
              ("position", "Job Position"),
              ("phoneNumber", "080073282942"),
              ("email", "email@test.com")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, updateContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UpdateContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(using request, messages(application)).toString
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, updateContactDetailsRoute)
    testRedirectToPostSubmissionIfRequired(UpdateRegisteredDetails, updateContactDetailsRoute)
    testNoUserAnswersError(updateContactDetailsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers =
        Some(userDetailsWithSetMethodsReturningFailure(UpdateRegisteredDetails))
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, updateContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("fullName", "Testing Example"),
              ("position", "Job Position"),
              ("phoneNumber", "080073282942"),
              ("email", "email@test.com")
            )

        val result = route(application, request).value
        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - Soft Drinks Industry Levy - GOV.UK"
      }
    }
    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails]
              .toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, updateContactDetailsRoute)
              .withFormUrlEncodedBody(
                ("fullName", "Testing Example"),
                ("position", "Job Position"),
                ("phoneNumber", "080073282942"),
                ("email", "email@test.com")
              )

          await(route(application, request).value)
          events
            .collectFirst { case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on updateContactDetails"
            }
            .getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
