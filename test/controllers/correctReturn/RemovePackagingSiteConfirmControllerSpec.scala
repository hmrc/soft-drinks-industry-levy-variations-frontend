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
import errors.SessionDatabaseInsertError
import forms.correctReturn.RemovePackagingSiteConfirmFormProvider
import models.SelectChange.CorrectReturn
import models.backend.{ Site, UkAddress }
import models.{ NormalMode, SdilReturn, UserAnswers }
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.correctReturn.RemovePackagingSiteConfirmView

import scala.concurrent.Future

class RemovePackagingSiteConfirmControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemovePackagingSiteConfirmFormProvider()
  val form = formProvider()

  val indexOfPackingSiteToBeRemoved: String = "foobar"
  lazy val packingSiteDetailsRemoveRoute =
    routes.RemovePackagingSiteConfirmController.onPageLoad(NormalMode, indexOfPackingSiteToBeRemoved).url
  val addressOfPackingSite: UkAddress = UkAddress(List("foo"), "bar", None)
  val packingSiteTradingName: String = "a name for a packing site here"
  val userAnswersWithPackingSite: UserAnswers = emptyUserAnswersForCorrectReturn
    .copy(packagingSiteList =
      Map(indexOfPackingSiteToBeRemoved -> Site(addressOfPackingSite, Some(packingSiteTradingName), None, None))
    )
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(
    userAnswers: Option[UserAnswers],
    optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)
  ): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }
  "RemovePackagingSiteConfirm Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = correctReturnAction(userAnswers = Some(userAnswersWithPackingSite)).build()

      running(application) {
        val request = FakeRequest(GET, packingSiteDetailsRemoveRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemovePackagingSiteConfirmView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            AddressFormattingHelper.addressFormatting(addressOfPackingSite, Some(packingSiteTradingName)),
            indexOfPackingSiteToBeRemoved
          )(using request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

      val application =
        correctReturnAction(userAnswers = Some(userAnswersWithPackingSite))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packingSiteDetailsRemoveRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = correctReturnAction(userAnswers = Some(userAnswersWithPackingSite)).build()

      running(application) {
        val request =
          FakeRequest(POST, packingSiteDetailsRemoveRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemovePackagingSiteConfirmView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            AddressFormattingHelper.addressFormatting(addressOfPackingSite, Some(packingSiteTradingName)),
            indexOfPackingSiteToBeRemoved
          )(using request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, packingSiteDetailsRemoveRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, packingSiteDetailsRemoveRoute)
    testNoUserAnswersError(packingSiteDetailsRemoveRoute)

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

      val application =
        correctReturnAction(userAnswers = Some(userAnswersWithPackingSite))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packingSiteDetailsRemoveRoute)
              .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events
            .collectFirst { case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on removePackagingSiteConfirm"
            }
            .getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
