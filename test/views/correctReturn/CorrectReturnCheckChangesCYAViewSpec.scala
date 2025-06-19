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

import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.ViewSpecHelper
import views.html.correctReturn.CorrectReturnCheckChangesCYAView

class CorrectReturnCheckChangesCYAViewSpec extends ViewSpecHelper {

  val view: CorrectReturnCheckChangesCYAView = application.injector.instanceOf[CorrectReturnCheckChangesCYAView]
  implicit val request: Request[_] = FakeRequest()

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
        "wizz" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bang"))))),
        "Balance" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("£1.00")))))
      )
    }
    val call = Call("GET","/foo")

    val orgName: String = " " + aSubscription.orgName
    val html: HtmlFormat.Appendable = view(orgName, summaryList, call)(request, messages(application))
    val document: Document = doc(html)

    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "Check your answers before sending your correction"
    }

    "should have the expected post header caption" in {
      document.getElementsByClass(Selectors.body).get(0).text() mustEqual "This update is for Super Lemonade Plc"
    }

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Confirm details and send correction"
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
        .get(1)
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bang"
    }
    "contains a balance section" in {
      document.getElementsByClass(Selectors.summaryListHeading).get(2).text() mustBe "Balance"
    }

    "should have the expected inset rounding help text" in {
      document.getElementsByClass(Selectors.insetText).get(0).text() mustEqual Messages("checkYourAnswers.roundingHelpText")
    }

    "contain a section before the submit action that contains the correct text" in {
      document.getElementsByClass(Selectors.body).get(1).text() mustBe
      "By sending this correction you are confirming that, to the best of your knowledge, the details you are providing are correct."
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
