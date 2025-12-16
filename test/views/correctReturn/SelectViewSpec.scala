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
import forms.correctReturn.SelectFormProvider
import models.ReturnPeriod
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.correctReturn.SelectView
class SelectViewSpec extends ViewSpecHelper {

  val view: SelectView = application.injector.instanceOf[SelectView]
  val formProvider = new SelectFormProvider
  val form: Form[String] = formProvider.apply()
  val returnsList: List[List[ReturnPeriod]] = List(returnPeriodsFor2022, returnPeriodsFor2020)
  implicit val request: Request[?] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-m"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val radios = "govuk-radios"
    val radioDivider = "govuk-radios__divider"
    val radiosInput = "govuk-radios__input"
    val radiosItems = "govuk-radios__item"
    val radiosLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val html = view(form, returnsList)(using request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("correctReturn.select" + ".title"))
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).text() mustEqual Messages("correctReturn.select.heading")
    }

    "should include radios" - {
      val radios = document.getElementsByClass(Selectors.radios)
      val dividers = document.getElementsByClass(Selectors.heading).select("h2")
      val radioItems = document.getElementsByClass(Selectors.radiosItems)
      val totalNumberOfReturnPeriods = returnsList.flatten.size

      s"that has a size of ${returnsList.size}" in {
        radios.size() mustBe 2
      }

      s"that has ${returnsList.size} dividers " in {
        dividers.size() mustBe returnsList.size
      }

      s"that has $totalNumberOfReturnPeriods radio items" in {
        radioItems.size() mustBe totalNumberOfReturnPeriods
      }

      returnsList.zipWithIndex.foreach { case (returnsForYear, index) =>
        val year = returnsForYear.head.year
        s"that includes a divider for year $year" in {
          dividers.get(index).text() mustBe year.toString
        }
      }

      returnPeriodList.zipWithIndex.foreach { case (returnPeriod, periodIndex) =>
        val year = returnPeriod.year
        val quarter = returnPeriod.quarter
        "when the form is empty and there are no errors" - {
          s"with the expected radio item for year $year and quarter $quarter that is unchecked" in {
            val expectedText = returnPeriod.quarter match {
              case 0 => s"January to March $year"
              case 1 => s"April to June $year"
              case 2 => s"July to September $year"
              case _ => s"October to December $year"
            }
            val radioItem = radioItems.get(periodIndex)
            radioItem.className() mustBe Selectors.radiosItems
            val radioInput = radioItem.getElementsByClass(Selectors.radiosInput)
            val radioLabel = radioItem.getElementsByClass(Selectors.radiosLabels)
            radioLabel.text() mustBe expectedText
            radioInput.attr("value") mustBe returnPeriod.radioValue
            radioInput.hasAttr("checked") mustBe false
          }
        }

        returnPeriodList.foreach { selectedReturnPeriod =>
          s"when the form is prepopulated with " + selectedReturnPeriod.radioValue + "selected and has no errors" - {
            val htmlPop =
              view(form.fill(selectedReturnPeriod.radioValue), returnsList)(using request, messages(application))
            val documentPop = doc(htmlPop)
            val radioItems = documentPop.getElementsByClass(Selectors.radiosItems)

            s"with the expected radio item for year $year and quarter $quarter that is checked equal ${selectedReturnPeriod == returnPeriod}" in {
              val expectedText = returnPeriod.quarter match {
                case 0 => s"January to March $year"
                case 1 => s"April to June $year"
                case 2 => s"July to September $year"
                case _ => s"October to December $year"
              }
              val radioItem = radioItems.get(periodIndex)
              radioItem.className() mustBe Selectors.radiosItems
              val radioInput = radioItem.getElementsByClass(Selectors.radiosInput)
              val radioLabel = radioItem.getElementsByClass(Selectors.radiosLabels)
              radioLabel.text() mustBe expectedText
              radioInput.attr("value") mustBe returnPeriod.radioValue
              radioInput.hasAttr("checked") mustBe selectedReturnPeriod == returnPeriod
            }
          }
        }
      }
    }

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" in {
      document
        .select(Selectors.form)
        .attr("action") mustEqual routes.SelectController.onSubmit.url
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), returnsList)(using request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("correctReturn.select.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0_0"
        errorSummary.text() mustBe Messages("correctReturn.select.error.required")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
