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

package views.correctReturn

import controllers.correctReturn.routes
import forms.correctReturn.ClaimCreditsForExportsFormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.correctReturn.ClaimCreditsForExportsView
import views.ViewSpecHelper
class ClaimCreditsForExportsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[ClaimCreditsForExportsView]
  val formProvider = new ClaimCreditsForExportsFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLables = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "Do you want to claim a credit for liable drinks that have been exported? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include the expected hint text" in {
      val hint = document.getElementById("value-hint")
      hint.className() mustBe "govuk-hint"
      hint.text() mustBe "You can only claim a levy credit for drinks that you have paid the levy on or will pay the levy on. Do not include drinks produced for small producers or imported from them."
    }
    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.text() mustBe "Do you want to claim a credit for liable drinks that have been exported?"
    }

    "when the form is not preoccupied and has no errors" - {

      "should have radio buttons" - {
        val radioButtons = document.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is unchecked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "Yes"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "true"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe false
        }

        "that has the option to select No and is unchecked" in {
          val radioButton1 = radioButtons
            .get(1)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "No"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "false"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe false
        }
      }
    }

    "when the form is preoccupied with yes and has no errors" - {
      val html1 = view(form.fill(true), NormalMode)(request, messages(application))
      val document1 = doc(html1)
      "should have radio buttons" - {
        val radioButtons = document1.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is checked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "Yes"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "true"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe true
        }

        "that has the option to select No and is unchecked" in {
          val radioButton1 = radioButtons
            .get(1)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "No"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "false"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe false
        }
      }
    }

    "when the form is preoccupied with no and has no errors" - {
      val html1 = view(form.fill(false), NormalMode)(request, messages(application))
      val document1 = doc(html1)
      "should have radio buttons" - {
        val radioButtons = document1.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is unchecked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "Yes"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "true"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe false
        }

        "that has the option to select No and is checked" in {
          val radioButton1 = radioButtons
            .get(1)
          radioButton1
            .getElementsByClass(Selectors.radioLables)
            .text() mustBe "No"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .attr("value") mustBe "false"
          radioButton1
            .getElementsByClass(Selectors.radioInput)
            .hasAttr("checked") mustBe true
        }
      }
    }

    val expectedDetails = Map(
      "What can I claim a credit for?" -> "You can claim a credit for liable drinks that have been, or you expect to be, exported by you or someone else. You will need to get and keep evidence of details such as the: brand of the liable drinks supplier or consigner customer and destination the liable drinks are supplied to method of delivery If you do not have the evidence by the end of the quarter after you reported the liable drinks as exported, you must add the levy credit back in your next return.")
    testDetails(document, expectedDetails)

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" - {
        val htmlYesSelected = view(form.fill(true), CheckMode)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.ClaimCreditsForExportsController.onSubmit(CheckMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.ClaimCreditsForExportsController.onSubmit(CheckMode).url
        }
      }

      "when in NormalMode" - {
        val htmlYesSelected = view(form.fill(true), NormalMode)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.ClaimCreditsForExportsController.onSubmit(NormalMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.ClaimCreditsForExportsController.onSubmit(NormalMode).url
        }
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title must include("Error: Do you want to claim a credit for liable drinks that have been exported? - Soft Drinks Industry Levy - GOV.UK")
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Select yes if you want to claim a credit for liable drinks that has been exported"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
