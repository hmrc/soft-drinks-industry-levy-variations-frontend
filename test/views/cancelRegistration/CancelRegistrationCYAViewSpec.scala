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

import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.ViewSpecHelper
import views.html.cancelRegistration.CancelRegistrationCYAView

class CancelRegistrationCYAViewSpec extends ViewSpecHelper {

  val view: CancelRegistrationCYAView = application.injector.instanceOf[CancelRegistrationCYAView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val POST_HEADER_CAPTION = "govuk-body"
    val heading = "govuk-heading-l"
    val button = "govuk-button"
    val summaryList = "govuk-summary-list"
    val summaryRow = "govuk-summary-list__row"
    val summaryValue = "govuk-summary-list__value"
    val form = "form"
  }
  "View" - {
    val summaryList: Seq[(String, SummaryList)] = {
      Seq(
        "foo" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("no longer sell drinks"))))),
        "bar" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bang")))))
      )
    }
    val call = Call("GET","/foo")
    val ALIAS = "ALIAS"
    val html = view(ALIAS, summaryList, call)(request, messages(application))
    val document = doc(html)

    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "Cancel your Soft Drinks Industry Levy registration"
    }

    "should have the expected post header caption" in {
      document.getElementsByClass(Selectors.POST_HEADER_CAPTION).get(0).text() mustEqual (s"Cancellation for" + ALIAS)
    }

    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Confirm cancellation"
    }

    "contain the correct summary lists" in {
      document.getElementsByClass(Selectors.summaryList)
        .first()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "no longer sell drinks"
      document.getElementsByClass(Selectors.summaryList)
        .last()
        .getElementsByClass(Selectors.summaryRow)
        .first()
        .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bang"
    }

    "contains a form with the correct action" in {
      document.select(Selectors.form).attr("action") mustEqual call.url
    }

    "contain a print page link" in {
      document.getElementById("printPage").text() mustBe "Print this page"
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)

  }
}
