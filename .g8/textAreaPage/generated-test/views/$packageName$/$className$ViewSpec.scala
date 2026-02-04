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

import controllers.$packageName$.routes
import forms.$packageName$.$className$FormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.$packageName$.$className$View
import scala.util.Random
import views.ViewSpecHelper

class $className$ViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[$className$View]
  val formProvider = new $className$FormProvider
  val form = formProvider.apply()
  implicit val request: Request[?] = FakeRequest()

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
    val html = view(form, NormalMode)(using request, messages(application))
    val document = doc(html)
    val formGroup = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
    }

    "should contain a govuk form group" - {
      "that contains the page heading" in {
        formGroup.get(0).getElementsByClass(Selectors.labelAsHeading)
          .text() mustBe Messages("$packageName$.$className;format="decap"$.heading")
      }

      "that contains the expected hint test" in {
        formGroup.get(0).getElementsByClass(Selectors.hint)
          .text() mustBe Messages("$packageName$.$className;format="decap"$.hint")
      }

      "that contains a text area" in {
        formGroup.get(0).getElementsByClass(Selectors.textArea).size mustBe 1
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill("testing"), CheckMode)(using request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill("testing"), NormalMode)(using request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(NormalMode).url
      }
    }

    "when a form error exists" - {
      val valueOutOfMaxRange = Random.nextString($maxLength$ + 1)

      val htmlWithErrors = view(form.bind(Map("value" -> valueOutOfMaxRange)), NormalMode)(using request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)
      "should have a title containing error" in {
        val titleMessage = Messages("$packageName$.$className;format="decap"$.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$.error.length")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
