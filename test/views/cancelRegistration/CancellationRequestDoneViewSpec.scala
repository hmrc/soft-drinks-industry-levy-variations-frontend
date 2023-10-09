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
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.cancelRegistration.CancellationRequestDoneView

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class CancellationRequestDoneViewSpec extends ViewSpecHelper {

  val view: CancellationRequestDoneView = application.injector.instanceOf[CancellationRequestDoneView]
  implicit val request: Request[_] = FakeRequest()

  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)
  val orgName: String = aSubscription.orgName
  val config: FrontendAppConfig = frontendAppConfig

  object Selectors {
    val heading = "govuk-heading-l"
    val panel = "govuk-panel govuk-panel--confirmation panel-indent"
    val panel_title = "govuk-panel__title"
    val panel_body = "govuk-panel__body"
    val body = "govuk-body"
    val link = "govuk-link"
    val details = "govuk-details"
    val detailsText = "govuk-details__summary-text"
    val detailsContent = "govuk-details__text"
  }

  "View" - {
    val html = view(formattedDate, formattedTime, orgName)(request, messages(application), config)
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "Cancellation request sent - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include the expected panel" in {
      val panel = document.getElementsByClass(Selectors.panel).get(0)
      panel.getElementsByClass(Selectors.panel_title).text() mustEqual "Cancellation request sent"
      panel.getElementsByClass(Selectors.panel_body).text() mustEqual "We have received the update to your Soft Drinks Industry Levy details"
    }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      printPageElements.className() mustBe "js-visible govuk-body govuk-!-display-none-print"
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual "Print this page"
      link.attr("href") mustEqual "#print-dialogue"
    }

    "should include a what happens next section" - {
      "that has the expected subheading" in {
        val subHeading = document.getElementById("whatNextHeader")
        subHeading.text() mustEqual "What happens next"
      }

      "that has the expected body" in {
        val bodyP1 = document.getElementById("whatNextTextP1")
        bodyP1.text() mustEqual "You need to send a final return for the period January 2023 to March 2023 and make any outstanding payments."
        val bodyP2 = document.getElementById("whatNextTextP2")
        bodyP2.text() mustEqual "You will be able to send the final return from 1 April until 30 April 2023."
        val bodyP3 = document.getElementById("whatNextTextP3")
        bodyP3.text() mustEqual "Once you have sent this return and made any outstanding payments then we will process your request to cancel your registration."
        val bodyP4 = document.getElementById("whatNextTextP4")
        bodyP4.text() mustEqual "You do not need to do anything else. We will update your details with the information you have provided. We will send you confirmation that the changes have been made."
      }
    }

    "should include a details section" - {
      val details = document.getElementsByClass(Selectors.details).get(0)
      "that has the expected details summary text" in {
        details.getElementsByClass(Selectors.detailsText).text() mustEqual "View the details of your update"
      }

      "that has the expected content" in {
        details.getElementsByClass(Selectors.detailsContent).text() mustEqual "TODO: IMPLEMENT DETAILS OF YOUR REQUEST"
      }
    }

    testNoBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
