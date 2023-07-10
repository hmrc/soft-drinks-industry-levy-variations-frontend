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
import forms.updateRegisteredDetails.PackingSiteDetailsRemoveFormProvider
import models.backend.{Site, UkAddress}
import models.{NormalMode, SelectChange, UserAnswers}
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.updateRegisteredDetails.PackingSiteDetailsRemoveView

import scala.concurrent.Future

class PackingSiteDetailsRemoveControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PackingSiteDetailsRemoveFormProvider()
  val form = formProvider()

  val indexOfPackingSiteToBeRemoved: String = "foobar"
  lazy val packingSiteDetailsRemoveRoute = routes.PackingSiteDetailsRemoveController.onPageLoad(NormalMode, indexOfPackingSiteToBeRemoved).url
  val addressOfPackingSite: UkAddress = UkAddress(List("foo"),"bar", None)
  val packingSiteTradingName: String = "a name for a packing site here"
  val userAnswersWithPackingSite: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
    .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(addressOfPackingSite, None, Some(packingSiteTradingName), None)))

  "PackingSiteDetailsRemove Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPackingSite)).build()

      running(application) {
        val request = FakeRequest(GET, packingSiteDetailsRemoveRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PackingSiteDetailsRemoveView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, AddressFormattingHelper.addressFormatting(addressOfPackingSite,
            Some(packingSiteTradingName)), indexOfPackingSiteToBeRemoved)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPackingSite))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
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

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPackingSite)).build()

      running(application) {
        val request =
          FakeRequest(POST, packingSiteDetailsRemoveRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PackingSiteDetailsRemoveView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, AddressFormattingHelper.addressFormatting(addressOfPackingSite,
            Some(packingSiteTradingName)),
            indexOfPackingSiteToBeRemoved)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(SelectChange.UpdateRegisteredDetails, packingSiteDetailsRemoveRoute)
    testNoUserAnswersError(packingSiteDetailsRemoveRoute)

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPackingSite))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packingSiteDetailsRemoveRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packingSiteDetailsRemove"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
