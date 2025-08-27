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
import config.FrontendAppConfig
import models.changeActivity.AmountProduced
import models.{LitresInBands, NormalMode, UserAnswers}
import pages.changeActivity._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.changeActivity.ChangeActivitySentView
import views.summary.changeActivity.ChangeActivitySummary

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}

class ChangeActivitySentControllerSpec extends SpecBase {

  lazy val updateSentRoute: String = routes.ChangeActivitySentController.onPageLoad().url
  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)
  val completedUserAnswers: UserAnswers = emptyUserAnswersForChangeActivity
    .copy(submitted = true, warehouseList = twoWarehouses)
    .set(AmountProducedPage, AmountProduced.None).success.value
    .set(ContractPackingPage, false).success.value
    .set(ImportsPage, true).success.value
    .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
    .set(SecondaryWarehouseDetailsPage, false).success.value

  val sections: Seq[(String, SummaryList)] = ChangeActivitySummary.summaryListsAndHeadings(completedUserAnswers, isCheckAnswers = false)
  val alias: String = aSubscription.orgName
  val config: FrontendAppConfig = frontendAppConfig

  "UpdateSent Controller" - {

    "must return OK and the correct view for a GET" in {

      val testTime = Instant.now()
      val application = applicationBuilder(userAnswers = Some(completedUserAnswers.copy(submittedOn = Some(testTime)))).build()

      running(application) {
        val request = FakeRequest(GET, updateSentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeActivitySentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(alias, formattedDate, LocalDateTime.ofInstant(testTime, ZoneId.of("Europe/London"))
          .format(DateTimeFormatter.ofPattern("h:mma")), sections)(request, messages(application), config).toString
      }
    }

    "must redirect to AmountProduced if submitted is false" in {
      val application = applicationBuilder(userAnswers = Some(completedUserAnswers.copy(submitted = false))).build()

      running(application) {
        val request = FakeRequest(GET, updateSentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AmountProducedController.onPageLoad(NormalMode).url
      }
    }

    "must redirect if there is no submission date" in {
      val application = applicationBuilder(userAnswers = Some(completedUserAnswers.copy(submittedOn = None))).build()

      running(application) {
        val request = FakeRequest(GET, updateSentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
