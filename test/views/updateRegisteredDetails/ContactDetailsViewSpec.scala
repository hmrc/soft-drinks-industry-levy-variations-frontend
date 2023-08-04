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

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.updateRegisteredDetails.ContactDetailsView
import views.ViewSpecHelper

class ContactDetailsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[ContactDetailsView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val html = view(SummaryList())(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustEqual "Change person's details - Soft Drinks Industry Levy - GOV.UK"
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual "Change person's details"
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
