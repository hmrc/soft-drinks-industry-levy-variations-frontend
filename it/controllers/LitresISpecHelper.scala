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

package controllers

import models.LitresInBands
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}


trait LitresISpecHelper extends ControllerITTestHelper {


  val lowBandValue: Long = 1000
  val highBandValue: Long = 2000
  val lowBandValueDiff: Long = 1100
  val highBandValueDiff: Long = 2200
  val litresInBands: LitresInBands = LitresInBands(lowBandValue, highBandValue)
  val litresInBandsDiff: LitresInBands = LitresInBands(lowBandValueDiff, highBandValueDiff)
  val litresInBandsObj: JsObject = Json.obj("litres.lowBand" -> s"$lowBandValue", "litres.highBand" -> s"$highBandValue")
  val litresInBandsDiffObj: JsObject = Json.obj("litres.lowBand" -> s"$lowBandValueDiff", "litres.highBand" -> s"$highBandValueDiff")

  val emptyJson: JsObject = Json.obj("litres.lowBand" -> "", "litres.highBand" -> "")
  val jsonWithNoNumeric: JsObject = Json.obj("litres.lowBand" -> "x", "litres.highBand" -> "y")
  val jsonWithNegativeNumber: JsObject = Json.obj("litres.lowBand" -> "-1", "litres.highBand" -> "-2")
  val jsonWithDecimalNumber: JsObject = Json.obj("litres.lowBand" -> "1.8", "litres.highBand" -> "2.3")
  val jsonWithOutOfRangeNumber: JsObject = Json.obj("litres.lowBand" -> "110000000000000", "litres.highBand" -> "120000000000000")
  val jsonWith0: JsObject = Json.obj("litres.lowBand" -> "0", "litres.highBand" -> "0")

  object Selectors {
    val heading = "govuk-heading-m"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val button = "govuk-button"
    val form = "form"
  }


  def testLitresInBandsWithPrepopulatedData(document: Document): Unit = {
    val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
    formGroups.size() mustEqual 2
    val lowBandGroup = formGroups.get(0)
    lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
    lowBandGroup.getElementById("litres.lowBand-hint").text() mustBe Messages("litres.lowBandHint")
    lowBandGroup.getElementById("litres.lowBand").hasAttr("value") mustBe true
    lowBandGroup.getElementById("litres.lowBand").attr("value") mustBe lowBandValue.toString
    val highBandGroup = formGroups.get(1)
    highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
    highBandGroup.getElementById("litres.highBand-hint").text() mustBe Messages("litres.highBandHint")
    highBandGroup.getElementById("litres.highBand").hasAttr("value") mustBe true
    highBandGroup.getElementById("litres.highBand").attr("value") mustBe highBandValue.toString
  }

  def testLitresInBandsNoPrepopulatedData(document: Document): Unit = {
    val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
    formGroups.size() mustEqual 2
    val lowBandGroup = formGroups.get(0)
    lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
    lowBandGroup.getElementById("litres.lowBand-hint").text() mustBe Messages("litres.lowBandHint")
    lowBandGroup.getElementById("litres.lowBand").hasAttr("value") mustBe false
    val highBandGroup = formGroups.get(1)
    highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
    highBandGroup.getElementById("litres.highBand-hint").text() mustBe Messages("litres.highBandHint")
    highBandGroup.getElementById("litres.highBand").hasAttr("value") mustBe false
  }

  def testEmptyFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 2
    val error1 = errors.get(0)
    val error2 = errors.get(1)

    error1.text() mustBe Messages("litres.error.lowBand.required")
    error1.select("a").attr("href") mustBe "#litres.lowBand"
    error2.text() mustBe Messages("litres.error.highBand.required")
    error2.select("a").attr("href") mustBe "#litres.highBand"
  }

  def testNoNumericFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 2
    val error1 = errors.get(0)
    val error2 = errors.get(1)

    error1.text() mustBe Messages("litres.error.lowBand.nonNumeric")
    error1.select("a").attr("href") mustBe "#litres.lowBand"
    error2.text() mustBe Messages("litres.error.highBand.nonNumeric")
    error2.select("a").attr("href") mustBe "#litres.highBand"
  }

  def testZeroFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 1
    val error1 = errors.get(0)


    error1.text() mustBe Messages("litres.error.negative")
    error1.select("a").attr("href") mustBe "#litres"
  }

  def testNegativeFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 2
    val error1 = errors.get(0)
    val error2 = errors.get(1)

    error1.text() mustBe Messages("litres.error.lowBand.negative")
    error1.select("a").attr("href") mustBe "#litres.lowBand"
    error2.text() mustBe Messages("litres.error.highBand.negative")
    error2.select("a").attr("href") mustBe "#litres.highBand"
  }

  def testDecimalFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 2
    val error1 = errors.get(0)
    val error2 = errors.get(1)

    error1.text() mustBe Messages("litres.error.lowBand.wholeNumber")
    error1.select("a").attr("href") mustBe "#litres.lowBand"
    error2.text() mustBe Messages("litres.error.highBand.wholeNumber")
    error2.select("a").attr("href") mustBe "#litres.highBand"
  }

  def testOutOfMaxValFormErrors(document: Document, errorTitle: String): Unit = {
    document.title() must include(errorTitle)
    val errorSummary = document
      .getElementsByClass(Selectors.errorSummaryList)
      .first()
    val errors = errorSummary.getElementsByTag("li")

    errors.size() mustEqual 2
    val error1 = errors.get(0)
    val error2 = errors.get(1)

    error1.text() mustBe Messages("litres.error.lowBand.outOfMaxVal")
    error1.select("a").attr("href") mustBe "#litres.lowBand"
    error2.text() mustBe Messages("litres.error.highBand.outOfMaxVal")
    error2.select("a").attr("href") mustBe "#litres.highBand"
  }
}
