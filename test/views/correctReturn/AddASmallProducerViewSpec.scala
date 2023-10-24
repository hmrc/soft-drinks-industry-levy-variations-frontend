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
import models.{CheckMode, EditMode, LitresInBands, Mode, NormalMode}
import org.jsoup.nodes.Document
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
  val addASmallProducer: AddASmallProducer = AddASmallProducer(Option("PRODUCER"), sdilProducerReference,litres = LitresInBands(lowBandValue, highBandValue))

  val formProvider = new AddASmallProducerFormProvider()
  val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod.head))
  val form: Form[AddASmallProducer] = formProvider.apply(userAnswers)
  val formWithHighAndLowBands: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> s"$lowBandValue", "litres.highBand" -> s"$highBandValue"))
  val formWithLowBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(litres = LitresInBands(1, 0)))
  val formWithHighBandOnly: Form[AddASmallProducer] = form.fill(addASmallProducer.copy(litres = LitresInBands(0, 1)))
  val emptyForm: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> "", "litres.highBand" -> ""))
  val formWithNoNumeric: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> "x", "litres.highBand" -> "y"))
  val formWithNegativeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> "-1", "litres.highBand" -> "-2"))
  val formWithDecimalNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> "1.8", "litres.highBand" -> "2.3"))
  val formWithOutOfRangeNumber: Form[AddASmallProducer] = form.bind(Map("producerName" -> "PRODUCER", "referenceNumber" -> sdilProducerReference, "litres.lowBand" -> "110000000000000", "litres.highBand" -> "120000000000000"))

  object Selectors {
    val heading = "govuk-heading-l"
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
          val lowBandGroup = formGroups.get(2)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("litres.lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("litres.lowBand").hasAttr("value") mustBe true
          lowBandGroup.getElementById("litres.lowBand").attr("value") mustBe lowBandValue.toString
        }
        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroups.get(3)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("litres.highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("litres.highBand").hasAttr("value") mustBe true
          highBandGroup.getElementById("litres.highBand").attr("value") mustBe highBandValue.toString
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
        "that includes a field for low band that is populated" in {
          val lowBandGroup = formGroups.get(2)
          lowBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.lowBand")
          lowBandGroup.getElementById("litres.lowBand-hint").text() mustBe Messages("litres.lowBandHint")
          lowBandGroup.getElementById("litres.lowBand").hasAttr("value") mustBe false
        }
        "that includes a field for high band that is populated" in {
          val highBandGroup = formGroups.get(3)
          highBandGroup.getElementsByClass(Selectors.label).text() mustBe Messages("litres.highBand")
          highBandGroup.getElementById("litres.highBand-hint").text() mustBe Messages("litres.highBandHint")
          highBandGroup.getElementById("litres.highBand").hasAttr("value") mustBe false
        }
      }
    }
  }

  def testButton(document: Document): Unit = {
    "should contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.saveContinue")
    }
  }

  def testAction(mode: Mode, document: Document, expectedAction: String): Unit = {
    "should contains a form with the correct action" in {
      val action = document.select(Selectors.form).attr("action")
      mode match{
        case NormalMode => action mustEqual s"/soft-drinks-industry-levy-variations-frontend/correct-return/add-small-producer"
        case EditMode => action mustEqual s"/soft-drinks-industry-levy-variations-frontend/correct-return/change-add-small-producer-edit?sdilReference="
        case CheckMode => action mustEqual s"/soft-drinks-industry-levy-variations-frontend/correct-return/change-add-small-producer-edit?sdilReference="
      }

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
          error1.select("a").attr("href") mustBe "#litres.lowBand"
          error2.text() mustBe Messages("litres.error.highBand.required")
          error2.select("a").attr("href") mustBe "#litres.highBand"
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
          error1.select("a").attr("href") mustBe "#litres.lowBand"
          error2.text() mustBe Messages("litres.error.highBand.nonNumeric")
          error2.select("a").attr("href") mustBe "#litres.highBand"
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
          error1.select("a").attr("href") mustBe "#litres.lowBand"
          error2.text() mustBe Messages("litres.error.highBand.negative")
          error2.select("a").attr("href") mustBe "#litres.highBand"
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
          error1.select("a").attr("href") mustBe "#litres.lowBand"
          error2.text() mustBe Messages("litres.error.highBand.wholeNumber")
          error2.select("a").attr("href") mustBe "#litres.highBand"
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
          error1.select("a").attr("href") mustBe "#litres.lowBand"
          error2.text() mustBe Messages("litres.error.highBand.outOfMaxVal")
          error2.select("a").attr("href") mustBe "#litres.highBand"
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
        testAction(mode, document, routes.AddASmallProducerController.onSubmit(mode).url)

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
