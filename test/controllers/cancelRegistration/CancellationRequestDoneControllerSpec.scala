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
import config.FrontendAppConfig
import models.{NormalMode, ReturnPeriod}
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.cancelRegistration.{CancelRegistrationDateSummary, ReasonSummary}
import views.html.cancelRegistration.CancellationRequestDoneView

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

class CancellationRequestDoneControllerSpec extends SpecBase with SummaryListFluency {

  lazy val cancellationRequestDoneRoute: String = routes.CancellationRequestDoneController.onPageLoad().url
  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)

  val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
  val nextReturnPeriod = ReturnPeriod(getSentDateTime.toLocalDate)
  val returnPeriodStart = nextReturnPeriod.start.format(returnPeriodFormat)
  val returnPeriodEnd = nextReturnPeriod.end.format(returnPeriodFormat)

  val deadlineStartFormat = DateTimeFormatter.ofPattern("d MMMM")
  val deadlineStart = nextReturnPeriod.end.plusDays(1).format(deadlineStartFormat)

  val deadlineEndFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val deadlineEnd = nextReturnPeriod.deadline.format(deadlineEndFormat)

  val orgName: String = aSubscription.orgName
  val config: FrontendAppConfig = frontendAppConfig

  val userAnswers = emptyUserAnswersForCancelRegistration.copy(submitted = true)
    .set(ReasonPage, "No longer sell drinks").success.value
    .set(CancelRegistrationDatePage, LocalDate.now()).success.value

  "CancellationRequestDone Controller" - {

    "must return OK and the correct view for a GET" in {
      val testTime = Instant.now()
      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(submittedOn = Some(testTime)))).build()

      running(application) {
        val request = FakeRequest(GET, cancellationRequestDoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancellationRequestDoneView]

        val cancelRegistrationSummary: (String, SummaryList) = ("", SummaryListViewModel(
          rows = Seq(
            ReasonSummary.row(userAnswers, isCheckAnswers = false),
            CancelRegistrationDateSummary.row(userAnswers, isCheckAnswers = false)
          ))
        )

        val list = Seq(cancelRegistrationSummary)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formattedDate, LocalDateTime.ofInstant(testTime, ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("h:mma")), returnPeriodStart, returnPeriodEnd, deadlineStart, deadlineEnd, orgName, list)(request, messages(application), config).toString
      }
    }

    "must redirect to Reason if submitted is false" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(submitted = false))).build()

      running(application) {
        val request = FakeRequest(GET, cancellationRequestDoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReasonController.onPageLoad(NormalMode).url
      }
    }

    "must redirect if there is no submission date" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(submittedOn = None))).build()

      running(application) {
        val request = FakeRequest(GET, cancellationRequestDoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
