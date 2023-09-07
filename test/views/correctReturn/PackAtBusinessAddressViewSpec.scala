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
import forms.correctReturn.PackAtBusinessAddressFormProvider
import models.backend.UkAddress
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import viewmodels.AddressFormattingHelper
import views.ViewSpecHelper
import views.html.correctReturn.PackAtBusinessAddressView

class PackAtBusinessAddressViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[PackAtBusinessAddressView]
  val formProvider = new PackAtBusinessAddressFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  val businessName: String = "ACME Drinks Ltd."
  val businessAddress: UkAddress = UkAddress(lines = List("First Line", "Middle Line", "Last Line"), postCode = "AA1 1AA")
  val address: Html = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))

  object Selectors {
    val HEADING_OUTSIDE_OF_FIELDSET = "govuk-heading-l"
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
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
    val html = view(form, NormalMode, address)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("correctReturn.packAtBusinessAddress.title"))
    }

    "should include the expected heading" in {
      val heading = document.getElementsByClass(Selectors.HEADING_OUTSIDE_OF_FIELDSET)
      heading.size() mustBe 1
      heading.get(0).text() mustEqual Messages("correctReturn.packAtBusinessAddress.heading")
    }

    "should not include a legend" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 0
    }

    "should include the business name and address" in {
      val body = document.getElementsByClass(Selectors.body)
      body.size() mustBe 1
      body.get(0).html().replace("\n", "") mustEqual address.body
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
      val html1 = view(form.fill(true), NormalMode, address)(request, messages(application))
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
      val html1 = view(form.fill(false), NormalMode, address)(request, messages(application))
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

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" - {
        val htmlYesSelected = view(form.fill(true), CheckMode, address)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode, address)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackAtBusinessAddressController.onSubmit(CheckMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackAtBusinessAddressController.onSubmit(CheckMode).url
        }
      }

      "when in NormalMode" - {
        val htmlYesSelected = view(form.fill(true), NormalMode, address)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode, address)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackAtBusinessAddressController.onSubmit(NormalMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackAtBusinessAddressController.onSubmit(NormalMode).url
        }
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, address)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("correctReturn.packAtBusinessAddress.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("correctReturn.packAtBusinessAddress.error.required")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
