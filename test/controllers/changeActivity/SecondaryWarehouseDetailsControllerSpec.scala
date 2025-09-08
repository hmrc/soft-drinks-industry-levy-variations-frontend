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
import forms.changeActivity.SecondaryWarehouseDetailsFormProvider
import models.SelectChange.ChangeActivity
import models.backend.{Site, UkAddress}
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.SecondaryWarehouseDetailsView
import views.summary.changeActivity.SecondaryWarehouseDetailsSummary

import scala.concurrent.Future

class SecondaryWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form: Form[Boolean] = formProvider(false)

  lazy val warehouseDetailsRoute: String = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url
  lazy val warehouseDetailsCheckRoute: String = routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url

  "SecondaryWarehouseDetails Controller" - {

    List(NormalMode, CheckMode).foreach { mode =>
      val path = if (mode == NormalMode) warehouseDetailsRoute else warehouseDetailsCheckRoute

      s"must return OK and the correct view for a GET in $mode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, None, mode)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered in $mode" in {

        val userAnswers = emptyUserAnswersForChangeActivity

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, None, mode)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered with a single warehouse in the list in $mode" in {

        val summaryList = Some(SummaryListViewModel(
          rows = SecondaryWarehouseDetailsSummary.summaryRows(Map("1" -> warehouse), mode)
        ))

        val userAnswers = warehouseAddedToUserAnswersForChangeActivity

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual  view(form, summaryList, mode)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered with multiple warehouses in the list in $mode" in {
        val warehouses = Map("1" -> warehouse, "2" -> Site(UkAddress(List("34 Rhes Priordy"),"WR53 7CX"), Some("DEF Ltd")))
        val summaryList = Some(SummaryListViewModel(rows = SecondaryWarehouseDetailsSummary.summaryRows(warehouses, mode)))

        val userAnswers = UserAnswers(userAnswersId, ChangeActivity, warehouseList = warehouses, contactAddress = contactAddress)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, summaryList, mode)(request, messages(application)).toString
        }
      }

      s"must redirect to the next page when valid data is submitted in $mode" in {

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
            FakeRequest(POST, path)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(applicationSetup, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ChangeActivityCYAController.onPageLoad().url
        }
      }

      s"must redirect to the next page when valid data (warehouses in the list) is submitted in $mode" in {

        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

        val application =
          applicationBuilder(userAnswers = Some(warehouseAddedToUserAnswersForChangeActivity))
            .overrides(
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, path).withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ChangeActivityCYAController.onPageLoad().url
        }
      }

      s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

        running(application) {
          val request = FakeRequest(POST, path).withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, None, mode)(request, messages(application)).toString
        }
      }
    }

    testInvalidJourneyType(ChangeActivity, warehouseDetailsRoute)
    testRedirectToPostSubmissionIfRequired(ChangeActivity, warehouseDetailsRoute)
    testNoUserAnswersError(warehouseDetailsRoute)
  }
}
