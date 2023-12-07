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

package controllers.cancelRegistration

import base.SpecBase
import controllers.cancelRegistration.routes._
import models.SelectChange.CancelRegistration
import orchestrators.CancelRegistrationOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.cancelRegistration.{CancelRegistrationDateSummary, ReasonSummary}
import views.html.cancelRegistration.CancelRegistrationCYAView

import java.time.LocalDate
import scala.concurrent.Future

class CancelRegistrationCYAControllerSpec extends SpecBase with SummaryListFluency {

  val cyaRoute: String = CancelRegistrationCYAController.onPageLoad.url

  "Cancel Registration Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(ReasonPage, "No longer sell drinks").success.value
        .set(CancelRegistrationDatePage, LocalDate.now()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cyaRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelRegistrationCYAView]

        val cancelRegistrationSummary : (String, SummaryList) = ("",SummaryListViewModel(
          rows = Seq(ReasonSummary.row(userAnswers), CancelRegistrationDateSummary.row(userAnswers)))
        )

        val list = Seq(cancelRegistrationSummary)

        val orgName = " Super Lemonade Plc"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, list, routes.CancelRegistrationCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must return Redirect to reason page when no user answers are present for the reason page" - {
      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(CancelRegistrationDatePage, LocalDate.now()).success.value
      def application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      "on a GET" in {
        running(application) {
          val request = FakeRequest(GET, cyaRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/cancel-registration/reason"
        }
      }

      "on a POST" in {
        running(application) {
          val request = FakeRequest(POST, cyaRoute).withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/cancel-registration/reason"
        }
      }
    }

    "must return Redirect to cancellation date page when no user answers are present for the cancellation date page" - {
      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(ReasonPage, "No longer sell drinks").success.value

      def application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      "on a GET" in {
        running(application) {
          val request = FakeRequest(GET, cyaRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/cancel-registration/date"
        }
      }

      "on a POST" in {
        running(application) {
          val request = FakeRequest(GET, cyaRoute).withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/cancel-registration/date"
        }
      }
    }

    "must return Redirect to cancellation reason page when no user answers are present" - {

      val userAnswers = emptyUserAnswersForCancelRegistration

      def application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      "on a GET" in {
        running(application) {
          val request = FakeRequest(GET, cyaRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/select-change"
        }
      }

      "on a POST" in {
        running(application) {
          val request = FakeRequest(POST, cyaRoute).withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/select-change"
        }
      }
    }


    "must redirect to cancel registration done when data is correct for POST" in {
      val mockOrchestrator: CancelRegistrationOrchestrator = mock[CancelRegistrationOrchestrator]
      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(ReasonPage, "No longer sell drinks").success.value
        .set(CancelRegistrationDatePage, LocalDate.now()).success.value
      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        subscription = Some(aSubscription))
        .overrides(
          bind[CancelRegistrationOrchestrator].toInstance(mockOrchestrator)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, CancelRegistrationCYAController.onPageLoad.url).withFormUrlEncodedBody()
        when(mockOrchestrator.submitVariation(any(), any())(any(), any())) thenReturn createSuccessVariationResult((): Unit)
        when(mockOrchestrator.submitUserAnswers(any())(any(), any())) thenReturn Future.successful(true)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CancellationRequestDoneController.onPageLoad.url
      }
    }

    testInvalidJourneyType(CancelRegistration, CancelRegistrationCYAController.onPageLoad.url, false)
    testNoUserAnswersError(CancelRegistrationCYAController.onPageLoad.url, false)
  }
}
