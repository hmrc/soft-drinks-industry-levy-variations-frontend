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
import forms.correctReturn.RemoveWarehouseDetailsFormProvider
import models.SelectChange.CorrectReturn
import models.backend.{Site, UkAddress}
import models.{NormalMode, UserAnswers}
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.correctReturn.RemoveWarehouseDetailsView

import scala.concurrent.Future

class RemoveWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RemoveWarehouseDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  val indexOfWarehouseToBeRemoved: String = "foobar"
  lazy val removeWarehouseDetailsRoute: String = routes.RemoveWarehouseDetailsController.onPageLoad(NormalMode, indexOfWarehouseToBeRemoved).url
  val addressOfWarehouse: UkAddress = UkAddress(List("foo"),"bar", None)
  val warehouseTradingName: String = "a name for a warehouse here"
  val userAnswersWithWarehouse: UserAnswers = emptyUserAnswersForCorrectReturn
    .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(addressOfWarehouse, Some(warehouseTradingName))))

  "Correct Return RemoveWarehouseDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithWarehouse)).build()

      running(application) {
        val request = FakeRequest(GET, removeWarehouseDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveWarehouseDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, AddressFormattingHelper.addressFormatting(addressOfWarehouse, Some(warehouseTradingName)),
            indexOfWarehouseToBeRemoved)(request, messages(application)).toString
      }
    }

    "must redirect to warehouse details when warehouse index does not exist on warehouse list on GET" in {
      val userAnswers = Some(userAnswersWithWarehouse.copy(warehouseList = twoWarehouses))
      val application = applicationBuilder(userAnswers).overrides(
        bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute))
      )
        .build()
      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, removeWarehouseDetailsRoute)

          val result = route(application, request).value

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "WARN"
              event.getMessage mustEqual s"Warehouse index $indexOfWarehouseToBeRemoved doesn't exist ${userAnswers.value.id} warehouse list length:2"
          }.getOrElse(fail("No logging captured"))
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.correctReturn.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithWarehouse))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithWarehouse)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, AddressFormattingHelper.addressFormatting(addressOfWarehouse, Some(warehouseTradingName)), indexOfWarehouseToBeRemoved)(request, messages(application)).toString
      }
    }

    "must redirect to warehouse details when warehouse index does not exist on warehouse list on POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, removeWarehouseDetailsRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "WARN"
              event.getMessage mustEqual s"Warehouse index $indexOfWarehouseToBeRemoved doesn't exist ${emptyUserAnswersForCorrectReturn.id} warehouse list length:0"
          }.getOrElse(fail("No logging captured"))
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.correctReturn.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url
        }
      }
    }

    testInvalidJourneyType(CorrectReturn, removeWarehouseDetailsRoute)
    testNoUserAnswersError(removeWarehouseDetailsRoute)

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithWarehouse))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, removeWarehouseDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on removeWarehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
