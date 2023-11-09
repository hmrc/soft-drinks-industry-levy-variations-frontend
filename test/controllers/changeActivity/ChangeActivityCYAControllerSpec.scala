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
import generators.ChangeActivityCYAGenerators._
import models.{DataHelper, LitresInBands}
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced.Large
import orchestrators.ChangeActivityOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.changeActivity._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.ChangeActivityCYAView
import views.summary.changeActivity.ChangeActivitySummary

class ChangeActivityCYAControllerSpec extends SpecBase with SummaryListFluency with DataHelper {

  def onwardRoute = Call("GET", "/foo")

  "Check Your Answers Controller" - {

    testCaseOptions.foreach { case userAnswerOptions =>
      val key = getKeyStringFromUserAnswerOptions(userAnswerOptions)
      val userAnswers = getUserAnswersFromUserAnswerOptions(userAnswerOptions)

      s"must return OK and the correct view for a GET for user answers $key" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, ChangeActivityCYAController.onPageLoad.url)

          val result = route(application, request).value
          val view = application.injector.instanceOf[ChangeActivityCYAView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            aSubscription.orgName,
            ChangeActivitySummary.summaryListsAndHeadings(userAnswers, isCheckAnswers = true),
            routes.ChangeActivityCYAController.onSubmit
          )(request, messages(application)).toString
        }
      }
    }

    "must redirect to return sent page on submit" in {
      val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, Large).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100L, 100L)).success.value
        .set(ContractPackingPage, true).success.value
        .set(HowManyContractPackingPage, LitresInBands(100 , 100)).success.value
        .set(ImportsPage, true).success.value
        .set(HowManyImportsPage, LitresInBands(100, 100)).success.value
        .copy(packagingSiteList = Map.empty,
              warehouseList = Map.empty)

      val mockOrchestrator: ChangeActivityOrchestrator = mock[ChangeActivityOrchestrator]

      when(mockOrchestrator.submitVariation(any(), any())(any(), any())) thenReturn createSuccessVariationResult((): Unit)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ChangeActivityOrchestrator].toInstance(mockOrchestrator)
        )
        .build()

      running(application) {
        val request =
        FakeRequest(POST, ChangeActivityCYAController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/change-activity/variation-done"
      }
    }

    testInvalidJourneyType(ChangeActivity, ChangeActivityCYAController.onPageLoad.url, false)
    testNoUserAnswersError(ChangeActivityCYAController.onPageLoad.url, false)
  }
}
