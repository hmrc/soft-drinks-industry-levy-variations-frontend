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
import models.correctReturn.AddASmallProducer
import models.{LitresInBands, SmallProducer}
import pages.correctReturn._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

class CorrectReturnCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val litres = LitresInBands(2000, 4000)
      val userAnswers = emptyUserAnswersForCorrectReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, litres).success.value
        .set(ExemptionsForSmallProducersPage, true).success.value
        .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, litres).success.value
        .set(BroughtIntoUkFromSmallProducersPage, true).success.value
        .set(HowManyBroughtIntoUkFromSmallProducersPage, litres).success.value
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, litres).success.value
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, litres).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, CorrectReturnCYAController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnCYAView]
        val orgName = " Super Lemonade Plc"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, CorrectReturnBaseCYASummary.summaryListAndHeadings(userAnswers, aSubscription),
          routes.CorrectReturnCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, CorrectReturnCYAController.onPageLoad.url, false)
    testNoUserAnswersError(CorrectReturnCYAController.onPageLoad.url, false)
  }
}
