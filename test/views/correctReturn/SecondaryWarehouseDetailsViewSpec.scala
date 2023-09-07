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
import forms.correctReturn.SecondaryWarehouseDetailsFormProvider
import models.backend.UkAddress
import models.{CheckMode, NormalMode, Warehouse}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.correctReturn.SecondaryWarehouseDetailsSummary
import views.ViewSpecHelper
import views.html.correctReturn.SecondaryWarehouseDetailsView

class SecondaryWarehouseDetailsViewSpec extends ViewSpecHelper with SummaryListFluency {

  val view: SecondaryWarehouseDetailsView = application.injector.instanceOf[SecondaryWarehouseDetailsView]
  val formProvider = new SecondaryWarehouseDetailsFormProvider
  val form: Form[Boolean] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

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

  "View" - {
    val WarehouseMap: Map[String,Warehouse] =
      Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
        "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

    val warehouseSummaryList: List[SummaryListRow] =
      SecondaryWarehouseDetailsSummary.row2(WarehouseMap)(messages(application))

    val summaryList: SummaryList = SummaryListViewModel(
      rows = warehouseSummaryList
    )

    val html = view(form, NormalMode, summaryList)(request, messages(application))
    val document = doc(html)

    "should contain the expected title" in {
      document.title() mustBe s"You added {0} UK warehouse{1} - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.legend).text() mustEqual Messages("Do you want to add another UK warehouse?")
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
      val WarehouseMap: Map[String,Warehouse] =
        Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
          "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

      val warehouseSummaryList: List[SummaryListRow] =
        SecondaryWarehouseDetailsSummary.row2(WarehouseMap)(messages(application))

      val summaryList: SummaryList = SummaryListViewModel(
        rows = warehouseSummaryList
      )

      val html1 = view(form.fill(true), NormalMode, summaryList)(request, messages(application))
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
      val WarehouseMap: Map[String,Warehouse] =
        Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
          "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

      val warehouseSummaryList: List[SummaryListRow] =
        SecondaryWarehouseDetailsSummary.row2(WarehouseMap)(messages(application))

      val summaryList: SummaryList = SummaryListViewModel(
        rows = warehouseSummaryList
      )

      val html1 = view(form.fill(false), NormalMode, summaryList)(request, messages(application))
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
        val WarhouseMap: Map[String,Warehouse] =
          Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
            "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )


        val htmlYesSelected = view(form.fill(true), CheckMode, summaryList)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), CheckMode, summaryList)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(CheckMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(CheckMode).url
        }
      }

      "when in NormalMode" - {
        val WarhouseMap: Map[String,Warehouse] =
          Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
            "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        val htmlYesSelected = view(form.fill(true), NormalMode, summaryList)(request, messages(application))
        val documentYesSelected = doc(htmlYesSelected)

        val htmlNoSelected = view(form.fill(false), NormalMode, summaryList)(request, messages(application))
        val documentNoSelected = doc(htmlNoSelected)
        "and yes is selected" in {
          documentYesSelected.select(Selectors.form)
            .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
        }

        "and no is selected" in {
          documentNoSelected.select(Selectors.form)
            .attr("action") mustEqual routes.SecondaryWarehouseDetailsController.onSubmit(NormalMode).url
        }
      }
    }

    "when there are form errors" - {
      val WarehouseMap: Map[String,Warehouse] =
        Map("1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
          "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE")))

      val warehouseSummaryList: List[SummaryListRow] =
        SecondaryWarehouseDetailsSummary.row2(WarehouseMap)(messages(application))

      val summaryList: SummaryList = SummaryListViewModel(
        rows = warehouseSummaryList
      )

      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, summaryList)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: You added {0} UK warehouse{1} - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("Select yes if you want to add another UK warehouse")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
