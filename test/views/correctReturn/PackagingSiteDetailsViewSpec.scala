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
import forms.correctReturn.PackagingSiteDetailsFormProvider
import models.backend.{Site, UkAddress}
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.correctReturn.PackagingSiteDetailsView
import java.time.LocalDate

class PackagingSiteDetailsViewSpec extends ViewSpecHelper {

  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val formProvider = new PackagingSiteDetailsFormProvider
  val form: Form[Boolean] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
    val summaryListActions = "govuk-summary-list__actions"
  }

  "Correct Returns, Packaging Site Details View" - {
    val html = view(form, NormalMode, packagingSites = Map.empty)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).text() mustBe "Do you want to add another UK packaging site?"
    }

    "should include the expected heading" in {
      val heading = document.getElementsByClass("govuk-heading-m")
      heading.size() mustBe 1
      heading.text() mustBe "You added 0 UK packaging sites"
    }

    "when the form is not preoccupied and has no errors" - {

      "should have radio buttons" - {
        val radioButtons = document.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is unchecked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLabels)
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
            .getElementsByClass(Selectors.radioLabels)
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
      val html1 = view(form.fill(true), NormalMode, packagingSites = Map.empty)(request, messages(application))
      val document1 = doc(html1)
      "should have radio buttons" - {
        val radioButtons = document1.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is checked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLabels)
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
            .getElementsByClass(Selectors.radioLabels)
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
      val html1 = view(form.fill(false), NormalMode, packagingSites = Map.empty)(request, messages(application))
      val document1 = doc(html1)
      "should have radio buttons" - {
        val radioButtons = document1.getElementsByClass(Selectors.radios)
        "that has the option to select Yes and is unchecked" in {
          val radioButton1 = radioButtons
            .get(0)
          radioButton1
            .getElementsByClass(Selectors.radioLabels)
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
            .getElementsByClass(Selectors.radioLabels)
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
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" - {
        val htmlYesSelected = view(form.fill(true), CheckMode, packagingSites = Map.empty)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode, packagingSites = Map.empty)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(CheckMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(CheckMode).url
        }
      }

      "when in NormalMode" - {
        val htmlYesSelected = view(form.fill(true), NormalMode, packagingSites = Map.empty)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode, packagingSites = Map.empty)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(NormalMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(NormalMode).url
        }
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, packagingSites = Map.empty)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Select yes if you want to add another UK packaging site"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

  "View should contain the correct heading and summary row details" - {
    lazy val pSite = Site(
      UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
      Some("88"),
      Some("Wild Lemonade Group"),
      Some(LocalDate.of(2018, 2, 26)))
    lazy val pSite2 = Site(
      UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
      Some("88"),
      None,
      Some(LocalDate.of(2018, 2, 26)))
    lazy val packagingSites2 = Map("000001" -> pSite, "00002" -> pSite2)
    lazy val packagingSites = Map("00213" -> pSite)

    val html1 = view(form.fill(true), NormalMode, packagingSites = Map.empty)(request, messages(application))
    val document1 = doc(html1)
    val heading1 = document1.getElementsByClass("govuk-heading-m").get(0).text()
    val html2 = view(form.fill(true), NormalMode, packagingSites)(request, messages(application))
    val document2 = doc(html2)
    val heading2 = document2.getElementsByClass("govuk-heading-m").get(0).text()
    val html3 = view(form.fill(true), NormalMode, packagingSites2)(request, messages(application))
    val document3 = doc(html3)
    val heading3 = document3.getElementsByClass("govuk-heading-m").get(0).text()

    "when the list is empty the heading should be pluralised" in {
      heading1 mustBe "You added 0 UK packaging sites"
    }

    "when the list has one it should be singular" in {
      heading2 mustBe "You added 1 UK packaging site"
    }

    "when the list has multiple packaging sites the heading should be pluralised" in {
      heading3 mustBe "You added 2 UK packaging sites"
    }

    "when there is 1 packaging site the summary list should display the correct packaging site information and the remove link should not be present" in {
      val listItems = document2.getElementsByClass(Selectors.summaryListActions)
      val summaryListKey = document2.getElementsByClass("govuk-summary-list__key")

      listItems.text() mustBe ""
      summaryListKey.size mustBe 1
    }

    "when there are 2 packaging sites the summary list should display the correct packaging site information" in {
      val listItems = document3.getElementsByClass(Selectors.summaryListActions)
      val summaryListKey = document3.getElementsByClass("govuk-summary-list__key")

      summaryListKey.size mustBe 2
      listItems.get(0).text() mustBe "Remove Wild Lemonade Group site address 33 Rhes Priordy"
      listItems.get(1).text() mustBe "Remove site address 33 Rhes Priordy"
    }
  }

}
