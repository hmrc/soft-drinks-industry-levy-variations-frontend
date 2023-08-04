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
import forms.correctReturn.AddASmallProducerFormProvider
import models.correctReturn.AddASmallProducer
import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.nodes.Document
import pages.correctReturn.SelectPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.ViewSpecHelper
import views.html.correctReturn.AddASmallProducerView

class AddASmallProducerViewSpec extends ViewSpecHelper {

  val addASmallProducerView: AddASmallProducerView = application.injector.instanceOf[AddASmallProducerView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val lowBandValue: Long = 1000
  val highBandValue: Long = 2000
  val litresInBands: LitresInBands = LitresInBands(lowBandValue, highBandValue)

  val sdilProducerReference: String = "XKSDIL000000023"
  val addASmallProducer: AddASmallProducer = AddASmallProducer(Option("PRODUCER"), sdilProducerReference, lowBandValue, highBandValue)

  val formProvider = new AddASmallProducerFormProvider()
  val userAnswers = emptyUserAnswersForCorrectReturn.set(SelectPage, returnPeriod.head).success.value
  val form: Form[AddASmallProducer] = formProvider.apply(userAnswers)
  val formWithHighAndLowBands: Form[AddASmallProducer] = form.fill(addASmallProducer)
  val formWithLowBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(highBand = 0))
  val formWithHighBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(lowBand = 0))
  val emptyForm: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "", "highBand" -> ""))
  val formWithNoNumeric: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "x", "highBand" -> "y"))
  val formWithNegativeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "-1", "highBand" -> "-2"))
  val formWithDecimalNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "1.8", "highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "lowBand" -> "110000000000000", "highBand" -> "120000000000000"))

  object Selectors {
    val heading = "govuk-heading-m"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val button = "govuk-button"
    val form = "form"
  }

  def testLitresInBandsWithPrepopulatedData(document: Document, numberOfPrecedingInputs: Int = 0): Unit = {
    "should include form groups for litres" - {
      "when the form is not prepopulated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        s"that includes ${numberOfPrecedingInputs + 2} input fields" in {
          formGroups.size() mustEqual numberOfPrecedingInputs + 2
        }
        "that includes a field for low band that is populated" in {
          val lowBandGroup = formGroups.get(numberOfPrecedingInputs)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe true
          lowBandGroup.getElementById("lowBand").attr("value") mustBe lowBandValue.toString
        }
        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroups.get(numberOfPrecedingInputs + 1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe true
          highBandGroup.getElementById("highBand").attr("value") mustBe highBandValue.toString
        }
      }
    }
  }

  def testLitresInBandsNoPrepopulatedData(document: Document, numberOfPrecedingInputs: Int = 0): Unit = {
    "should include form groups for litres" - {
      "when the form is populated and has no errors" - {
        val formGroups = document.getElementsByClass(Selectors.govukFormGroup)
        s"that includes ${numberOfPrecedingInputs + 2} input fields" in {
          formGroups.size() mustEqual numberOfPrecedingInputs + 2
        }
        "that includes a field for low band that is not populated" in {
          val lowBandGroup = formGroups.get(numberOfPrecedingInputs)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("lowBand").hasAttr("value") mustBe false
        }
        "that includes a field for high band that is not populated" in {
          val highBandGroup = formGroups.get(numberOfPrecedingInputs + 1)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("highBand").hasAttr("value") mustBe false
        }
      }
    }
  }

  def testButton(document: Document): Unit = {
    "should contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }
  }

  def testAction(document: Document, expectedAction: String): Unit = {
    "should contains a form with the correct action" in {
      document.select(Selectors.form)
        .attr("action") mustEqual expectedAction
    }
  }

  def testEmptyFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form being empty" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.required")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.required")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testNoNumericFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no numeric values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.nonNumeric")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.nonNumeric")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testNegativeFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing no negative values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")
          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)
          error1.text() mustBe Messages("litres.error.lowBand.negative")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.negative")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testDecimalFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing decimal values" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.wholeNumber")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.wholeNumber")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  def testOutOfMaxValFormErrors(document: Document, errorTitle: String): Unit = {
    "due to the form containing values out of max range" - {
      "should contain the title with error" in {
        document.title() must include(errorTitle)
      }
      "should contain the expected error summary" - {
        "when the form is empty" in {
          val errorSummary = document
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          val errors = errorSummary.getElementsByTag("li")

          errors.size() mustEqual 2
          val error1 = errors.get(0)
          val error2 = errors.get(1)

          error1.text() mustBe Messages("litres.error.lowBand.outOfMaxVal")
          error1.select("a").attr("href") mustBe "#lowBand"
          error2.text() mustBe Messages("litres.error.highBand.outOfMaxVal")
          error2.select("a").attr("href") mustBe "#highBand"
        }
      }
    }
  }

  "AddASmallProducerView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = addASmallProducerView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = addASmallProducerView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = addASmallProducerView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = addASmallProducerView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = addASmallProducerView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = addASmallProducerView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = addASmallProducerView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() mustEqual "Enter the registered small producer’s details - Soft Drinks Industry Levy - GOV.UK"
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe "Enter the registered small producer’s details"
        }

        val formGroupsNotPopulated = document.getElementsByClass(Selectors.govukFormGroup)
        "that includes a field for small producer name that is not populated" in {
          val smallProducerGroup = formGroupsNotPopulated.get(0)
          smallProducerGroup.getElementsByClass(Selectors.label).text() mustBe Messages("correctReturn.addASmallProducer.hint1")
          smallProducerGroup.getElementById("producerName").hasAttr("value") mustBe false
        }
        "that includes a field for SDIL reference number that is not populated" in {
          val sdilRefGroup = formGroupsNotPopulated.get(1)
          sdilRefGroup.getElementsByClass(Selectors.label).text() mustBe Messages("correctReturn.addASmallProducer.referenceNumber")
          sdilRefGroup.getElementById("referenceNumber-hint").text() mustBe Messages("correctReturn.addASmallProducer.hint2")
          sdilRefGroup.getElementById("referenceNumber").hasAttr("value") mustBe false
        }

        val formGroupsPopulated = documentWithValidData.getElementsByClass(Selectors.govukFormGroup)
        "that includes a field for small producer name that is populated" in {
          val smallProducerGroup = formGroupsPopulated.get(0)
          smallProducerGroup.getElementsByClass(Selectors.label).text() mustBe Messages("correctReturn.addASmallProducer.hint1")
          smallProducerGroup.getElementById("producerName").hasAttr("value") mustBe true
          smallProducerGroup.getElementById("producerName").attr("value") mustBe "PRODUCER"
        }
        "that includes a field for SDIL reference number that is populated" in {
          val sdilRefGroup = formGroupsPopulated.get(1)
          sdilRefGroup.getElementsByClass(Selectors.label).text() mustBe Messages("correctReturn.addASmallProducer.referenceNumber")
          sdilRefGroup.getElementById("referenceNumber-hint").text() mustBe Messages("correctReturn.addASmallProducer.hint2")
          sdilRefGroup.getElementById("referenceNumber").hasAttr("value") mustBe true
          sdilRefGroup.getElementById("referenceNumber").attr("value") mustBe sdilProducerReference
        }

        testLitresInBandsNoPrepopulatedData(document, numberOfPrecedingInputs = 2)
        testLitresInBandsWithPrepopulatedData(documentWithValidData, numberOfPrecedingInputs = 2)
        
        testButton(document)
        testAction(document, routes.AddASmallProducerController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: Enter the registered small producer’s details - Soft Drinks Industry Levy - GOV.UK"
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
