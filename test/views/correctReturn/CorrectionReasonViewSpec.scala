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
import forms.correctReturn.CorrectionReasonFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.correctReturn.CorrectionReasonView

import scala.util.Random

class CorrectionReasonViewSpec extends ViewSpecHelper {

  val view: CorrectionReasonView = application.injector.instanceOf[CorrectionReasonView]
  val formProvider = new CorrectionReasonFormProvider
  val form: Form[String] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val formGroup = "govuk-form-group"
    val labelAsHeading = "govuk-label  govuk-label--m"
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
      document.title() mustBe "Why do you need to correct this return? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should contain a govuk form group" - {

      "that contains the expected hint text" in {
        formGroup.get(0).getElementsByClass(Selectors.hint)
          .text() mustBe
          "Make sure you provide as much detail as you can. We may get in touch with your contact person to discuss this. You can enter up to 255 characters"
      }

      "that contains a text area" in {
        formGroup.get(0).getElementsByClass(Selectors.textArea).size mustBe 1
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill("testing"), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.CorrectionReasonController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill("testing"), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.CorrectionReasonController.onSubmit(NormalMode).url
      }
    }

    "when a form error exists" - {
      val valueOutOfMaxRange = Random.nextString(255 + 1)
      val htmlWithErrors = view(form.bind(Map("value" -> valueOutOfMaxRange)), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)
      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: Why do you need to correct this return? - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe "Reason for correcting the return must be 255 characters or less"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
