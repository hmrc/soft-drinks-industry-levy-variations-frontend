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

package controllers.changeActivity

import base.SpecBase
import errors.SessionDatabaseInsertError
import forms.changeActivity.SecondaryWarehouseDetailsFormProvider
import models.SelectChange.ChangeActivity
import models.{NormalMode, UserAnswers}
import models.backend.{Site, UkAddress}
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.changeActivity.SecondaryWarehouseDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.SecondaryWarehouseDetailsView
import views.summary.changeActivity.SecondaryWarehouseDetailsSummary

import scala.concurrent.Future

class SecondaryWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val warehouseDetailsRoute: String = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url

  "SecondaryWarehouseDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, None)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForChangeActivity.set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), None)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with a single warehouse in the list" in {

      val summaryList = Some(SummaryListViewModel(
        rows = SecondaryWarehouseDetailsSummary.summaryRows(Map("1" -> warehouse))
      ))

      val userAnswers = warehouseAddedToUserAnswersForChangeActivity.set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual  view(form.fill(true), summaryList)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with multiple warehouses in the list" in {
      val warehouses = Map("1" -> warehouse, "2" -> Site(UkAddress(List("34 Rhes Priordy"),"WR53 7CX"), Some("DEF Ltd")))
      val summaryList = Some(SummaryListViewModel(rows = SecondaryWarehouseDetailsSummary.summaryRows(warehouses)))

      val userAnswers = UserAnswers(userAnswersId, ChangeActivity, warehouseList = warehouses, contactAddress = contactAddress)
        .set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), summaryList)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val applicationSetup =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity))
          .overrides(
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(applicationSetup) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(applicationSetup, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ChangeActivityCYAController.onPageLoad.url
      }
    }

    "must redirect to the next page when valid data (warehouses in the list) is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(warehouseAddedToUserAnswersForChangeActivity))
          .overrides(
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ChangeActivityCYAController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, None)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(ChangeActivity, warehouseDetailsRoute)
    testNoUserAnswersError(warehouseDetailsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(ChangeActivity))).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute
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
        applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity))
          .overrides(
            bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, warehouseDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on secondaryWarehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
