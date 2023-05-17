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

package views.$packageName$

import controllers.routes
import forms.$packageName$.$className$FormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.$packageName$.$className$View

import models.$packageName$.$className$

class $className$ViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[$className$View]
  val formProvider = new $className$FormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val checkboxes = "govuk-checkboxes"
    val checkboxesInput = "govuk-checkboxes__input"
    val checkboxesItems = "govuk-checkboxes__item"
    val checkboxesLables = "govuk-label govuk-checkboxes__label"
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
      document.title() must include(Messages("$className;format="decap"$" + ".title"))
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustEqual Messages("$className;format="decap"$" + ".heading")
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected check boxes" - {
        val checkboxes = document.getElementsByClass(Selectors.checkboxesItems)

        "that has 2 items" in {
          checkboxes.size() mustBe $className$.values.size
        }
        $className$.values.zipWithIndex.foreach { case (checkbox, index) =>
          s"that has the " + checkbox.toString + " to select and is unchecked" in {
            val checkbox1 = checkboxes
              .get(index)
            checkbox1
              .getElementsByClass(Selectors.checkboxesLables)
              .text() mustBe Messages("$className;format="decap"$." + checkbox.toString)
            val input = checkbox1
              .getElementsByClass(Selectors.checkboxesInput)
            input.attr("value") mustBe checkbox.toString
            input.hasAttr("checked") mustBe false
          }
        }
      }
    }

    $className$.values.foreach { checkbox =>
      val html1 = view(form.fill(Set(checkbox)), NormalMode)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + checkbox.toString + "selected and has no errors" - {
        "should have checkboxes" - {
          val checkboxes = document1.getElementsByClass(Selectors.checkboxesItems)
          $className$.values.zipWithIndex.foreach { case (checkbox1, index) =>
            if (checkbox1.toString == checkbox.toString) {
              s"that has the option to select" + checkbox1.toString + " and is checked" in {
                val checkboxes1 = checkboxes
                  .get(index)
                checkboxes1
                  .getElementsByClass(Selectors.checkboxesLables)
                  .text() mustBe Messages("$className;format="decap"$." + checkbox1.toString)
                val input = checkboxes1
                  .getElementsByClass(Selectors.checkboxesInput)
                input.attr("value") mustBe checkbox1.toString
                input.hasAttr("checked") mustBe true
              }
            } else {
              s"that has the option to select " + checkbox1.toString + " and is unchecked" in {
                val checkboxes1 = checkboxes
                  .get(index)
                checkboxes1
                  .getElementsByClass(Selectors.checkboxesLables)
                  .text() mustBe Messages("$className;format="decap"$." + checkbox1.toString)
                val input = checkboxes1
                  .getElementsByClass(Selectors.checkboxesInput)
                input.attr("value") mustBe checkbox1.toString
                input.hasAttr("checked") mustBe false
              }
            }
          }
        }
      }
    }

    s"when the form is preoccupied with all checkboxes selected and has no errors" - {
      val htmlAllSelected = view(form.fill($className$.values.toSet), CheckMode)(request, messages(application))
      val documentAllSelected = doc(htmlAllSelected)
      "should have checkboxes" - {
        val checkboxes = documentAllSelected.getElementsByClass(Selectors.checkboxesItems)
        $className$.values.zipWithIndex.foreach { case (checkbox1, index) =>
            s"that has the option to select " + checkbox1.toString + " and is checked" in {
              val checkboxes1 = checkboxes
                .get(index)
              val input = checkboxes1
                .getElementsByClass(Selectors.checkboxesInput)

              input.attr("value") mustBe checkbox1.toString
              input.hasAttr("checked") mustBe true
            }
          }
        }
      }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill($className$.values.toSet), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill($className$.values.toSet), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("$className;format="decap"$.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe Messages("$className;format="decap"$.error.required")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
