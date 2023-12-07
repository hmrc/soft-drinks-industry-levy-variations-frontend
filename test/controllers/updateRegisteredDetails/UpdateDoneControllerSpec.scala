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
import config.FrontendAppConfig
import models.UserAnswers
import models.updateRegisteredDetails.ContactDetails
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.updateRegisteredDetails.UpdateDoneView
import views.summary.updateRegisteredDetails.{BusinessAddressSummary, UKSitesSummary, UpdateContactDetailsSummary}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}

class UpdateDoneControllerSpec extends SpecBase {

  lazy val updateDoneRoute: String = routes.UpdateDoneController.onPageLoad().url
  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)
  val userAnswers: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails.
    set(UpdateContactDetailsPage, ContactDetails("Full Name", "job position", "012345678901", "email@test.com")).success.value
  val summaryList: Seq[(String, SummaryList)] = Seq(
    UKSitesSummary.getHeadingAndSummary(userAnswers, isCheckAnswers = false),
    UpdateContactDetailsSummary.rows(userAnswers, isCheckAnswers = false),
    BusinessAddressSummary.rows(userAnswers, isCheckAnswers = false)
  ).flatten
  val orgName: String = aSubscription.orgName
  val config: FrontendAppConfig = frontendAppConfig

  "UpdateDone Controller" - {

    "must return OK and the correct view for a GET" in {

      val testTime = Instant.now()
      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(submittedOn = Some(testTime)))).build()

      running(application) {
        val request = FakeRequest(GET, updateDoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateDoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryList, formattedDate, LocalDateTime.ofInstant(testTime, ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("h:mma")), orgName)(request, messages(application), config).toString
      }
    }

    "must redirect if there is no submission date" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, updateDoneRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
}
