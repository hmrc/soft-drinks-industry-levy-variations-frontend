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
import controllers.updateRegisteredDetails.routes._
import models.SelectChange.UpdateRegisteredDetails
import models.backend.UkAddress
import models.updateRegisteredDetails.ContactDetails
import orchestrators.UpdateRegisteredDetailsOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.UpdateRegisteredDetailsCYAView
import views.summary.updateRegisteredDetails.{BusinessAddressSummary, UKSitesSummary, UpdateContactDetailsSummary}

class UpdateRegisteredDetailsCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val contactDetails = ContactDetails("foo", "bar", "wizz", "bang")
      val businessAddress: UkAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")
      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails
        .copy(contactAddress = businessAddress, packagingSiteList = Map("1" -> packingSite), warehouseList = Map("1" -> warehouse))
        .set(UpdateContactDetailsPage,contactDetails).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, UpdateRegisteredDetailsCYAController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateRegisteredDetailsCYAView]
        val list: Seq[(String, SummaryList)] = Seq(
          UKSitesSummary.getHeadingAndSummary(userAnswers, true),
          UpdateContactDetailsSummary.rows(userAnswers),
          BusinessAddressSummary.rows(userAnswers)
        ).flatten
        val orgName = " " + aSubscription.orgName

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, list, routes.UpdateRegisteredDetailsCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must redirect to update done when data is correct for POST" in {
      val mockOrchestrator: UpdateRegisteredDetailsOrchestrator = mock[UpdateRegisteredDetailsOrchestrator]

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails),
        subscription = Some(aSubscription))
        .overrides(
          bind[UpdateRegisteredDetailsOrchestrator].toInstance(mockOrchestrator)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, UpdateRegisteredDetailsCYAController.onPageLoad.url).withFormUrlEncodedBody()
        when(mockOrchestrator.submitVariation(any(), any())(any(), any())) thenReturn createSuccessVariationResult((): Unit)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual UpdateDoneController.onPageLoad.url
      }
    }
    testInvalidJourneyType(UpdateRegisteredDetails, UpdateRegisteredDetailsCYAController.onPageLoad.url)
    testRedirectToPostSubmissionIfRequired(UpdateRegisteredDetails, UpdateRegisteredDetailsCYAController.onPageLoad.url)
    testNoUserAnswersError(UpdateRegisteredDetailsCYAController.onPageLoad.url)
  }
}
