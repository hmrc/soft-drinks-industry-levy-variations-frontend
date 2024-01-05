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

import org.jsoup.nodes.Document
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.ViewSpecHelper
import views.html.changeActivity.ChangeActivitySentView

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class ChangeActivitySentViewSpec extends ViewSpecHelper {

  val view: ChangeActivitySentView = application.injector.instanceOf[ChangeActivitySentView]
  implicit val request: Request[_] = FakeRequest()

  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)

  object Selectors {
    val heading = "govuk-heading-l"
    val panel = "govuk-panel govuk-panel--confirmation"
    val panel_title = "govuk-panel__title"
    val panel_body = "govuk-panel__body"
    val body = "govuk-body"
    val link = "govuk-link"
    val details = "govuk-details"
    val detailsText = "govuk-details__summary-text"
    val summaryListHeading = "govuk-heading-m"
    val button = "govuk-button"
    val summaryList = "govuk-summary-list"
    val summaryRow = "govuk-summary-list__row"
    val summaryValue = "govuk-summary-list__value"
    val detailsContent = "govuk-details__text"
  }

  "View" - {
    val summaryList: Seq[(String, SummaryList)] = {
      Seq(
        "foo" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bar"))))),
        "wizz" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bang")))))
      )
    }

    val orgName: String = " " + aSubscription.orgName
    val html: HtmlFormat.Appendable = view(orgName, formattedDate, formattedTime, summaryList)(request, messages(application), frontendAppConfig)
    val document: Document = doc(html)

    "should contain the expected title" in {
      document.title() mustBe "Update sent - Soft Drinks Industry Levy - GOV.UK"
    }

    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "Update sent"
    }

    "should include the expected panel" in {
    val panel = document.getElementsByClass(Selectors.panel).get(0)
    panel.getElementsByClass(Selectors.panel_title).text() mustEqual "Update sent"
    panel.getElementsByClass(Selectors.panel_body).text() mustEqual "We have received the update to your Soft Drinks Industry Levy details"
  }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual "Print this page"
      link.attr("href") mustEqual "#print-dialogue"
    }

    "should have the expected h2 caption" in {
      document.getElementsByTag("h2").first().text() mustEqual "What happens next"
    }

    "The what happens next section should have the expected body" in {
      val whatNextBody = document.getElementById("whatNextBody")
      whatNextBody.text() mustEqual "You do not need to do anything else. We will update your details with the information you have provided."
    }

    "should include a details section" - {
      val details = document.getElementsByClass(Selectors.details).get(0)
      "that has the expected details summary text" in {
        details.getElementsByClass(Selectors.detailsText).text() mustEqual "View the details of your update"
      }
    }

    "that has the expected content" in {
      document.getElementsByClass(Selectors.summaryListHeading).get(1).text() mustBe "foo"
      document.getElementsByClass(Selectors.summaryList)
        .first()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bar"
      document.getElementsByClass(Selectors.summaryListHeading).get(2).text() mustBe "wizz"
      document.getElementsByClass(Selectors.summaryList)
        .last()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bang"
    }

    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
