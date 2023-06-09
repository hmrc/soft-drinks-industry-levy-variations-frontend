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

import config.FrontendAppConfig
import controllers.cancelRegistration.routes
import forms.cancelRegistration.CancelRegistrationDateFormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.cancelRegistration.CancelRegistrationDateView

import java.time.LocalDate
import views.ViewSpecHelper


class CancelRegistrationDateViewSpec extends ViewSpecHelper {

  val appConfig = application.injector.instanceOf[FrontendAppConfig]

  val view = application.injector.instanceOf[CancelRegistrationDateView]
  val formProvider = new CancelRegistrationDateFormProvider(appConfig)
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val dateItems = "govuk-date-input__item"
    val dateLables = "govuk-label govuk-date-input__label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading).text() mustEqual Messages("cancelRegistration.cancelRegistrationDate" + ".heading")
    }

    "when the form is not prepopulated and has no errors" - {

      "should include the expected date fields" - {
        val dateItems = document.getElementsByClass(Selectors.dateItems)

        "that has 3 items" in {
          dateItems.size() mustBe 3
        }
        s"that has Day field" in {
          val dateItem1 = dateItems
            .get(0)
          dateItem1
            .getElementsByClass(Selectors.dateLables)
            .text() mustBe "Day"
        }
        s"that has Month field" in {
          val dateItem1 = dateItems
            .get(1)
          dateItem1
            .getElementsByClass(Selectors.dateLables)
            .text() mustBe "Month"
        }
        s"that has Year field" in {
          val dateItem1 = dateItems
            .get(2)
          dateItem1
            .getElementsByClass(Selectors.dateLables)
            .text() mustBe "Year"
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(LocalDate.now()), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.CancelRegistrationDateController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(LocalDate.now()), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.CancelRegistrationDateController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("cancelRegistrationDate" -> "")), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("cancelRegistration.cancelRegistrationDate.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#cancelRegistrationDate.day"
        errorSummary.text() mustBe Messages("cancelRegistrationDate.error.required.all")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
