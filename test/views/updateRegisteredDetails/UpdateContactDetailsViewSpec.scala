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
import forms.updateRegisteredDetails.UpdateContactDetailsFormProvider
import models.updateRegisteredDetails.ContactDetails
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.updateRegisteredDetails.UpdateContactDetailsView



class UpdateContactDetailsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[UpdateContactDetailsView]
  val formProvider = new UpdateContactDetailsFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-m"
    val formGroup = "govuk-form-group"
    val label = "govuk-label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val contactdetails: ContactDetails = ContactDetails("Full Name", "job position", "012345678901", "email@test.com")
  val contactdetailsJsObject: collection.Map[String, JsValue] = Json.toJson(contactdetails).as[JsObject].value
  val contactdetailsMap: Map[String, String] = Map("fullName" -> "Full Name",
    "position" -> "job position",
  "phoneNumber" -> "012345678901",
  "email" -> "email@test.com")

  val fieldNameToLabel: Map[String, String] = Map("fullName" -> "Full name",
    "position" -> "Job title",
    "phoneNumber" -> "Telephone number",
    "email" -> "Email address")

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    val questionItems = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include(Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("updateRegisteredDetails.updateContactDetails" + ".heading")
    }

    "should contain" + contactdetailsMap.size + " questions" in {
      questionItems.size() mustBe contactdetailsMap.size
    }

    contactdetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>

      "when the form is not prepopulated and has no errors" - {
        "should include the expected question fields" - {

          "that has the field " + fieldNameToLabel(fieldName) in {
            val questionItem1 = questionItems
              .get(index)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe fieldNameToLabel(fieldName)
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(contactdetails), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.UpdateContactDetailsController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(contactdetails), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.UpdateContactDetailsController.onSubmit(NormalMode).url
      }
    }


    contactdetailsMap.foreach { case (fieldName, fieldValue) =>
      val fieldWithError: Map[String, String] = contactdetailsMap.map{
        case(k,v) => if(k == fieldName) {(k -> "")} else {(k -> v)}
      }

      val fieldWithFormatError: Map[String, String] = contactdetailsMap.map {
        case (k, v) => if (k == fieldName) {
          (k -> "****%%%%%**(())(")
        } else {
          (k -> v)
        }
      }
      val htmlWithErrors = view(form.bind(fieldWithError), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      val htmlWithInvalidErrors = view(form.bind(fieldWithFormatError), NormalMode)(request, messages(application))
      val documentWithInvalidErrors = doc(htmlWithInvalidErrors)

      "when " + fieldNameToLabel(fieldName) + " is empty" - {
        "should have a title containing error" in {
          val titleMessage = Messages("updateRegisteredDetails.updateContactDetails.title")
          documentWithErrors.title must include("Error: " + titleMessage)
        }

        "contains a message that links to field with error" in {
          val errorSummary = documentWithErrors
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#" + fieldName
          errorSummary.text() must include(Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".required"))
        }
      }

      "when " + fieldNameToLabel(fieldName) + " is formatted wrong" - {
        "should have a title containing error" in {
          val titleMessage = Messages("updateRegisteredDetails.updateContactDetails.title")
          documentWithInvalidErrors.title must include("Error: " + titleMessage)
        }

        "contains a message that links to field with error" in {
          val errorSummary = documentWithInvalidErrors
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#" + fieldName
          errorSummary.text() mustBe Jsoup.parse(Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".invalid")).text()
        }
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
