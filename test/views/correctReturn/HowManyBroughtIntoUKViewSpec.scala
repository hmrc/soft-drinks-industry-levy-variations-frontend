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
import models.{ CheckMode, NormalMode }
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.correctReturn.HowManyBroughtIntoUKView
import views.LitresSpecHelper
class HowManyBroughtIntoUKViewSpec extends LitresSpecHelper {

  val howManyBroughtIntoUKView: HowManyBroughtIntoUKView = application.injector.instanceOf[HowManyBroughtIntoUKView]

  implicit val request: Request[?] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyBroughtIntoUKView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyBroughtIntoUKView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyBroughtIntoUKView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyBroughtIntoUKView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyBroughtIntoUKView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyBroughtIntoUKView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyBroughtIntoUKView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyBroughtIntoUKView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustEqual "How many litres of liable drinks have you brought into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document
            .getElementsByClass(Selectors.heading)
            .text() mustBe "How many litres of liable drinks have you brought into the UK from anywhere outside of the UK?"
        }

        "should include a govuk body with the expected content" in {
          document
            .getElementsByClass(Selectors.body)
            .first()
            .text() mustBe "Do not include liable drinks from small producers or your own brands if you are a registered small producer."
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)

        val expectedDetailsContent = "A business is a small producer if it: " +
          "has had less than 1 million litres of its own brands of liable drinks packaged globally in the" +
          " past 12 months will not have more than 1 million litres of its own brands of liable drinks packaged globally in the next 30 days"

        val expectedDetails = Map("What is a small producer?" -> expectedDetailsContent)
        testDetails(document, expectedDetails)
        testButton(document)
        testAction(document, routes.HowManyBroughtIntoUKController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle =
            "Error: How many litres of liable drinks have you brought into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
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
