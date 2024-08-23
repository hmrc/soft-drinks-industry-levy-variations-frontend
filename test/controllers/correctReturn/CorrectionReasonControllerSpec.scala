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
import connectors.SoftDrinksIndustryLevyConnector
import forms.correctReturn.CorrectionReasonFormProvider
import models.{NormalMode, SdilReturn, UserAnswers}
import models.SelectChange.CorrectReturn
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.{CorrectReturnBaseCYAPage, CorrectionReasonPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.correctReturn.CorrectionReasonView
import utilities.GenericLogger
import errors.SessionDatabaseInsertError

import scala.concurrent.Future
import org.jsoup.Jsoup
import play.api.inject.guice.GuiceApplicationBuilder

class CorrectionReasonControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CorrectionReasonFormProvider()
  val form = formProvider()

  lazy val correctionReasonRoute = routes.CorrectionReasonController.onPageLoad(NormalMode).url
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }
  
  val userAnswers = emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value

  "CorrectionReason Controller" - {

    "must redirect to CYA when that page has not been submitted" in {
      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, correctionReasonRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CorrectReturnCYAController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, correctionReasonRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectionReasonView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val filledUserAnswers = userAnswers.set(CorrectionReasonPage, "answer").success.value

      val application = correctReturnAction(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, correctionReasonRoute)

        val view = application.injector.instanceOf[CorrectionReasonView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        correctReturnAction(userAnswers = Some(userAnswers))
      .overrides(
        bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, correctionReasonRoute)
        .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, correctionReasonRoute)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CorrectionReasonView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, correctionReasonRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, correctionReasonRoute)
    testNoUserAnswersError(correctionReasonRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = correctReturnAction(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(CorrectReturn))).build()

      running(application) {
        val request =
          FakeRequest(POST, correctionReasonRoute
        )
        .withFormUrlEncodedBody(("value", "answer"))

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
        correctReturnAction(userAnswers = Some(userAnswers))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, correctionReasonRoute)
          .withFormUrlEncodedBody(("value", "answer"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on correctionReason"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
