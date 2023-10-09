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

package views.FileReturnBeforeDereg

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.cancelRegistration.FileReturnBeforeDeregView
import views.ViewSpecHelper

class FileReturnBeforeDeregViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[FileReturnBeforeDeregView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val html = view(Html(""))(request, messages(application), frontendAppConfig)
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("You cannot cancel your registration while you have returns to send - Soft Drinks Industry Levy - GOV.UK"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("You cannot cancel your registration while you have returns to send")
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
