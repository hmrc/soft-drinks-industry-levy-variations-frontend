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

package views.changeActivity

import controllers.changeActivity.routes
import forms.changeActivity.AmountProducedFormProvider
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.changeActivity.AmountProducedView
import models.changeActivity.AmountProduced
import play.api.data.Form
import views.ViewSpecHelper
class AmountProducedViewSpec extends ViewSpecHelper {

  val view: AmountProducedView = application.injector.instanceOf[AmountProducedView]
  val formProvider = new AmountProducedFormProvider
  val form: Form[AmountProduced] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val radios = "govuk-radios"
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
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include("How many litres of your own brands of liable drinks have been packaged globally in the past 12 months?")
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.heading)
        .text() mustEqual "How many litres of your own brands of liable drinks have been packaged globally in the past 12 months?"
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected radio buttons" - {
        val radiobuttons = document.getElementsByClass(Selectors.radiosItems)

        "that has 3 items" in {
          radiobuttons.size() mustBe 3
        }
        AmountProduced.values.zipWithIndex.foreach { case (radio, index) =>
          s"that has the " + radio.toString + " to select and is unchecked" in {
            val radio1 = radiobuttons
              .get(index)
            radio1
              .getElementsByClass(Selectors.radiosLabels)
              .text() mustBe Messages("changeActivity.amountProduced." + radio.toString)
            val input = radio1
              .getElementsByClass(Selectors.radiosInput)
            input.attr("value") mustBe radio.toString
            input.hasAttr("checked") mustBe false
          }
        }
      }
    }

    AmountProduced.values.foreach { radio =>
      val html1 = view(form.fill(radio), NormalMode)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + radio.toString + "selected and has no errors" - {
        "should have radiobuttons" - {
          val radiobuttons = document1.getElementsByClass(Selectors.radiosItems)
          AmountProduced.values.zipWithIndex.foreach { case (radio1, index) =>
            if (radio1.toString == radio.toString) {
              s"that has the option to select" + radio1.toString + " and is checked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("changeActivity.amountProduced." + radio1.toString)
                val input = radiobuttons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe true
              }
            } else {
              s"that has the option to select " + radio1.toString + " and is unchecked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("changeActivity.amountProduced." + radio1.toString)
                val input = radiobuttons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe false
              }
            }
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(AmountProduced.values.head), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.AmountProducedController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(AmountProduced.values.head), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.AmountProducedController.onSubmit(NormalMode).url
      }
    }

    "should include the What does packaged mean detail content" in {
      val details = document.getElementsByClass("govuk-details")
      details.get(0).getElementsByClass("govuk-details__summary")
        .text() mustEqual "What does packaged mean?"
      details.get(0).getElementsByClass("govuk-details__text")
        .text() must include("A liable drink is packaged if it is bottled, canned, or otherwise packaged, so it is ready to either drink or dilute.")
    }
    "should include the expected What is a liable drink detail content" in {
      val details = document.getElementsByClass("govuk-details")
      details.get(1).getElementsByClass("govuk-details__summary")
        .text() mustEqual "What is a liable drink?"
      details.get(1).getElementsByClass("govuk-body")
        .text() mustEqual "A drink is liable if it meets all of the following conditions"
      details.get(1).getElementsByClass("govuk-details__text")
        .text() must include("it has a content of 1.2% alcohol by volume or less")
    }
    "What is a liable drink detail content should have 5 bullet points" in {
      val details = document.getElementsByClass("govuk-details")
      val detailBullets = details.get(1).getElementsByClass("govuk-list govuk-list--bullet").get(0)
      detailBullets.childrenSize() mustBe 5
    }

    "should include the expected What drinks are exempt? content" in {
      val details = document.getElementsByClass("govuk-details")
      details.get(2).getElementsByClass("govuk-details__summary")
        .text() mustEqual "What drinks are exempt?"
      details.get(2).getElementsByClass("govuk-body")
        .text() mustEqual "A drink is exempt if it meets one of the following conditions:"
      details.get(2).getElementsByClass("govuk-details__text")
        .text() must include("it is a milk-substitute which contains at least 120 milligrams of calcium per 100ml, for example soya or almond milk")
      val detailBullets = details.get(2).getElementsByClass("govuk-list govuk-list--bullet").get(0)
      detailBullets.childrenSize() mustBe 4
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("changeActivity.amountProduced.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe "Select how many litres of your own brands of liable drinks have been packaged globally in the past 12 months"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
