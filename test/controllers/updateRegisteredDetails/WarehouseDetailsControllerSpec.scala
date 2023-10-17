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
import base.SpecBase.userAnswerTwoWarehousesUpdateRegisteredDetails
import errors.SessionDatabaseInsertError
import forms.updateRegisteredDetails.WarehouseDetailsFormProvider
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails
import models.{CheckMode, Mode, NormalMode}
import navigation._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.{ChangeRegisteredDetailsPage, WarehouseDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressLookupService, SessionService, WarehouseDetails}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.WarehouseDetailsView
import views.summary.updateRegisteredDetails.WarehouseDetailsSummary

import scala.concurrent.Future

class WarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  def doc(result: String): Document = Jsoup.parse(result)
  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new WarehouseDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val warehouseDetailsRoute: String = routes.WarehouseDetailsController.onPageLoad(NormalMode).url
  lazy val warehouseDetailsCheckRoute: String = routes.WarehouseDetailsController.onPageLoad(CheckMode).url

  def warehouseDetailsRouteForMode(mode: Mode): String = if (mode == CheckMode) warehouseDetailsCheckRoute else warehouseDetailsRoute

  "WarehouseDetails Controller" - {

    List(NormalMode, CheckMode).foreach(mode => {
      s"must return OK and the correct view for a GET in $mode" in {

        val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehousesUpdateRegisteredDetails)).build()

        running(application) {
          val request = FakeRequest(GET, warehouseDetailsRouteForMode(mode))

          val result = route(application, request).value

          status(result) mustEqual OK
          val summaryActions = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
          summaryActions.size() mustEqual 2
          summaryActions.first.text() must include("Remove")
          summaryActions.last.text() must include("Remove")

          val removeLink = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
            .tagName("ul").tagName("li").last().getElementsByClass("govuk-link").last()
          val changeLink = if (mode == CheckMode) {
            "/soft-drinks-industry-levy-variations-frontend/change-registered-details/change-warehouse-details/remove/2"
          } else {
            "/soft-drinks-industry-levy-variations-frontend/change-registered-details/warehouse-details/remove/2"
          }
          removeLink.attr("href") mustEqual changeLink
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered in $mode" in {

        val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, warehouseDetailsRouteForMode(mode))

          val view = application.injector.instanceOf[WarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), mode, None)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered with a warehouse in the list in $mode" in {

        val summaryList = Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(Map("1" -> warehouse), mode)
        ))

        val userAnswers = warehouseAddedToUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, warehouseDetailsRouteForMode(mode))

          val view = application.injector.instanceOf[WarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), mode, summaryList)(request, messages(application)).toString
        }
      }
    })

    "must redirect to the next page when valid data is submitted (true)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any())(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, ChangeRegisteredDetails.values).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must redirect to the next page when valid data is submitted (false)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, ChangeRegisteredDetails.values).success.value
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.updateRegisteredDetails.routes.UpdateContactDetailsController.onPageLoad(NormalMode).url

        verify(mockAddressLookupService, times(0)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, warehouseDetailsRoute)
    testNoUserAnswersError(warehouseDetailsRoute)

    List(NormalMode, CheckMode).foreach(mode => {
      s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

        running(application) {
          val request =
            FakeRequest(POST, warehouseDetailsRouteForMode(mode))
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[WarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, mode, None)(request, messages(application)).toString
        }
      }

      s"must fail if the setting of userAnswers fails in $mode" in {

        val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(UpdateRegisteredDetails))).build()

        running(application) {
          val request = FakeRequest(POST, warehouseDetailsRouteForMode(mode))
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          val page = Jsoup.parse(contentAsString(result))
          page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }

      s"should log an error message when internal server error is returned when user answers are not set in session repository in $mode" in {
        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
            .overrides(
              bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService)
            ).build()

        running(application) {
          withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
            val request =
              FakeRequest(POST, warehouseDetailsRouteForMode(mode))
                .withFormUrlEncodedBody(("value", "true"))

            await(route(application, request).value)
            events.toString() must include("Failed to set value in session repository while attempting set on warehouseDetails")
          }
        }
      }

      s"should log an error when no answers are returned from the ChangeRegisteredDetailsPage in $mode" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
            )
            .build()

        running(application) {
          withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
            val request =
              FakeRequest(POST, warehouseDetailsRoute)
                .withFormUrlEncodedBody(("value", "false"))

            await(route(application, request).value)

            events.toString() must include("Failed to obtain which registered details to change from user answers while on warehouseDetails")
          }
        }
      }
    })

  }
}
