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
import forms.HowManyLitresFormProvider
import models.{ LitresInBands, NormalMode, SdilReturn, UserAnswers }
import models.SelectChange.CorrectReturn
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.HowManyBroughtIntoUkFromSmallProducersPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.correctReturn.HowManyBroughtIntoUkFromSmallProducersView
import utilities.GenericLogger
import errors.SessionDatabaseInsertError

import scala.concurrent.Future
import org.jsoup.Jsoup
import play.api.inject.guice.GuiceApplicationBuilder

class HowManyBroughtIntoUkFromSmallProducersControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HowManyLitresFormProvider
  val form = formProvider()

  lazy val howManyBroughtIntoUkFromSmallProducersRoute =
    routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode).url
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(
    userAnswers: Option[UserAnswers],
    optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)
  ): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }

  "HowManyBroughtIntoUkFromSmallProducers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, howManyBroughtIntoUkFromSmallProducersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowManyBroughtIntoUkFromSmallProducersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(using request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(100, 200))
        .success
        .value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howManyBroughtIntoUkFromSmallProducersRoute)

        val view = application.injector.instanceOf[HowManyBroughtIntoUkFromSmallProducersView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LitresInBands(100, 200)), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

      val application =
        correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUkFromSmallProducersRoute)
            .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUkFromSmallProducersRoute)
            .withFormUrlEncodedBody(("lowBand", ""), ("highBand", ""))

        val boundForm = form.bind(Map("lowBand" -> "", "highBand" -> ""))

        val view = application.injector.instanceOf[HowManyBroughtIntoUkFromSmallProducersView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(using request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, howManyBroughtIntoUkFromSmallProducersRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, howManyBroughtIntoUkFromSmallProducersRoute)
    testNoUserAnswersError(howManyBroughtIntoUkFromSmallProducersRoute)

    "must fail if the setting of userAnswers fails" in {

      val application =
        correctReturnAction(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(CorrectReturn))).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyBroughtIntoUkFromSmallProducersRoute)
            .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

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
        correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, howManyBroughtIntoUkFromSmallProducersRoute)
              .withFormUrlEncodedBody(("litres.lowBand", "1000"), ("litres.highBand", "2000"))

          await(route(application, request).value)
          events
            .collectFirst { case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on howManyBroughtIntoUkFromSmallProducers"
            }
            .getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
