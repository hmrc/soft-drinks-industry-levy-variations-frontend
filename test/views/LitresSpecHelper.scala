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

package views

import forms.HowManyLitresFormProvider
import models.LitresInBands
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages

trait LitresSpecHelper extends ViewSpecHelper {


  val lowBandValue: Long = 1000
  val highBandValue: Long = 2000
  val litresInBands: LitresInBands = LitresInBands(lowBandValue, highBandValue)

  val formProvider = new HowManyLitresFormProvider()
  val form: Form[LitresInBands] = formProvider.apply()
  val formWithHighAndLowBands: Form[LitresInBands] = form.fill(litresInBands)
  val formWithLowBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(highBand = 0))
  val formWithHighBandOnly: Form[LitresInBands] = form.fill(litresInBands.copy(lowBand = 0))
  val emptyForm: Form[LitresInBands] = form.bind(Map("lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[LitresInBands] = form.bind(Map("lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[LitresInBands] = form.bind(Map("lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

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
    "should include form groups for litres" - {
      "when the form is not prepopulated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        "that includes 2 input fields" in {
          formGroups.size() mustEqual 2
        }
        "that includes a field for low band that is populated" in {
          val lowBandGroup = formGroups.get(0)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe true
          lowBandGroup.getElementById("lowBand").attr("value") mustBe lowBandValue.toString
        }
        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroups.get(1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe true
          highBandGroup.getElementById("highBand").attr("value") mustBe highBandValue.toString
        }
      }
    }
  }

  def testLitresInBandsNoPrepopulatedData(document: Document): Unit = {
    "should include form groups for litres" - {
      "when the form is populated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        "that includes 2 input fields" in {
          formGroups.size() mustEqual 2
        }
        "that includes a field for low band that is not populated" in {
          val lowBandGroup = formGroups.get(0)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe false
        }
        "that includes a field for high band that is not populated" in {
          val highBandGroup = formGroups.get(1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe false
        }
      }
    }
  }

  def testButton(document: Document): Unit = {
    "should contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }
  }

  def testAction(document: Document, expectedAction: String): Unit = {
    "should contains a form with the correct action" in {
      document.select(Selectors.form)
        .attr("action") mustEqual expectedAction
    }
  }

  def testEmptyFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form being empty" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.required")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.required")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testNoNumericFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no numeric values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.nonNumeric")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.nonNumeric")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testNegativeFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no negative values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")
          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)
          error1.text() mustBe Messages("litres.error.lowBand.negative")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.negative")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testDecimalFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing decimal values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.wholeNumber")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.wholeNumber")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testOutOfMaxValFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing values out of max range" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.outOfMaxVal")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.outOfMaxVal")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }
}
