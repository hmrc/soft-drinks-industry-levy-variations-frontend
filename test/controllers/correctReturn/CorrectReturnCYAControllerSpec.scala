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
import controllers.correctReturn.routes._
import models.SelectChange.CorrectReturn
import models.backend.RetrievedSubscription
import models.correctReturn.AddASmallProducer
import models.submission.Litreage
import models.{Amounts, CheckMode, LitresInBands, ReturnPeriod, SdilReturn, SmallProducer, UserAnswers}
import orchestrators.CorrectReturnOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.Page
import pages.correctReturn._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

import scala.concurrent.Future

class CorrectReturnCYAControllerSpec extends SpecBase with SummaryListFluency {

  val mockOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def correctReturnAction(userAnswers: Option[UserAnswers],
                          optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn),
                          subscription: Option[RetrievedSubscription] = None): GuiceApplicationBuilder = {
    lazy val requiredAnswers: RequiredUserAnswersForCorrectReturn = new RequiredUserAnswersForCorrectReturn() {
//      override def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = action
      override def requireData(page: Page, userAnswers: UserAnswers, subscription: RetrievedSubscription)
                              (action: => Future[Result]): Future[Result] = action
    }
//    guiceApplicationBuilder.overrides(bind[RequiredUserAnswersForCorrectReturn].to(requiredAnswers))
    val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
    when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers, subscription = subscription)
      .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
      .overrides(bind[RequiredUserAnswersForCorrectReturn].to(requiredAnswers))
  }

  private val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
  private val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val litres = LitresInBands(100, 200)
      val amounts1 = Amounts(0.00, 4200.00, -300.00, 4500.00, 4500.00)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000))))
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


      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
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

    "must not show own brands packaged when user is a small producer" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))

      val application = correctReturnAction(Some(userAnswers), subscription = Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(PackagedAsContractPackerPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ExemptionsForSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      // TODO: Figure out how to implement
    }

    "must show exemptions for small producers row containing calculation when yes is selected - 2025 tax year rates" in {
      // TODO: Figure out how to implement
    }

    "must show brought into UK row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUKPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      //      TODO: UNEXPECTED FAILURE
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
        page.getElementsByTag("dd").text() must include("-£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        //      TODO: UNEXPECTED FAILURE HERE
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForExports").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4,800.00")
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - 2025 tax year rates" in {
      //      TODO: UNEXPECTED FAILURE, ALTHOUGH WILL FAIL AFTERWARDS ON LEVY VALUES
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
      //      TODO: UNEXPECTED FAILURE
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(preApril2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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
        page.getElementsByTag("dd").text() must include("-£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForLostDamaged").attributes().get("href") mustEqual
          controllers.correctReturn.routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("-£4,800.00")
      }
    }

    "must show claim credits for lost or damaged row containing calculation when yes is selected - 2025 tax year rates" in {
      //      TODO: UNEXPECTED FAILURE, ALTHOUGH WILL FAIL AFTERWARDS ON LEVY VALUES
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers), subscription = Some(updatedSubscriptionWithChangedActivityToNewImporterAndPacker)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
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

    "must return OK and contain amount owed header when balance and return amount is positive (i.e. there is an amount to pay)" in {
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", Litreage())
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", Litreage())
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

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("cya-inset-sub-header").text() mustBe "You need to pay £4,500.00"
        page.getElementsByTag("h2").text must include("Summary")
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
      val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", Litreage())
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", Litreage())
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

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()
      running(application) {
        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(), any())) thenReturn createSuccessVariationResult(amounts1)
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementById("cya-inset-sub-header").text() mustBe "Your Soft Drinks Levy Account will be credited £3,900.00"
        page.getElementsByTag("h2").text must include("Summary")
        page.getElementsByTag("dt").text() must include("Total this quarter")
        page.getElementsByClass("total-for-quarter").text() must include("−£4,200.00")
        page.getElementsByTag("dt").text() must include("Balance brought forward")
        page.getElementsByClass("balance-brought-forward").text() must include("£300.00")
        page.getElementsByTag("dt").text() must include("Total")
        page.getElementsByClass("total").text() must include("−£3,900.00")
      }
    }

    testInvalidJourneyType(CorrectReturn, CorrectReturnCYAController.onPageLoad.url)
    testNoUserAnswersError(CorrectReturnCYAController.onPageLoad.url)
  }
}
