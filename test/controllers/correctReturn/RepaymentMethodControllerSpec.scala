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
import forms.correctReturn.RepaymentMethodFormProvider
import models.NormalMode
import models.SelectChange.ChangeActivity
import models.correctReturn.RepaymentMethod
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.RepaymentMethodPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.RepaymentMethodView

import scala.concurrent.Future

class RepaymentMethodControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val repaymentMethodRoute: String = routes.RepaymentMethodController.onPageLoad(NormalMode).url

  val formProvider = new RepaymentMethodFormProvider()
  val form: Form[RepaymentMethod] = formProvider()

  "RepaymentMethod Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, repaymentMethodRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RepaymentMethodView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswersWithRepaymentMethod = emptyUserAnswersForCorrectReturn
      .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithRepaymentMethod)).build()

      running(application) {
        val request = FakeRequest(GET, repaymentMethodRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RepaymentMethodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(RepaymentMethod.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
      .overrides(
        bind[NavigatorForCorrectReturn
      ].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, repaymentMethodRoute
        )
        .withFormUrlEncodedBody(("value", RepaymentMethod.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, repaymentMethodRoute
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RepaymentMethodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(ChangeActivity, repaymentMethodRoute)
    testNoUserAnswersError(repaymentMethodRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(ChangeActivity))).build()

      running(application) {
        val request =
          FakeRequest(POST, repaymentMethodRoute)
        .withFormUrlEncodedBody(("value", RepaymentMethod.values.head.toString))

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
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, repaymentMethodRoute
            )
            .withFormUrlEncodedBody(("value", RepaymentMethod.values.head.toString))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on repaymentMethod"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}