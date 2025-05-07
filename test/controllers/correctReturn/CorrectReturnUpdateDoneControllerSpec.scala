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
import models.backend.RetrievedSubscription
import models.correctReturn.{AddASmallProducer, ChangedPage, RepaymentMethod}
import models.submission.Litreage
import models.{CheckMode, LitresInBands, ReturnPeriod, SdilReturn, SmallProducer, UserAnswers}
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
import views.html.correctReturn.CorrectReturnUpdateDoneView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import scala.concurrent.Future

class CorrectReturnUpdateDoneControllerSpec extends SpecBase with SummaryListFluency {

  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)

  val returnPeriodFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
  val currentReturnPeriod: ReturnPeriod = ReturnPeriod(getSentDateTime.toLocalDate)
  val returnPeriodStart: String = currentReturnPeriod.start.format(returnPeriodFormat)
  val returnPeriodEnd: String = currentReturnPeriod.end.format(returnPeriodFormat)

  val mockOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val litresInBands = LitresInBands(2000, 4000)
  val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn.copy(submitted = true, submittedOn = Some(Instant.now()))
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
    .set(CorrectionReasonPage, "foo").success.value
    .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

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

  private val testTime = Instant.now()

  "Update Done Controller" - {

    "must return OK and the correct view for a GET" in {
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

      val currentReturnPeriod = ReturnPeriod(2023, 1)
      def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
        when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
      }
      
      val application = correctReturnAction(
        userAnswers = Some(userAnswers
          .copy(correctReturnPeriod = Option(currentReturnPeriod), submittedOn = Some(testTime)))
      ).overrides(bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        val request = FakeRequest(GET, CorrectReturnUpdateDoneController.onPageLoad.url)

        when(mockOrchestrator.calculateAmounts(any(), any(), any(), any())(any(),any())) thenReturn createSuccessVariationResult(amounts)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnUpdateDoneView]
        val orgName = " Super Lemonade Plc"
        val section = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(userAnswers, aSubscription, changedPages,
          isCheckAnswers = false, amounts)
        val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
        val returnPeriodStart = currentReturnPeriod.start.format(returnPeriodFormat)
        val returnPeriodEnd = currentReturnPeriod.end.format(returnPeriodFormat)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, section, formattedDate, LocalDateTime.ofInstant(testTime, ZoneId.of("Europe/London"))
          .format(DateTimeFormatter.ofPattern("h:mma")), returnPeriodStart, returnPeriodEnd)(request, messages(application), frontendAppConfig).toString
      }
    }

    "must redirect to SelectController if submitted is false" in {
      def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
        when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
      }
      
      val application = correctReturnAction(
        userAnswers = Some(userAnswers
          .copy(correctReturnPeriod = Option(currentReturnPeriod), submitted = false))
      ).overrides(bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)).build()

      running(application) {
        val request = FakeRequest(GET, CorrectReturnUpdateDoneController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectController.onPageLoad.url
      }
    }

    "must not show own brands packaged when user is a small producer" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))

      val application = correctReturnAction(Some(userAnswers), subscription = Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() mustNot include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() mustNot include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
      }
    }

    "must show own brands packaged at own site row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(ownBrand = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-operatePackagingSiteOwnBrands") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-operatePackagingSiteOwnBrands") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show own brands packaged at own site row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersLabel"))
        page.getElementById("change-operatePackagingSiteOwnBrands") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-operatePackagingSiteOwnBrands") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-operatePackagingSiteOwnBrands") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show packaged as contract packer row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(PackagedAsContractPackerPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(packLarge = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show packaged as contract packer row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-packagedAsContractPacker") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-packagedAsContractPacker") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show packaged as contract packer row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.packagedAsContractPacker.checkYourAnswersLabel"))
        page.getElementById("change-packagedAsContractPacker") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-packagedAsContractPacker") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-packagedAsContractPacker") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show exemptions for small producers row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ExemptionsForSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(packSmall = smallProducerList))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show exemptions for small producers row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ExemptionsForSmallProducersPage, true).success.value
        .copy(smallProducerList = List(
          SmallProducer("", "XZSDIL000000234", Litreage(5000, 10000)),
          SmallProducer("", "XZSDIL000001234", Litreage(5000, 10000)),
        ))

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-small-producers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-small-producers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show exemptions for small producers row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ExemptionsForSmallProducersPage, true).success.value
        .copy(smallProducerList = List(
          SmallProducer("", "XZSDIL000000234", Litreage(5000, 10000)),
          SmallProducer("", "XZSDIL000001234", Litreage(5000, 10000)),
        ))

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.exemptionsForSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-exemptionsForSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-small-producers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-small-producers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£0.00")
      }
    }

    "must show brought into UK row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUKPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(importLarge = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show brought into UK row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUK") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUK") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show brought into UK row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUK.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUK") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUK") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUK") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show brought into UK from small producers row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(importSmall = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show brought into UK from small producers row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUkFromSmallProducersPage, true).success.value
        .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUkFromSmallProducers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUkFromSmallProducers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show brought into UK from small producers row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersLabel"))
        page.getElementById("change-broughtIntoUkFromSmallProducers") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-broughtIntoUkFromSmallProducers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-broughtIntoUkFromSmallProducers") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show claim credits for exports row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForExportsPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(`export` = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForExports") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForExports") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show claim credits for exports row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForExports.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForExports") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForExports") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForExports") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }

    "must show claim credits for lost or damaged row when present and answer is no" in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val application = correctReturnAction(Some(userAnswers), optOriginalReturn = Some(emptySdilReturn.copy(wastage = basicLitreage))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged") mustEqual null

        page.getElementsByTag("dt").text() mustNot include(Messages("litres.lowBand"))
        page.getElementsByTag("dt").text() mustNot include(Messages("litres.highBand"))
      }
    }

    "must show claim credits for lost or damaged row containing calculation when yes is selected - pre April 2025 rates" in {
      when(mockConfig.lowerBandCostPerLitre).thenReturn(BigDecimal("0.18"))
      when(mockConfig.higherBandCostPerLitre).thenReturn(BigDecimal("0.24"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(preApril2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForLostDamaged") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,800.00")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForLostDamaged") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£4,800.00")
      }
    }

    "must show claim credits for lost or damaged row containing calculation when yes is selected - 2025 tax year rates" in {
      when(mockConfig.lowerBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.194"))
      when(mockConfig.higherBandCostPerLitrePostApril2025).thenReturn(BigDecimal("0.259"))

      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(correctReturnPeriod = Some(taxYear2025ReturnPeriod), submitted = true, submittedOn = Some(testTime))
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(10000, 20000)).success.value

      val application = correctReturnAction(Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.getElementsByTag("h2").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader"))
        page.getElementsByTag("dt").text() must include(Messages("correctReturn.claimCreditsForLostDamaged.checkYourAnswersLabel"))
        page.getElementById("change-claimCreditsForLostDamaged") mustEqual null

        page.getElementsByTag("dt").text() must include(Messages("litres.lowBand"))
        page.getElementsByTag("dd").text() must include("10,000")
        page.getElementById("change-lowband-litreage-correctReturn.claimCreditsForLostDamaged") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.lowBandLevy"))
        page.getElementsByTag("dd").text() must include("£1,940.19")

        page.getElementsByTag("dt").text() must include(Messages("litres.highBand"))
        page.getElementsByTag("dd").text() must include("20,000")
        page.getElementById("change-highband-litreage-correctReturn.claimCreditsForLostDamaged") mustEqual null
        page.getElementsByTag("dt").text() must include(Messages("litres.highBandLevy"))
        page.getElementsByTag("dd").text() must include("£5,180.52")
      }
    }
  }
}
