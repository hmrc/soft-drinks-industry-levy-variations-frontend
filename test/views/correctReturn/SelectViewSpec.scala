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
import models.{CheckMode, NormalMode, ReturnPeriod}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.correctReturn.SelectView
import models.correctReturn.Select
import views.ViewSpecHelper
class SelectViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[SelectView]
  val formProvider = new SelectFormProvider
  val form = formProvider.apply()
  val returnsList: List[List[ReturnPeriod]] = List(returnPeriodList)
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios"
    val radiosInput = "govuk-radios__input"
    val radiosItems = "govuk-radios__item"
    val radiosLables = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val html = view(form, NormalMode, returns = returnsList)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("correctReturn.select" + ".title"))
    }

    "should include a legend with the expected heading (the oldest year within the returns)" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustEqual returnPeriodList.last.year.toString
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected radio buttons" - {
        val radiobuttons = document.getElementsByClass(Selectors.radiosItems)

        "should have same amount of items as returns" in {
          radiobuttons.size() mustBe returnPeriodList.size
        }
        Select.values.zipWithIndex.foreach { case (radio, index) =>
          s"that has the " + radio.toString + " to select and is unchecked" in {
            if(index == 0) {
              val radio1 = radiobuttons
                .get(index)
              radio1
                .getElementsByClass(Selectors.radiosLables)
                .text() mustBe Messages("January to March 2020")
              println(radio1)
              val input = radio1
                .getElementsByClass(Selectors.radiosInput)
              input.attr("value") mustBe "ReturnPeriod(2020,0)"
              input.hasAttr("checked") mustBe false
            }
            if(index == 1) {
              val radio1 = radiobuttons
                .get(index)
              radio1
                .getElementsByClass(Selectors.radiosLables)
                .text() mustBe Messages("April to June 2020")
              println(radio1)
              val input = radio1
                .getElementsByClass(Selectors.radiosInput)
              input.attr("value") mustBe "ReturnPeriod(2020,1)"
              input.hasAttr("checked") mustBe false
            }
            if(index == 2) {
              val radio1 = radiobuttons
                .get(index)
              radio1
                .getElementsByClass(Selectors.radiosLables)
                .text() mustBe Messages("August to September 2020")
              println(radio1)
              val input = radio1
                .getElementsByClass(Selectors.radiosInput)
              input.attr("value") mustBe "ReturnPeriod(2020,2)"
              input.hasAttr("checked") mustBe false
            }
            if(index == 3) {
              val radio1 = radiobuttons
                .get(index)
              radio1
                .getElementsByClass(Selectors.radiosLables)
                .text() mustBe Messages("October to December 2020")
              println(radio1)
              val input = radio1
                .getElementsByClass(Selectors.radiosInput)
              input.attr("value") mustBe "ReturnPeriod(2020,3)"
              input.hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    Select.values.foreach { radio =>
      val html1 = view(form.fill(radio), NormalMode, returns = returnsList)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + radio.toString + "selected and has no errors" - {
        "should have radiobuttons" - {
          val radiobuttons = document1.getElementsByClass(Selectors.radiosItems)
          Select.values.zipWithIndex.foreach { case (radio1, index) =>
            if (radio1.toString == radio.toString) {
              s"that has the option to select" + radio1.toString + " and is checked" in {
                if(index == 1){
                  val radiobuttons1 = radiobuttons
                    .get(index)
                  radiobuttons1
                    .getElementsByClass(Selectors.radiosLables)
                    .text() mustBe Messages("April to June 2020")
                  val input = radiobuttons1
                    .getElementsByClass(Selectors.radiosInput)
                  input.attr("value") mustBe "ReturnPeriod(2020,1)"
                  input.hasAttr("checked") mustBe false
                }
                if(index == 2){
                  val radiobuttons1 = radiobuttons
                    .get(index)
                  radiobuttons1
                    .getElementsByClass(Selectors.radiosLables)
                    .text() mustBe Messages("April to June 2020")
                  val input = radiobuttons1
                    .getElementsByClass(Selectors.radiosInput)
                  input.attr("value") mustBe "ReturnPeriod(2020,2)"
                  input.hasAttr("checked") mustBe true
                }
              }
            } else {
              s"that has the option to select " + radio1.toString + " and is unchecked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                if(index == 0) {
                  radiobuttons1
                    .getElementsByClass(Selectors.radiosLables)
                    .text() mustBe Messages("January to March 2020")
                  val input = radiobuttons1
                    .getElementsByClass(Selectors.radiosInput)
                  input.attr("value") mustBe "ReturnPeriod(2020,0)"
                  input.hasAttr("checked") mustBe false
                }
                if(index == 1) {
                  radiobuttons1
                    .getElementsByClass(Selectors.radiosLables)
                    .text() mustBe Messages("April to June 2020")
                  val input = radiobuttons1
                    .getElementsByClass(Selectors.radiosInput)
                  input.attr("value") mustBe "ReturnPeriod(2020,1)"
                  input.hasAttr("checked") mustBe false
                }
              }
            }
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(Select.values.head), CheckMode, returns = returnsList)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.SelectController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(Select.values.head), NormalMode, returns = returnsList)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.SelectController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, returns = returnsList)(request, messages(application))
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
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe Messages("correctReturn.select.error.required")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
