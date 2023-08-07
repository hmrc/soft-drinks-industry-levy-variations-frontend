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

import models.backend.UkAddress
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.updateRegisteredDetails.BusinessAddressView

class BusinessAddressViewSpec extends ViewSpecHelper {

  val view: BusinessAddressView = application.injector.instanceOf[BusinessAddressView]
  implicit val request: Request[_] = FakeRequest()
  lazy val businessAddress: UkAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")

  object Selectors {
    val heading = "govuk-heading-l"
    val button = "govuk-button"
    val summaryListActions = "govuk-summary-list__actions"
  }

  "View" - {
    val html = view(List(businessAddress))(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "Your business address for the Soft Drinks Industry Levy - Soft Drinks Industry Levy - GOV.UK"
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustBe "Your business address for the Soft Drinks Industry Levy"
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "View should contain the correct heading and summary row details" in {
      val html1 = view(List(businessAddress))(request, messages(application))
      val document1 = doc(html1)
      val listItems = document1.getElementsByClass(Selectors.summaryListActions)
      val summaryListKey = document1.getElementsByClass("govuk-summary-list__key")

      summaryListKey.size mustBe 1
      listItems.get(0).text() mustBe "Change business address"
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
