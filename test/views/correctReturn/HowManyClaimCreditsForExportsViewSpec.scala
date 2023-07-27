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

import config.FrontendAppConfig
import controllers.correctReturn.routes
import models.{CheckMode, NormalMode}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.LitresSpecHelper
import views.html.correctReturn.HowManyClaimCreditsForExportsView
class HowManyClaimCreditsForExportsViewSpec extends LitresSpecHelper {

  val howManyClaimCreditsForExportsView: HowManyClaimCreditsForExportsView = application.injector.instanceOf[HowManyClaimCreditsForExportsView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyClaimCreditsForExportsView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyClaimCreditsForExportsView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustEqual "How many credits do you want to claim for liable drinks that have been exported? - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe "How many credits do you want to claim for liable drinks that have been exported?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe "You can only claim a levy credit for drinks that you have paid the levy on or will pay the levy on. Do not include drinks produced for small producers or imported from them. You can claim a credit for liable drinks that have been, or you expect to be, exported by you or someone else. You will need to get and keep evidence of details such as the: If you do not have the evidence by the end of the quarter after you reported the liable drinks as exported, you must add the levy credit back in your next return."
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)

        val expectedDetails = Map(
          "What can I claim a credit for?" -> "You can claim a credit for liable drinks that have been, or you expect to be, exported by you or someone else. You will need to get and keep evidence of details such as the: brand of the liable drinks supplier or consigner customer and destination the liable drinks are supplied to method of delivery If you do not have the evidence by the end of the quarter after you reported the liable drinks as exported, you must add the levy credit back in your next return.")
        testDetails(document, expectedDetails)
        testButton(document)
        testAction(document, routes.HowManyClaimCreditsForExportsController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: How many credits do you want to claim for liable drinks that have been exported? - Soft Drinks Industry Levy - GOV.UK"
          testEmptyFormErrors(documentFormErrorsEmpty, errorTitle)
          testNoNumericFormErrors(documentFormErrorsNoneNumeric, errorTitle)
          testNegativeFormErrors(documentFormErrorsNegative, errorTitle)
          testDecimalFormErrors(documentFormErrorsNotWhole, errorTitle)
          testOutOfMaxValFormErrors(documentFormErrorsOutOfRange, errorTitle)
        }
      }
    }
  }
}
