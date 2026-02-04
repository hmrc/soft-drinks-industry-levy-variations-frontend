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

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.$packageName$.$className$View
import views.ViewSpecHelper

class $className$ViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[$className$View]
  implicit val request: Request[?] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val panel = "govuk-panel govuk-panel--confirmation"
    val panel_title = "govuk-panel__title"
    val panel_body = "govuk-panel__body"
    val bodyM = "govuk-body-m"
    val body = "govuk-body"
    val link = "govuk-link"
    val details = "govuk-details"
    val detailsText = "govuk-details__summary-text"
    val detailsContent = "govuk-details__text"
  }

  "View" - {
    val html = view(emptyUserAnswersFor$packageName;format="cap"$)(using request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
    }

    "should include the expected panel" in {
      val panel = document.getElementsByClass(Selectors.panel).get(0)
      panel.getElementsByClass(Selectors.panel_title).text() mustEqual Messages("$packageName$.$className;format="decap"$" + ".title")
      panel.getElementsByClass(Selectors.panel_body).text() mustEqual Messages("$packageName$.$className;format="decap"$" + ".panel.message")
    }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      printPageElements.className() mustBe Selectors.bodyM
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual Messages("site.print")
      link.attr("data-module") mustEqual "hmrc-print-link"
    }

    "should include a what happens next section" - {
      "that has the expected subheading" in {
        val subHeading = document.getElementById("whatNextHeader")
        subHeading.text() mustEqual Messages("site.whatNext")
      }
      "that has the expected body" in {
        val body = document.getElementById("whatNextText")
        body.text() mustEqual Messages("$packageName$.$className;format="decap"$.whatNextText")
      }
    }

    "should include a details section" - {
      val details = document.getElementsByClass(Selectors.details).get(0)
      "that has the expected details summary text" in {
        details.getElementsByClass(Selectors.detailsText).text() mustEqual Messages("$packageName$.$className;format="decap"$.detailsSummary")
      }

      "that has the expected content" in {
        details.getElementsByClass(Selectors.detailsContent).text() mustEqual "reference file here that is shared with checkAnswers for this flow"
      }
    }

    testNoBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
