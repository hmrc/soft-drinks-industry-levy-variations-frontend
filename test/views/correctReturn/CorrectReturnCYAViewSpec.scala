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

import models.Amounts
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.ViewSpecHelper
import views.html.correctReturn.CorrectReturnCYAView


class CorrectReturnCYAViewSpec extends ViewSpecHelper {

  val view: CorrectReturnCYAView = application.injector.instanceOf[CorrectReturnCYAView]
  implicit val request: Request[_] = FakeRequest()
  val summaryAmounts: Amounts = Amounts(1000, -100, 500, -600, -400)
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
    val call = Call("GET","/foo")

    val orgName: String = " " + aSubscription.orgName
    val html: HtmlFormat.Appendable = view(orgName, summaryAmounts, summaryList, call)(request, messages(application))
    val document: Document = doc(html)

    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "Check your answers before sending your update"
    }

    "should have the expected post header caption" in {
      document.getElementsByClass(Selectors.body).get(0).text() mustEqual "This update is for Super Lemonade Plc"
    }

    "should have the expected inset text" in {
      document.getElementsByClass(Selectors.insetText).get(0).text() mustEqual "Your Soft Drinks Levy Account will be credited £600.00"
    }

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contain the correct summary lists" in {
      document.getElementsByClass(Selectors.summaryListHeading).get(0).text() mustBe "foo"
      document.getElementsByClass(Selectors.summaryList)
        .first()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bar"
      document.getElementsByClass(Selectors.summaryListHeading).get(1).text() mustBe "wizz"
      document.getElementsByClass(Selectors.summaryList)
        .last()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bang"
    }

    "should have the expected inset rounding help text" in {
      document.getElementsByClass(Selectors.insetText).get(1).text() mustEqual Messages("checkYourAnswers.roundingHelpText")
    }

    "contains a form with the correct action" in {
      document.select(Selectors.form).attr("action") mustEqual call.url
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)

  }
}
