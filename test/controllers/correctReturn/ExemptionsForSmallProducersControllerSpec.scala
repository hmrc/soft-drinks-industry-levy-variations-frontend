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
import forms.correctReturn.ExemptionsForSmallProducersFormProvider
import models.NormalMode
import models.SelectChange.CorrectReturn
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.ExemptionsForSmallProducersPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.ExemptionsForSmallProducersView

import scala.concurrent.Future
class ExemptionsForSmallProducersControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val exemptionsForSmallProducersRoute: String = routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode).url

  val formProvider = new ExemptionsForSmallProducersFormProvider()
  val form = formProvider()

  "ExemptionsForSmallProducers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) ).build()

      running(application) {
        val request = FakeRequest(GET, exemptionsForSmallProducersRoute
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExemptionsForSmallProducersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn
      .set(ExemptionsForSmallProducersPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, exemptionsForSmallProducersRoute
        )

        val view = application.injector.instanceOf[ExemptionsForSmallProducersView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) )
      .overrides(
        bind[NavigatorForCorrectReturn
      ].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)
      ),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, exemptionsForSmallProducersRoute
        )
        .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) ).build()

      running(application) {
        val request =
          FakeRequest(POST, exemptionsForSmallProducersRoute
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ExemptionsForSmallProducersView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, exemptionsForSmallProducersRoute)
    testNoUserAnswersError(exemptionsForSmallProducersRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(CorrectReturn))).build()

      running(application) {
        val request =
          FakeRequest(POST, exemptionsForSmallProducersRoute
        )
        .withFormUrlEncodedBody(("value", "true"))

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
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, exemptionsForSmallProducersRoute
            )
            .withFormUrlEncodedBody(("value", "true"))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on exemptionsForSmallProducers"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}