package controllers

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

import controllers.correctReturn.routes
import models.backend.RetrievedSubscription
import models.correctReturn.AddASmallProducer
import models.{CheckMode, LitresInBands, SdilReturn, UserAnswers}
import org.jsoup.nodes.Element
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.QuestionPage
import pages.correctReturn._
import play.api.libs.json.Json
import testSupport.SDILBackendTestData.{smallProducerList, submittedDateTime}
import utilities.UserTypeCheck

import java.time.ZoneOffset

trait CorrectReturnBaseCYASummaryISpecHelper extends ControllerITTestHelper {

  object Selectors {
    val heading = "govuk-heading-l"
    val body = "govuk-body"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val button = "govuk-button"
    val form = "form"
  }

  val operatePackagingSiteLitres: LitresInBands = LitresInBands(1000, 2000)
  val contractPackingLitres: LitresInBands = LitresInBands(3000, 4000)
  val importsLitres: LitresInBands = LitresInBands(5000, 6000)
  val importsSmallProducerLitres: LitresInBands = LitresInBands(5000, 6000)
  val smallProducerLitres: LitresInBands = LitresInBands(2000, 4000)
  val emptyReturn: SdilReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0), submittedOn =
    Some(submittedDateTime.toInstant(ZoneOffset.UTC)))
  val populatedReturn: SdilReturn = SdilReturn((100, 200), (200, 100),
    smallProducerList, (300, 400), (400, 300), (50, 60), (60, 50),
    submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))

  def userAnswerWithLitresForAllPagesNilSdilReturn: UserAnswers = emptyUserAnswersForCorrectReturn
    .copy(data = Json.obj("originalSDILReturn" -> Json.toJson(emptyReturn)))
    .copy(packagingSiteList = packagingSitesFromSubscription, warehouseList = warehousesFromSubscription, smallProducerList = smallProducersAddedList)
    .set(OperatePackagingSiteOwnBrandsPage, true).success.value
    .set(HowManyOperatePackagingSiteOwnBrandsPage, operatePackagingSiteLitres).success.value
    .set(PackagedAsContractPackerPage, true).success.value
    .set(HowManyPackagedAsContractPackerPage, contractPackingLitres).success.value
    .set(ExemptionsForSmallProducersPage, true).success.value
    .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", smallProducerLitres)).success.value
    .set(BroughtIntoUKPage, true).success.value
    .set(HowManyBroughtIntoUKPage, importsLitres).success.value
    .set(BroughtIntoUkFromSmallProducersPage, true).success.value
    .set(HowManyBroughtIntoUkFromSmallProducersPage, importsSmallProducerLitres).success.value
    .set(ClaimCreditsForExportsPage, true).success.value
    .set(HowManyClaimCreditsForExportsPage, importsLitres).success.value
    .set(ClaimCreditsForLostDamagedPage, true).success.value
    .set(HowManyCreditsForLostDamagedPage, importsLitres).success.value
    .set(AskSecondaryWarehouseInReturnPage, true).success.value
    .set(SecondaryWarehouseDetailsPage, false).success.value

  def userAnswerWithOnePageChangedAndNilSdilReturn(page: QuestionPage[Boolean], howManyPage: QuestionPage[LitresInBands]): UserAnswers = emptyUserAnswersForCorrectReturn
    .copy(data = Json.obj("originalSDILReturn" -> Json.toJson(emptyReturn)))
    .set(page, true).success.value
    .set(howManyPage, operatePackagingSiteLitres).success.value

  def userAnswerWithExemptionSmallProducerPageUpdatedAndNilSdilReturn: UserAnswers = emptyUserAnswersForCorrectReturn
    .copy(data = Json.obj("originalSDILReturn" -> Json.toJson(emptyReturn)))
    .copy(smallProducerList = smallProducersAddedList)
    .set(ExemptionsForSmallProducersPage, true).success.value
    .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", smallProducerLitres)).success.value

  def userAnswerWithAllNosWithOriginalSdilReturn: UserAnswers = emptyUserAnswersForCorrectReturn
      .copy(data = Json.obj("originalSDILReturn" -> Json.toJson(populatedReturn)))
      .set(OperatePackagingSiteOwnBrandsPage, false).success.value
      .set(PackagedAsContractPackerPage, false).success.value
      .set(ExemptionsForSmallProducersPage, false).success.value
      .set(BroughtIntoUKPage, false).success.value
      .set(BroughtIntoUkFromSmallProducersPage, false).success.value
      .set(ClaimCreditsForExportsPage, false).success.value
      .set(ClaimCreditsForLostDamagedPage, false).success.value

  def validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites: Element,
                                                         litresInBands: LitresInBands,
                                                         isCheckAnswers: Boolean): Assertion = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if(isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you package your own brands at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you package your own brands at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in low band packaged at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in low band packaged at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in high band packaged at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in high band packaged at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites: Element,
                                                           isCheckAnswers: Boolean): Assertion = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you package your own brands at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you package your own brands at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateContractPackingWithLitresSummaryList(operatePackagingSites: Element,
                                                         litresInBands: LitresInBands,
                                                   isCheckAnswers: Boolean): Assertion = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe
        "Change whether you use contract packing at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you use contract packing at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in low band contract packed at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in low band contract packed at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in high band contract packed at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in high band contract packed at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyPackagedAsContractPackerController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateContractPackingWithNoLitresSummaryList(exemptionsForSmallProducers: Element,
                                                   isCheckYourAnswers: Boolean): Assertion = {
    val rows = exemptionsForSmallProducers.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you use contract packing at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you use contract packing at your own site"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateContractPackedForSmallProducersWithLitresSummaryList(operatePackagingSites: Element,
                                                   litresInBands: LitresInBands,
                                                   isCheckAnswers: Boolean): Assertion = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe
        "Change whether you contract pack for registered small producers"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you contract pack for registered small producers"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in low band you are claiming exemption for"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in low band you are claiming exemption for"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in high band you are claiming exemption for"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in high band you are claiming exemption for"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateContractPackedForSmallProducersWithNoLitresSummaryList(operatePackagingSites: Element,
                                                     isCheckYourAnswers: Boolean) = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you contract pack for registered small producers"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you contract pack for registered small producers"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateImportsWithLitresSummaryList(imports: Element,
                                                         litresInBands: LitresInBands,
                                                         isCheckYourAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")

    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you bring liable drinks into the UK"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you bring liable drinks into the UK"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.BroughtIntoUKController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckYourAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in low band brought into the UK"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in low band brought into the UK"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckYourAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in high band brought into the UK"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in high band brought into the UK"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateImportsWithNoLitresSummaryList(imports: Element,
                                           isCheckYourAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"

    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you bring liable drinks into the UK"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you bring liable drinks into the UK"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.BroughtIntoUKController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateImportsFromSmallProducersWithLitresSummaryList(imports: Element,
                                           litresInBands: LitresInBands,
                                           isCheckYourAnswers: Boolean) = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you bring liable drinks into the UK from a small producer"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you bring liable drinks into the UK from a small producer"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckYourAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in low band brought into the UK from a small producer"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in low band brought into the UK from a small producer"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckYourAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in high band brought into the UK from a small producer"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in high band brought into the UK from a small producer"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateImportsFromSmallProducersWithNoLitresSummaryList(imports: Element,
                                             isCheckYourAnswers: Boolean) = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you bring liable drinks into the UK from a small producer"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you bring liable drinks into the UK from a small producer"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateExportsWithLitresSummaryList(imports: Element,
                                           litresInBands: LitresInBands,
                                           isCheckAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe
        "Change whether you want to claim credits for exported liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you want to claim credits for exported liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ClaimCreditsForExportsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in the low band for exported liable drinks"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in the low band for exported liable drinks"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in the high band for exported liable drinks"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in the high band for exported liable drinks"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateExportsWithNoLitresSummaryList(imports: Element, isCheckYourAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you want to claim credits for exported liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you want to claim credits for exported liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ClaimCreditsForExportsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateLostOrDamagedWithLitresSummaryList(imports: Element,
                                                 litresInBands: LitresInBands,
                                                 isCheckAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(3)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe
        "Change whether you want to claim credits for lost or destroyed liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you want to claim credits for lost or destroyed liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in the low band for lost or destroyed liable drinks"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in the low band for lost or destroyed liable drinks"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change number of litres in the high band for lost or destroyed liable drinks"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "number of litres in the high band for lost or destroyed liable drinks"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateLostOrDamagedWithNoLitresSummaryList(imports: Element,
                                                   isCheckYourAnswers: Boolean): Assertion = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().text() mustBe "Change whether you want to claim credits for lost or destroyed liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
        .first().text() mustBe "whether you want to claim credits for lost or destroyed liable drinks"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
        .first().attr("href") mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateIfNewPacker(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    UserTypeCheck.isNewPacker(SdilReturn.apply(userAnswers), subscription) && subscription.productionSites.isEmpty
  }

  def validateIfNewImporter(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    UserTypeCheck.isNewImporter(SdilReturn.apply(userAnswers), subscription) && subscription.warehouseSites.isEmpty
  }


  def validateSiteDetailsSummary(userAnswers: UserAnswers,
                                 subscription: RetrievedSubscription,
                                 summaryList: Element,
                                 numberOfPackagingSites: Int = 0,
                                 numberOfWarehouses: Int = 0,
                                 isCheckAnswers: Boolean = true): Unit = {
    val newImporter = validateIfNewImporter(userAnswers, subscription)
    val newPacker = validateIfNewPacker(userAnswers, subscription)
    val rows = summaryList.getElementsByClass("govuk-summary-list__row")
    if (!newPacker && newImporter && numberOfWarehouses != 0) {
      rows.size() mustBe 1
      testWarehouseSitesRow(rows.get(0))
    } else if (newPacker && numberOfPackagingSites != 0 && !newImporter) {
      rows.size() mustBe 1
      testPackingSitesRow(rows.get(0))
    } else if (newPacker && numberOfPackagingSites != 0 && newImporter && numberOfWarehouses != 0) {
      rows.size() mustBe 2
      testPackingSitesRow(rows.get(0))
      testWarehouseSitesRow(rows.get(1))
    } else {
      rows.size() mustBe 0
    }

    def testPackingSitesRow(packingRow: Element) = {
      if (isCheckAnswers && newPacker) {
        val packingLink = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
        packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
          .first().attr("href") mustBe packingLink
        if (numberOfPackagingSites == 1) {
          packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
            .first().text() mustBe "Change the UK packaging site that you operate to produce liable drinks"
          packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
            .first().text() mustBe "the UK packaging site that you operate to produce liable drinks"
        } else {
          packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
            .first().text() mustBe "Change the UK packaging sites that you operate to produce liable drinks"
          packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
            .first().text() mustBe "the UK packaging sites that you operate to produce liable drinks"
        }
      } else {
        packingRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
      }
    }

    def testWarehouseSitesRow(warehouseRow: Element) = {
      if (isCheckAnswers && newImporter) {
        val warehouseLink = if (numberOfWarehouses == 0) {
          routes.AskSecondaryWarehouseInReturnController.onPageLoad(CheckMode).url
        } else {
          routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url
        }
        warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
          .first().attr("href") mustBe warehouseLink
        if (numberOfWarehouses == 1) {
          warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
            .first().text() mustBe "Change the UK warehouse you use to store liable drinks"
          warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
            .first().text() mustBe "the UK warehouse you use to store liable drinks"
        }
        warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a")
          .first().text() mustBe "Change the UK warehouses you use to store liable drinks"
        warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden")
          .first().text() mustBe "the UK warehouses you use to store liable drinks"
      } else {
        warehouseRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
      }
    }
  }
}
