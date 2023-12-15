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

import config.FrontendAppConfig
import models.{CheckMode, NormalMode}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.LitresSpecHelper
import views.html.changeActivity.HowManyImportsView
class HowManyImportsViewSpec extends LitresSpecHelper {

  val howManyImportsView: HowManyImportsView = application.injector.instanceOf[HowManyImportsView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyImportsView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = howManyImportsView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyImportsView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyImportsView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyImportsView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyImportsView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyImportsView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyImportsView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustBe
            "How many litres will you bring into the UK in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe
            "How many litres will you bring into the UK in the next 12 months?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe
          "Do not include liable drinks from small producers or your own brands if you are a registered small producer."
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)


        "and the form has errors" - {
          val errorTitle = "Error: How many litres will you bring into the UK in the next 12 months?"
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
