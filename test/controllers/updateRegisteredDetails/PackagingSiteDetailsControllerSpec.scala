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
import forms.updateRegisteredDetails.PackagingSiteDetailsFormProvider
import models.{CheckMode, NormalMode}
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.{ChangeRegisteredDetailsPage, PackagingSiteDetailsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressLookupService, PackingDetails, SessionService, WarehouseDetails}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.PackagingSiteDetailsView
import views.summary.updateRegisteredDetails.PackagingSiteDetailsSummary
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{times, verify}
import play.api.data.Form
import repositories.SessionRepository

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar  with SummaryListFluency{

  def onwardRoute: Call = Call("GET", "/foo")
  val formProvider = new PackagingSiteDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val packagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  lazy val packagingSiteDetailsCheckModeRoute: String = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url

  "PackagingSiteDetails Controller within Update Registered Details" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      val summary = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(Map.empty)
      )

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, summary)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val summary = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(Map.empty)
      )

      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(PackagingSiteDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, summary)(request, messages(application)).toString
      }
    }

    "must redirect to the warehouse details page when valid false answer data is submitted in NormalMode" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.WarehouseDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the Check your answers page when false answer data is submitted in Check answers" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsCheckModeRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val summary = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(Map.empty)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, summary)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, packagingSiteDetailsRoute)
    testNoUserAnswersError(packagingSiteDetailsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(UpdateRegisteredDetails))).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute
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
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packagingSiteDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packagingSiteDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

    "must redirect to the next page when valid data is submitted (true)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any())(
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
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

  }
}
