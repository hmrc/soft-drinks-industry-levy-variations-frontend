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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.cancelRegistration.CancellationRequestDoneView
import views.summary.updateRegisteredDetails.UpdateContactDetailsSummary

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class CancellationRequestDoneControllerSpec extends SpecBase {

  lazy val cancellationRequestDoneRoute: String = routes.CancellationRequestDoneController.onPageLoad().url
  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)
  val summaryList: Seq[(String, SummaryList)] = Seq(UpdateContactDetailsSummary.rows(emptyUserAnswersForUpdateRegisteredDetails)).flatten
  val orgName: String = aSubscription.orgName
  val config: FrontendAppConfig = frontendAppConfig

  "CancellationRequestDone Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).build()

      running(application) {
        val request = FakeRequest(GET, cancellationRequestDoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancellationRequestDoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryList, formattedDate, formattedTime, orgName)(request, messages(application), config).toString
      }
    }
  }
}
