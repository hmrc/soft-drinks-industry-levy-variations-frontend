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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.updateRegisteredDetails.UpdateDoneView
import models.SelectChange.UpdateRegisteredDetails
import views.summary.updateRegisteredDetails.UpdateContactDetailsSummary

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class UpdateDoneControllerSpec extends SpecBase {

  lazy val updateDoneRoute = routes.UpdateDoneController.onPageLoad().url
  val getSentDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate = getSentDateTime.format(dateFormatter)
  val formattedTime = getSentDateTime.format(timeFormatter)
  val summaryList = Seq(UpdateContactDetailsSummary.rows(emptyUserAnswersForUpdateRegisteredDetails)).flatten
  val orgName = aSubscription.orgName

  "UpdateDone Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(GET, updateDoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateDoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryList, formattedDate, formattedTime, orgName)(request, messages(application)).toString
      }
    }
  }
}
