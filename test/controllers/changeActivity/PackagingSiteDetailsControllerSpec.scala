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
import forms.changeActivity.PackagingSiteDetailsFormProvider
import models.{CheckMode, NormalMode}
import models.SelectChange.ChangeActivity
import navigation._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.changeActivity.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.{AddressLookupService, PackingDetails, SessionService}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.changeActivity.PackagingSiteDetailsSummary
import views.html.changeActivity.PackagingSiteDetailsView

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar  with SummaryListFluency{

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new PackagingSiteDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val packagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  lazy val packagingSiteDetailsCheckRoute: String = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url

  "PackagingSiteDetails Controller" - {

    "must return OK and the correct view for a GET in NormalMode" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

      val summary = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(Map.empty, NormalMode)
      )

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, summary)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered in NormalMode" in {

      val summary = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(Map.empty, NormalMode)
      )

      val userAnswers = emptyUserAnswersForChangeActivity

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val view = application.injector.instanceOf[PackagingSiteDetailsView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, summary)(request, messages(application)).toString
      }
    }

    List(NormalMode, CheckMode).foreach { mode =>
      val path = if (mode == NormalMode) packagingSiteDetailsRoute else packagingSiteDetailsCheckRoute

      s"must redirect to the next page when valid data is submitted in $mode" in {

        val mockSessionService = mock[SessionService]
        val mockAddressLookupService = mock[AddressLookupService]
        val onwardUrlForALF = "foobarwizz"

        when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))
        when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(onwardUrlForALF))


        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity))
            .overrides(
              bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService),
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, path)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardUrlForALF

          verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
            ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        }
      }

      s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

        val summary = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(Map.empty, mode)
        )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

        running(application) {
          val request =
            FakeRequest(POST, path)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PackagingSiteDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, mode, summary)(request, messages(application)).toString
        }
      }
    }

    "must redirect to PackAtBusinessAddress when packaging site list empty and in CheckMode" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsCheckRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
      }
    }

    testInvalidJourneyType(ChangeActivity, packagingSiteDetailsRoute)
    testNoUserAnswersError(packagingSiteDetailsRoute)

  }
}
