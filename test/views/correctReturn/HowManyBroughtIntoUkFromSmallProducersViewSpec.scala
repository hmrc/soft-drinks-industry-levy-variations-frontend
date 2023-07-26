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
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.correctReturn.HowManyBroughtIntoUkFromSmallProducersView
import views.LitresSpecHelper
class HowManyBroughtIntoUkFromSmallProducersViewSpec extends LitresSpecHelper {

  val howManyBroughtIntoUkFromSmallProducersView: HowManyBroughtIntoUkFromSmallProducersView = application.injector.instanceOf[HowManyBroughtIntoUkFromSmallProducersView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyBroughtIntoUkFromSmallProducersView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyBroughtIntoUkFromSmallProducersView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustEqual "How many litres of liable drinks have you brought into the UK from small producers? - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe "How many litres of liable drinks have you brought into the UK from small producers?"
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe "Include your own brands of liable drinks produced outside of the UK. If you are a registered small producer and you bring your own brand of liable drinks into the UK, you still need to report them but you will not pay the levy on them. If you bring liable drinks into the UK from someone else who would be considered a small producer, you need to get evidence of the:"
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)

        val expectedDetails = Map(
          "Liable drinks from small producers" -> "If you are a registered small producer and you bring your own brand of liable drinks into the UK, you still need to report them but you will not pay the levy on them. If you bring liable drinks into the UK from someone else who would be considered a small producer, you need to get evidence of the: contact details, EU VAT number (if they have one) and website of the business amount of litres of liable drinks packaged globally for brands the business owns in the past 12 months signature of someone from the business, their position and the date of the signature")

        testDetails(document, expectedDetails)
        testButton(document)
        testAction(document, routes.HowManyBroughtIntoUkFromSmallProducersController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: How many litres of liable drinks have you brought into the UK from small producers? - Soft Drinks Industry Levy - GOV.UK"
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
