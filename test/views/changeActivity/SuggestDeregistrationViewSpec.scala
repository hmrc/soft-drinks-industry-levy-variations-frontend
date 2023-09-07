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

import controllers.changeActivity.routes
import models.NormalMode
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.ViewSpecHelper
import views.html.changeActivity.SuggestDeregistrationView

class SuggestDeregistrationViewSpec extends ViewSpecHelper {

  val view: SuggestDeregistrationView = application.injector.instanceOf[SuggestDeregistrationView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val body = "govuk-body"
    val button = "govuk-button"
  }

  "View" - {
    val html = view()(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustBe "You need to cancel your Soft Drinks Industry Levy registration - Soft Drinks Industry Levy - GOV.UK"
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustBe "You need to cancel your Soft Drinks Industry Levy registration"
    }

    "should have the expected cancel registration button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Cancel your registration"
    }

    "should include the deregistration information content" in {
      document.getElementsByClass(Selectors.body).text() mustBe
        "Based on your answers, you are a small producer who does not use a third party to package liable drinks. If you do not think this is right you need to go back and change your answers."
    }

    "should include a link to the Amount Produced page" in {
      val route: String = routes.AmountProducedController.onSubmit(NormalMode).url
      document.getElementsByClass(Selectors.body).select("a").attr("href") mustBe route
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
