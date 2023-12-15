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
import models.correctReturn.{AddASmallProducer, ChangedPage, RepaymentMethod}
import models.submission.Litreage
import models.{LitresInBands, ReturnPeriod, SmallProducer}
import orchestrators.CorrectReturnOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.correctReturn._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnUpdateDoneView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}

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

  val mockReturnOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]

  "Update Done Controller" - {

    "must return OK and the correct view for a GET" in {
      val litres = LitresInBands(2000, 4000)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000))))
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

      val currentReturnPeriod = ReturnPeriod(2023, 1)
      val testTime = Instant.now()
      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(correctReturnPeriod = Option(currentReturnPeriod),
        submittedOn = Some(testTime))))
        .overrides(bind[CorrectReturnOrchestrator].toInstance(mockReturnOrchestrator))
        .build()

      running(application) {
        val request = FakeRequest(GET, CorrectReturnUpdateDoneController.onPageLoad.url)

        when(mockReturnOrchestrator.calculateAmounts(any(), any(), any())(any(),any())) thenReturn createSuccessVariationResult(amounts)

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
  }
}
