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
import connectors.SoftDrinksIndustryLevyConnector
import errors.UnexpectedResponseFromSDIL
import models.correctReturn.RepaymentMethod.BankAccount
import models.correctReturn.{AddASmallProducer, ChangedPage, RepaymentMethod}
import models.submission.Litreage
import models.{Amounts, CheckMode, LitresInBands, SdilReturn, SmallProducer, UserAnswers}
import navigation.{FakeNavigatorForCorrectReturn, NavigatorForCorrectReturn}
import orchestrators.CorrectReturnOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.correctReturn._
import play.api.inject
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

class CorrectReturnCheckChangesCYAControllerSpec extends SpecBase with SummaryListFluency {

  val mockCorrectReturnOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }

  val litresInBands = LitresInBands(2000, 4000)
  val filledUserAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
    .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
      smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000))))
    .set(OperatePackagingSiteOwnBrandsPage, true).success.value
    .set(HowManyOperatePackagingSiteOwnBrandsPage, litresInBands).success.value
    .set(PackagedAsContractPackerPage, true).success.value
    .set(HowManyPackagedAsContractPackerPage, litresInBands).success.value
    .set(ExemptionsForSmallProducersPage, true).success.value
    .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litresInBands)).success.value
    .set(BroughtIntoUKPage, true).success.value
    .set(HowManyBroughtIntoUKPage, litresInBands).success.value
    .set(BroughtIntoUkFromSmallProducersPage, true).success.value
    .set(HowManyBroughtIntoUkFromSmallProducersPage, litresInBands).success.value
    .set(ClaimCreditsForExportsPage, true).success.value
    .set(HowManyClaimCreditsForExportsPage, litresInBands).success.value
    .set(ClaimCreditsForLostDamagedPage, true).success.value
    .set(HowManyCreditsForLostDamagedPage, litresInBands).success.value

  def onwardRoute: Call = Call("GET", "/foo")

  "Check Changes Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "foo").success.value
        .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

      val changedPages = List(
        ChangedPage(OperatePackagingSiteOwnBrandsPage, answerChanged = true),
        ChangedPage(HowManyOperatePackagingSiteOwnBrandsPage, answerChanged = true),
        ChangedPage(PackagedAsContractPackerPage, answerChanged = true),
        ChangedPage(HowManyPackagedAsContractPackerPage, answerChanged = true),
        ChangedPage(BroughtIntoUKPage, answerChanged = true),
        ChangedPage(HowManyBroughtIntoUKPage, answerChanged = true),
        ChangedPage(BroughtIntoUkFromSmallProducersPage, answerChanged = true),
        ChangedPage(HowManyBroughtIntoUkFromSmallProducersPage, answerChanged = true),
        ChangedPage(ClaimCreditsForExportsPage, answerChanged = true),
        ChangedPage(HowManyClaimCreditsForExportsPage, answerChanged = true),
        ChangedPage(ClaimCreditsForLostDamagedPage, answerChanged = true),
        ChangedPage(HowManyCreditsForLostDamagedPage, answerChanged = true),
        ChangedPage(ExemptionsForSmallProducersPage, answerChanged = true))

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(),any())) thenReturn createSuccessVariationResult(amounts)
        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnCheckChangesCYAView]
        val orgName = " Super Lemonade Plc"
        val section = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(userAnswers, aSubscription, changedPages,
          isCheckAnswers = true, amounts)
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, section,
          controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must redirect to CYA when that page has not been submitted" in {
      val amounts1 = Amounts(40200.00, 4200.00, -300.00, 4500.00, -35700.00)
      val userAnswers = filledUserAnswers

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CorrectReturnCYAController.onPageLoad.url
      }
    }

    "must return OK and contain correct net adjusted amount when prior return total was £0" in {
      val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "Changed the amount packaged as a contract packer").success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text must include("Balance")
        page.getElementsByTag("dt").text() must include("Original return total")
        page.getElementsByClass("original-return-total").text() must include("£0.00")
        page.getElementsByTag("dt").text() must include("New return total")
        page.getElementsByClass("new-return-total").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include("Account balance")
        page.getElementsByClass("balance-brought-forward").text() mustBe "£300.00"
        page.getElementsByTag("dt").text() must include("Net adjusted amount")
        page.getElementsByClass("net-adjusted-amount").text() must include("£4,500.00")
        page.getElementsByClass("net-adjusted-amount").text() mustNot include("−£4,500.00")
      }
    }

    "must return OK and contain correct net adjusted amount when prior return total is less than new return total" in {
      val amounts1 = Amounts(4000.00, 4200.00, -300.00, 4500.00, 500.00)
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "Changed the amount packaged as a contract packer").success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text must include("Balance")
        page.getElementsByTag("dt").text() must include("Original return total")
        page.getElementsByClass("original-return-total").text() must include("£4,000.00")
        page.getElementsByTag("dt").text() must include("New return total")
        page.getElementsByClass("new-return-total").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include("Account balance")
        page.getElementsByClass("balance-brought-forward").text() mustBe "£300.00"
        page.getElementsByTag("dt").text() must include("Net adjusted amount")
        page.getElementsByClass("net-adjusted-amount").text() must include("£500.00")
        page.getElementsByClass("net-adjusted-amount").text() mustNot include("−£500.00")
      }
    }

    "must return OK and contain correct net adjusted amount when prior return total is more than new return total when repayment method answered" in {
      val amounts1 = Amounts(40200.00, 4200.00, -300.00, 4500.00, -35700.00)
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "Changed the amount packaged as a contract packer").success.value
        .set(RepaymentMethodPage, BankAccount).success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByTag("h2").text must include("Balance")
        page.getElementsByTag("dt").text() must include("Original return total")
        page.getElementsByClass("original-return-total").text() must include("£40,200.00")
        page.getElementsByTag("dt").text() must include("New return total")
        page.getElementsByClass("new-return-total").text() must include("£4,200.00")
        page.getElementsByTag("dt").text() must include("Account balance")
        page.getElementsByClass("balance-brought-forward").text() mustBe "£300.00"
        page.getElementsByTag("dt").text() must include("Net adjusted amount")
        page.getElementsByClass("net-adjusted-amount").text() must include("−£35,700.00")
      }
    }

    "must redirect to RepaymentMethod and contain correct net adjusted amount when prior return total is more than new return total when repayment method is not answered" in {
      val amounts1 = Amounts(40200.00, 4200.00, -300.00, 4500.00, -35700.00)
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "Changed the amount packaged as a contract packer").success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockCorrectReturnOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RepaymentMethodController.onPageLoad(CheckMode).url
      }
    }

    "must submit successfully " in {
      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()
        when (mockCorrectReturnOrchestrator.submitReturn(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult((): Unit)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url
      }
    }

    "must return internal server error if submission is not a success" in {
      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
        .overrides(
          bind[CorrectReturnOrchestrator].toInstance(mockCorrectReturnOrchestrator)
        )
        .build()

      running(application) {
        when (mockCorrectReturnOrchestrator.submitReturn(any(), any(), any(), any())(any(), any())) thenReturn createFailureVariationResult(UnexpectedResponseFromSDIL)

        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "return to select change if user answers fails" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute))
          )
          .build()
      running(application) {
        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SelectChangeController.onPageLoad.url
      }
    }
  }
}
