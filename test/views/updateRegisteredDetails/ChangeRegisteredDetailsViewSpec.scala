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

package views.updateRegisteredDetails

import controllers.updateRegisteredDetails.routes
import forms.updateRegisteredDetails.ChangeRegisteredDetailsFormProvider
import models.updateRegisteredDetails.ChangeRegisteredDetails
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.updateRegisteredDetails.ChangeRegisteredDetailsView

class ChangeRegisteredDetailsViewSpec extends ViewSpecHelper {

  val view: ChangeRegisteredDetailsView = application.injector.instanceOf[ChangeRegisteredDetailsView]
  val formProvider = new ChangeRegisteredDetailsFormProvider
  val isVoluntary: Boolean = false
  val form: Form[Set[ChangeRegisteredDetails]] = formProvider.apply(isVoluntary)
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val checkboxes = "govuk-checkboxes"
    val checkboxesInput = "govuk-checkboxes__input"
    val checkboxesItems = "govuk-checkboxes__item"
    val checkboxesLabels = "govuk-label govuk-checkboxes__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "Change Registered Details View" - {
    val html = view(form, isVoluntary)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustBe "What do you need to update?"
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected check boxes" - {
        val checkboxes = document.getElementsByClass(Selectors.checkboxesItems)

        "that has 3 items" in {
          checkboxes.size() mustBe 3
        }
        ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkbox, index) =>
          s"that has the " + checkbox.toString + " to select and is unchecked" in {
            val checkbox1 = checkboxes
              .get(index)
            checkbox1
              .getElementsByClass(Selectors.checkboxesLabels)
              .text() mustBe Messages("updateRegisteredDetails.changeRegisteredDetails." + checkbox.toString)
            val input = checkbox1
              .getElementsByClass(Selectors.checkboxesInput)
            input.attr("value") mustBe checkbox.toString
            input.hasAttr("checked") mustBe false
          }
        }
      }
    }

    ChangeRegisteredDetails.values.foreach { checkbox =>
      val html1 = view(form.fill(Set(checkbox)), isVoluntary)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + checkbox.toString + "selected and has no errors" - {
        "should have checkboxes" - {
          val checkboxes = document1.getElementsByClass(Selectors.checkboxesItems)
          ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkbox1, index) =>
            if (checkbox1.toString == checkbox.toString) {
              s"that has the option to select" + checkbox1.toString + " and is checked" in {
                val checkboxes1 = checkboxes
                  .get(index)
                checkboxes1
                  .getElementsByClass(Selectors.checkboxesLabels)
                  .text() mustBe Messages("updateRegisteredDetails.changeRegisteredDetails." + checkbox1.toString)
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
                  .getElementsByClass(Selectors.checkboxesLabels)
                  .text() mustBe Messages("updateRegisteredDetails.changeRegisteredDetails." + checkbox1.toString)
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
      val htmlAllSelected = view(form.fill(ChangeRegisteredDetails.values.toSet), isVoluntary)(request, messages(application))
      val documentAllSelected = doc(htmlAllSelected)
      "should have checkboxes" - {
        val checkboxes = documentAllSelected.getElementsByClass(Selectors.checkboxesItems)
        ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkbox1, index) =>
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
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(ChangeRegisteredDetails.values.toSet), isVoluntary)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.ChangeRegisteredDetailsController.onSubmit().url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), isVoluntary)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe "Select at least one option to continue"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)

  }

  "Change Registered Details View when user has the activity status of voluntary registered = true" - {
    val html = view(form, isVoluntary = true)(request, messages(application))
    val document = doc(html)

    "should include the expected check boxes" - {
      val checkboxes = document.getElementsByClass(Selectors.checkboxesItems)

      "that has 2 items" in {
        checkboxes.size() mustBe 2
      }
      ChangeRegisteredDetails.voluntaryValues.zipWithIndex.foreach { case (checkbox, index) =>
        s"that has the " + checkbox.toString + " to select and is unchecked" in {
          val checkbox1 = checkboxes
            .get(index)
          checkbox1
            .getElementsByClass(Selectors.checkboxesLabels)
            .text() mustBe Messages("updateRegisteredDetails.changeRegisteredDetails." + checkbox.toString)
          val input = checkbox1
            .getElementsByClass(Selectors.checkboxesInput)
          input.attr("value") mustBe checkbox.toString
          input.hasAttr("checked") mustBe false
        }
      }
    }
  }

}
