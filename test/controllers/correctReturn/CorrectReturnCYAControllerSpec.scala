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
import models.{Amounts, LitresInBands, SmallProducer}
import orchestrators.CorrectReturnOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.correctReturn._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReturnService
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

class CorrectReturnCYAControllerSpec extends SpecBase with SummaryListFluency {

  val mockReturnService: ReturnService = mock[ReturnService]
  val mockOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val litres = LitresInBands(100, 200)
      val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
        .set(PackagedAsContractPackerPage, false).success.value
        .set(ExemptionsForSmallProducersPage, true).success.value
        .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, litres).success.value
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, litres).success.value


      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnCYAView]
        val orgName = " Super Lemonade Plc"
        val section = CorrectReturnBaseCYASummary.summaryListAndHeadings(userAnswers, aSubscription, amounts1)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, amounts1, section,
          controllers.correctReturn.routes.CorrectReturnCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must return OK and contain amount owed header when balance and return amount is positive (i.e. there is an amount to pay)" in {
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
      val litres = LitresInBands(0, 0)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(superCola, sparkyJuice))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 10000)).success.value
        .set(ExemptionsForSmallProducersPage, true).success.value
        .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, litres).success.value
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, litres).success.value

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("cya-inset-sub-header").text() mustBe "You need to pay £4,500.00"
        page.getElementsByTag("h2").text must include ("Summary")
        page.getElementsByTag("dt").text() must include("Total this quarter")
        page.getElementsByClass("total-for-quarter").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include("Balance brought forward")
        page.getElementsByClass("balance-brought-forward").text() mustBe "£300.00"
        page.getElementsByTag("dt").text() must include("Total")
        page.getElementsByClass("total").text() must include("£4,500.00")
        page.getElementsByClass("total").text() mustNot include("−£4,500.00")
      }
    }

    "must return OK and contain credit amount header when total is negative" in {
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
      val amounts1 = Amounts(originalReturnTotal = 0.00, newReturnTotal = -4200.00, balanceBroughtForward = -300.00,
        totalForQuarterLessForwardBalance = -3900.00, netAdjustedAmount = -3900.00)
      val litres = LitresInBands(0, 0)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(superCola, sparkyJuice))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 10000)).success.value
        .set(ExemptionsForSmallProducersPage, true).success.value
        .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, litres).success.value
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, litres).success.value

      val application = applicationBuilder(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("cya-inset-sub-header").text() mustBe "Your Soft Drinks Levy Account will be credited £3,900.00"
        page.getElementsByTag("h2").text must include ("Summary")
        page.getElementsByTag("dt").text() must include("Total this quarter")
        page.getElementsByClass("total-for-quarter").text() must include("−£4,200.00")
        page.getElementsByTag("dt").text() must include("Balance brought forward")
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include("Total")
        page.getElementsByClass("total").text() must include("−£3,900.00")
      }
    }

    testInvalidJourneyType(CorrectReturn, CorrectReturnCYAController.onPageLoad.url, hasPostMethod = false)
    testNoUserAnswersError(CorrectReturnCYAController.onPageLoad.url, hasPostMethod = false)
  }
}
