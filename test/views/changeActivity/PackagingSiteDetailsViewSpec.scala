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

package views.changeActivity

import controllers.changeActivity.routes
import forms.changeActivity.PackagingSiteDetailsFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.html.changeActivity.PackagingSiteDetailsView
import views.ViewSpecHelper
class PackagingSiteDetailsViewSpec extends ViewSpecHelper {

  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val formProvider = new PackagingSiteDetailsFormProvider
  val form: Form[Boolean] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val html = view(form, NormalMode, SummaryList())(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("changeActivity.packagingSiteDetails" + ".title"))
    }

    "should include a heading with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.heading)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustEqual Messages("changeActivity.packagingSiteDetails.heading")
    }

    "should include a legend with the expected sub-heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.legend).text() mustEqual Messages("changeActivity.packagingSiteDetails.subHeading")
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
      val html1 = view(form.fill(true), NormalMode, SummaryList())(request, messages(application))
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
      val html1 = view(form.fill(false), NormalMode, SummaryList())(request, messages(application))
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

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" - {
        val htmlYesSelected = view(form.fill(true), CheckMode, SummaryList())(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode, SummaryList())(request, messages(application))
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
        val htmlYesSelected = view(form.fill(true), NormalMode, SummaryList())(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode, SummaryList())(request, messages(application))
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
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, SummaryList())(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("changeActivity.packagingSiteDetails.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("changeActivity.packagingSiteDetails.error.required")
      }
    }

    "when there is one packaging site only" - {
      val summaryListWithOneRow = SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("Packaging Site")))))
      val htmlWithOneSummaryListRow = view(form, NormalMode, summaryListWithOneRow)(request, messages(application))
      val documentWithOneSummaryListRow = doc(htmlWithOneSummaryListRow)
      val expectedDetails = Map(
        Messages("changeActivity.packagingSiteDetails.detailsLink") -> Messages("changeActivity.packagingSiteDetails.detailsInfo")
      )
      testDetails(documentWithOneSummaryListRow, expectedDetails)
    }

    "when there is more than one packaging site" - {
      val summaryListWithTwoRows = SummaryList(Seq(
        SummaryListRow(value = Value(content = HtmlContent("Packaging Site"))),
        SummaryListRow(value = Value(content = HtmlContent("Another Packaging Site")))
      ))
      val htmlWithTwoSummaryListRows = view(form, NormalMode, summaryListWithTwoRows)(request, messages(application))
      val documentWithTwoSummaryListRows = doc(htmlWithTwoSummaryListRows)
      testDetails(documentWithTwoSummaryListRows, Map.empty)
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
