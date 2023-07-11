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

package controllers.correctReturn

import base.SpecBase
import controllers.correctReturn.routes._
import models.SelectChange.CorrectReturn
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCYAView

class CorrectReturnCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, CorrectReturnCYAController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnCYAView]
        val list: Seq[(String, SummaryList)] = Seq.empty

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, CorrectReturnCYAController.onPageLoad.url, false)
    testNoUserAnswersError(CorrectReturnCYAController.onPageLoad.url, false)
  }
}
