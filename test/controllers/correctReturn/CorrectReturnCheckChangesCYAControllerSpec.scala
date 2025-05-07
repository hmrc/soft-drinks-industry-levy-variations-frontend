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
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.RequiredUserAnswersForCorrectReturn
import errors.UnexpectedResponseFromSDIL
import models.backend.RetrievedSubscription
import models.correctReturn.RepaymentMethod.BankAccount
import models.correctReturn.{AddASmallProducer, ChangedPage, RepaymentMethod}
import models.submission.Litreage
import models.{Amounts, CheckMode, LitresInBands, ReturnPeriod, SdilReturn, SmallProducer, UserAnswers}
import navigation.{FakeNavigatorForCorrectReturn, NavigatorForCorrectReturn}
import orchestrators.CorrectReturnOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.Page
import pages.correctReturn._
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import scala.concurrent.Future

class CorrectReturnCheckChangesCYAControllerSpec extends SpecBase with SummaryListFluency {

  val mockOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def correctReturnAction(userAnswers: Option[UserAnswers],
                          optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn),
                          subscription: Option[RetrievedSubscription] = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)
                         ): GuiceApplicationBuilder = {
    lazy val requiredAnswers: RequiredUserAnswersForCorrectReturn = new RequiredUserAnswersForCorrectReturn() {
      override def requireData(page: Page, userAnswers: UserAnswers, subscription: RetrievedSubscription)
                              (action: => Future[Result]): Future[Result] = action
    }
    when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts)
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers, subscription = subscription)
      .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
      .overrides(bind[RequiredUserAnswersForCorrectReturn].to(requiredAnswers))
  }

  private val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
  private val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

  private val basicLitreage = Litreage(1, 1)

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

      val application = correctReturnAction(userAnswers = Some(userAnswers), subscription = Some(aSubscription)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(),any())) thenReturn createSuccessVariationResult(amounts)
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
      val userAnswers = filledUserAnswers.remove(CorrectReturnBaseCYAPage).success.value

      when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(Some(emptySdilReturn)))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector),
          bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CorrectReturnCYAController.onPageLoad.url
      }
    }

    "must not show own brands packaged when user is a small producer" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))

      val application = correctReturnAction(Some(userAnswers), subscription = Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() mustNot include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() mustNot include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
      }
    }

    "must show own brands packaged at own site row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(ownBrand = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-operatePackagingSiteOwnBrands").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show packaged as contract packer row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(PackagedAsContractPackerPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show packaged as contract packer row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show packaged as contract packer row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-packagedAsContractPacker").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show exemptions for small producers row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ExemptionsForSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show exemptions for small producers row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ExemptionsForSmallProducersPage, true).success.value
        .copy(smallProducerList = List(
          SmallProducer("", "XZSDIL000000234", Litreage(5000, 10000)),
          SmallProducer("", "XZSDIL000001234", Litreage(5000, 10000)),
        ))

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-small-producers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-small-producers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show exemptions for small producers row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(ExemptionsForSmallProducersPage, true).success.value
        .copy(smallProducerList = List(
          SmallProducer("", "XZSDIL000000234", Litreage(5000, 10000)),
          SmallProducer("", "XZSDIL000001234", Litreage(5000, 10000)),
        ))

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-small-producers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-small-producers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show brought into UK row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUKPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUKController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show brought into UK row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUKController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show brought into UK row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUKController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUK").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show brought into UK from small producers row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show brought into UK from small producers row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUkFromSmallProducersPage, true).success.value
        .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show brought into UK from small producers row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUkFromSmallProducers").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show claim credits for exports row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForExportsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForExportsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForExportsController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show claim credits for lost or damaged row when present and answer is no" in {
      //      TODO: FAILING BECAUSE NOT CHANGED
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show claim credits for lost or damaged row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show claim credits for lost or damaged row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must return OK and contain correct net adjusted amount when prior return total was £0" in {
      val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
      val userAnswers = filledUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(CorrectionReasonPage, "Changed the amount packaged as a contract packer").success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

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
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

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
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

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

      when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(Some(emptySdilReturn)))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector),
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RepaymentMethodController.onPageLoad(CheckMode).url
      }
    }

    "must submit successfully " in {
      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()
        when (mockOrchestrator.submitReturn(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult((): Unit)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url
      }
    }

    "must return internal server error if submission is not a success" in {
      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
        .overrides(
          bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
        )
        .build()

      running(application) {
        when (mockOrchestrator.submitReturn(any(), any(), any(), any())(any(), any())) thenReturn createFailureVariationResult(UnexpectedResponseFromSDIL)

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
