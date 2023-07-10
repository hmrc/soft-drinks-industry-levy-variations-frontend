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
import controllers.changeActivity.routes._
import models.SelectChange
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.ChangeActivityCYAView

class ChangeActivityCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

      running(application) {
        val request = FakeRequest(GET, ChangeActivityCYAController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeActivityCYAView]
        val list: Seq[(String, SummaryList)] = Seq.empty

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(SelectChange.ChangeActivity, ChangeActivityCYAController.onPageLoad.url, false)
    testNoUserAnswersError(ChangeActivityCYAController.onPageLoad.url, false)
  }
}
