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
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.correctReturn.HowManyPackagedAsContractPackerView
import views.LitresSpecHelper
class HowManyPackagedAsContractPackerViewSpec extends LitresSpecHelper {

  val howManyPackagedAsContractPackerView: HowManyPackagedAsContractPackerView =
    application.injector.instanceOf[HowManyPackagedAsContractPackerView]

  implicit val request: Request[?] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyPackagedAsContractPackerView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode + " mode" - {
        val html: HtmlFormat.Appendable = howManyPackagedAsContractPackerView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable =
          howManyPackagedAsContractPackerView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyPackagedAsContractPackerView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable =
          howManyPackagedAsContractPackerView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable =
          howManyPackagedAsContractPackerView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable =
          howManyPackagedAsContractPackerView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable =
          howManyPackagedAsContractPackerView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() must include(
            Messages(
              "How many litres of liable drinks have you packaged as a third party or contract packer at UK sites you operate?"
            )
          )
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe Messages(
            "How many litres of liable drinks have you packaged as a third party or contract packer at UK sites you operate?"
          )
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).first().text() mustBe Messages(
            "Do not include liable drinks you have packaged for registered small producers."
          )
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)

        val expectedDetails = Map(
          Messages("What is a small producer?") -> Messages(
            "A business is a small producer if it: has had less than 1 million litres of its own brands of liable drinks packaged globally in the past 12 months will not have more than 1 million litres of its own brands of liable drinks packaged globally in the next 30 days"
          )
        )
        testButton(document)
        testDetails(document, expectedDetails)
        testAction(document, routes.HowManyPackagedAsContractPackerController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: " + Messages(
            "How many litres of liable drinks have you packaged as a third party or contract packer at UK sites you operate?"
          )
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
