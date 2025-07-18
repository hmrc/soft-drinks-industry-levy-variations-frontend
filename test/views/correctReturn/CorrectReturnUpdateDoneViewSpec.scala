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

import models.ReturnPeriod
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.ViewSpecHelper
import views.html.correctReturn.CorrectReturnUpdateDoneView

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

class CorrectReturnUpdateDoneViewSpec extends ViewSpecHelper {

  val view: CorrectReturnUpdateDoneView = application.injector.instanceOf[CorrectReturnUpdateDoneView]
  implicit val request: Request[_] = FakeRequest()

  val getSentDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  val formattedDate: String = getSentDateTime.format(dateFormatter)
  val formattedTime: String = getSentDateTime.format(timeFormatter)

  val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
  val currentReturnPeriod = ReturnPeriod(getSentDateTime.toLocalDate)
  val returnPeriodStart = currentReturnPeriod.start.format(returnPeriodFormat)
  val returnPeriodEnd = currentReturnPeriod.end.format(returnPeriodFormat)

  object Selectors {
    val body = "govuk-body"
    val heading = "govuk-heading-l"
    val insetText = "govuk-inset-text"
    val summaryListHeading = "govuk-heading-m"
    val button = "govuk-button"
    val summaryList = "govuk-summary-list"
    val summaryRow = "govuk-summary-list__row"
    val summaryValue = "govuk-summary-list__value"
    val form = "form"
  }

  "View" - {
    val summaryList: Seq[(String, SummaryList)] = {
      Seq(
        "foo" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bar"))))),
        "wizz" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bang")))))
      )
    }

    val orgName: String = " " + aSubscription.orgName
    val html: HtmlFormat.Appendable = view(orgName, summaryList,
      formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd)(request, messages(application), frontendAppConfig)
    val document: Document = doc(html)

    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "Correction sent"
    }

    "should have the expected post header caption" in {
      document.getElementsByClass(Selectors.body).get(0).text() mustEqual "Print this page"
    }

    "should have the expected h2 caption" in {
      document.getElementsByTag("h2").first().text() mustEqual "What happens next"
    }

    "contain the correct summary lists" in {
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

    "should have the expected inset rounding help text" in {
      document.getElementsByClass(Selectors.insetText).get(0).text() mustEqual Messages("checkYourAnswers.roundingHelpText")
    }

    "contain a section before the submit action that contains the correct text" in {
      document.getElementsByClass(Selectors.body).get(1).text() mustBe
        "We may get in touch with your contact person to discuss your changes. Once we have approved your correction, your account balance will be updated."
    }

    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)

  }
}
