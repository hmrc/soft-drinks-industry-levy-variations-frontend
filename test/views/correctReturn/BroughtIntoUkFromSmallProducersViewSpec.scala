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
import forms.correctReturn.BroughtIntoUkFromSmallProducersFormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.correctReturn.BroughtIntoUkFromSmallProducersView
import views.ViewSpecHelper
class BroughtIntoUkFromSmallProducersViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[BroughtIntoUkFromSmallProducersView]
  val formProvider = new BroughtIntoUkFromSmallProducersFormProvider
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
      document.title() mustBe "Are you reporting liable drinks you have brought into the UK from small producers? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustEqual "Are you reporting liable drinks you have brought into the UK from small producers?"
    }

    "should include the expected hint text" in {
      val hint = document.getElementById("value-hint")
      hint.className() mustBe "govuk-hint"
      hint.text() mustBe "Include your own brands of liable drinks produced outside of the UK."
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
      "Liable drinks from small producers" -> "If you are a registered small producer and you bring your own brand of liable drinks into the UK, you still need to report them but you will not pay the levy on them. If you bring liable drinks into the UK from someone else who would be considered a small producer, you need to get evidence of the: contact details, EU VAT number (if they have one) and website of the business amount of litres of liable drinks packaged globally for brands the business owns in the past 12 months signature of someone from the business, their position and the date of the signature")

    testDetails(document, expectedDetails)

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" - {
        val htmlYesSelected = view(form.fill(true), CheckMode)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.BroughtIntoUkFromSmallProducersController.onSubmit(CheckMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.BroughtIntoUkFromSmallProducersController.onSubmit(CheckMode).url
        }
      }

      "when in NormalMode" - {
        val htmlYesSelected = view(form.fill(true), NormalMode)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.BroughtIntoUkFromSmallProducersController.onSubmit(NormalMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.BroughtIntoUkFromSmallProducersController.onSubmit(NormalMode).url
        }
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title must include("Error: Are you reporting liable drinks you have brought into the UK from small producers? - Soft Drinks Industry Levy - GOV.UK")
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Select yes if are you reporting liable drinks you have brought into the UK from small producers?"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
