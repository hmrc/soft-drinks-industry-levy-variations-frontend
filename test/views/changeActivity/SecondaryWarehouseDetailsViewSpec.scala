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
import forms.changeActivity.SecondaryWarehouseDetailsFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.ViewSpecHelper
import views.html.changeActivity.SecondaryWarehouseDetailsView
import views.summary.changeActivity.SecondaryWarehouseDetailsSummary

class SecondaryWarehouseDetailsViewSpec extends ViewSpecHelper {

  val view: SecondaryWarehouseDetailsView = application.injector.instanceOf[SecondaryWarehouseDetailsView]
  val formProvider = new SecondaryWarehouseDetailsFormProvider
  val formWithWarehouses: Form[Boolean] = formProvider.apply(true)
  val formWithNoWarehouses: Form[Boolean] = formProvider.apply(false)
  implicit val request: Request[?] = FakeRequest()
  val warehousesInSummaryList: SummaryList = SummaryList(
    rows = SecondaryWarehouseDetailsSummary
      .summaryRows(warehouseAddedToUserAnswersForChangeActivity.warehouseList, NormalMode)
  )

  object Selectors {
    val heading = "govuk-fieldset__heading"
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

  "View - when there are warehouses" - {
    val html = view(formWithWarehouses, Some(warehousesInSummaryList), NormalMode)(using request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.legend).text() mustBe "Do you want to add another UK warehouse?"
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
      val html1 =
        view(formWithWarehouses.fill(true), Some(warehousesInSummaryList), NormalMode)(using
          request,
          messages(application)
        )
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
      val html1 =
        view(formWithWarehouses.fill(false), Some(warehousesInSummaryList), NormalMode)(using
          request,
          messages(application)
        )
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

    val expectedDetails = "Why should I register a warehouse?"
    val expectedDetailsInfo =
      "You can delay the point you will have to report liable drinks in a return and pay the levy on them if you register the warehouses you use to store them."

    "contain the correct details" - {
      document.getElementsByClass("govuk-details__summary-text").text() mustBe expectedDetails
    }

    "contain the correct details" - {
      document.getElementsByClass("govuk-details__text").text() mustBe expectedDetailsInfo
    }
    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {

      val htmlYesSelected =
        view(formWithWarehouses.fill(true), Some(warehousesInSummaryList), NormalMode)(using
          request,
          messages(application)
        )
      val documentYesSelected = doc(htmlYesSelected)

      val htmlNoSelected =
        view(formWithWarehouses.fill(false), Some(warehousesInSummaryList), NormalMode)(using
          request,
          messages(application)
        )
      val documentNoSelected = doc(htmlNoSelected)
      "and yes is selected" in {
        documentYesSelected
          .select(Selectors.form)
          .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
      }

      "and no is selected" in {
        documentNoSelected
          .select(Selectors.form)
          .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(formWithWarehouses.bind(Map("value" -> "")), Some(warehousesInSummaryList), NormalMode)(
        request,
        messages(application)
      )
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Select yes if you want to add another UK warehouse"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

  "View - when there are no warehouses" - {
    val html = view(formWithNoWarehouses, None, NormalMode)(using request, messages(application))

    val documentNoWarehouses = doc(html)
    "should contain the expected title" in {
      documentNoWarehouses.title() mustBe "UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = documentNoWarehouses.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.legend).text() mustBe "Do you want to add a UK warehouse?"
    }

    "contains a form with the correct action" - {

      val htmlYesSelected =
        view(formWithNoWarehouses.fill(true), None, NormalMode)(using request, messages(application))
      val documentYesSelected = doc(htmlYesSelected)
      val htmlNoSelected =
        view(formWithNoWarehouses.fill(false), None, NormalMode)(using request, messages(application))
      val documentNoSelected = doc(htmlNoSelected)
      "and yes is selected" in {
        documentYesSelected
          .select(Selectors.form)
          .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
      }

      "and no is selected" in {
        documentNoSelected
          .select(Selectors.form)
          .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors =
        view(formWithNoWarehouses.bind(Map("value" -> "")), None, NormalMode)(using request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Select yes if you want to add a UK warehouse"
      }
    }
  }
}
