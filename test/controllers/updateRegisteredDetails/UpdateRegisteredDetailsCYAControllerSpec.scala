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
import controllers.routes._
import controllers.updateRegisteredDetails.routes._
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.UpdateContactDetails
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.UpdateRegisteredDetailsCYAView
import views.summary.updateRegisteredDetails.UpdateContactDetailsSummary

class UpdateRegisteredDetailsCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val contactDetails = UpdateContactDetails("foo", "bar", "wizz", "bang")
      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(UpdateContactDetailsPage,contactDetails).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, UpdateRegisteredDetailsCYAController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateRegisteredDetailsCYAView]
        val list: Seq[(String, SummaryList)] = Seq(UpdateContactDetailsSummary.rows(userAnswers).get)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, routes.UpdateRegisteredDetailsCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must Redirect to next page when data is correct for POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(POST, UpdateRegisteredDetailsCYAController.onPageLoad.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual IndexController.onPageLoad.url
      }
    }
    testInvalidJourneyType(UpdateRegisteredDetails, UpdateRegisteredDetailsCYAController.onPageLoad.url)
    testNoUserAnswersError(UpdateRegisteredDetailsCYAController.onPageLoad.url)
  }
}
