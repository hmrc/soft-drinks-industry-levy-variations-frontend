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

package views.cancelRegistration

import controllers.cancelRegistration.routes
import forms.cancelRegistration.ReasonFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.cancelRegistration.ReasonView

import scala.util.Random
import views.ViewSpecHelper

class ReasonViewSpec extends ViewSpecHelper {

  val view: ReasonView = application.injector.instanceOf[ReasonView]
  val formProvider = new ReasonFormProvider
  val form: Form[String] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val formGroup = "govuk-form-group"
    val labelAsHeading = "govuk-label  govuk-label--l"
    val hint = "govuk-hint"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
    val textArea = "govuk-textarea"
  }

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    val formGroup = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK")
    }

    "should contain a govuk form group" - {
      "that contains the page heading" in {
        formGroup.get(0).getElementsByClass(Selectors.labelAsHeading)
          .text() mustBe "Why do you need to cancel your registration?"
      }

      "that contains the expected hint test" in {
        formGroup.get(0).getElementsByClass(Selectors.hint)
          .text() mustBe "Enter a maximum of 255 characters"
      }

      "that contains a text area" in {
        formGroup.get(0).getElementsByClass(Selectors.textArea).size mustBe 1
      }
    }

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill("testing"), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.ReasonController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill("testing"), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.ReasonController.onSubmit(NormalMode).url
      }
    }

    "when a form error exists" - {
      val valueOutOfMaxRange = Random.nextString(255 + 1)

      val htmlWithErrors = view(form.bind(Map("value" -> valueOutOfMaxRange)), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)
      "should have a title containing error" in {
        val titleMessage = Messages("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "The reason you are cancelling your registration must be 255 characters or less"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
