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

package views.$packageName$

import config.FrontendAppConfig
import controllers.$packageName$.routes
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.$packageName$.HowMany$className$View
import views.LitresSpecHelper
class HowMany$className$ViewSpec extends LitresSpecHelper {

  val howMany$className$View: HowMany$className$View = application.injector.instanceOf[HowMany$className$View]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowMany$className$View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = howMany$className$View(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howMany$className$View(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howMany$className$View(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howMany$className$View(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howMany$className$View(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howMany$className$View(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howMany$className$View(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustEqual "$litresTitle$ - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe "$litresHeading$"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe "$subText$"
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)

        val expectedDetails = Map(
          "$detailsLinkText$" -> "$detailsContent$")
        testDetails(document, expectedDetails)
        testButton(document)
        testAction(document, routes.HowMany$className$Controller.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: $litresTitle$ - Soft Drinks Industry Levy - GOV.UK"
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
