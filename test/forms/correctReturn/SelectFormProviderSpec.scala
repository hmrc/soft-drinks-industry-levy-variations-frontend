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

package forms.correctReturn

import base.SpecBase
import forms.behaviours.OptionFieldBehaviours
import models.ReturnPeriod
import play.api.data.{Form, FormError}
import play.api.libs.json.Json

class SelectFormProviderSpec extends OptionFieldBehaviours with SpecBase {

  val form = new SelectFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "correctReturn.select.error.required"

    def returnsoptionsField[T](form: Form[_],
                               fieldName: String,
                               validValues: Seq[ReturnPeriod],
                               invalidError: FormError): Unit = {

      "bind all valid values" in {

        for (value <- validValues) {

          val result = form.bind(Map(fieldName -> Json.toJson(value).toString)).apply(fieldName)
          result.value.value mustEqual Json.toJson(value).toString
          result.errors mustBe empty
        }
      }

      "not bind required values" in {

        val generator = stringsExceptSpecificValues(validValues.map(_.toString))

        forAll(generator -> "invalidValue") {
          value =>

            val result = form.bind(Map(fieldName -> value)).apply(fieldName)
            result.errors mustEqual  List(FormError("value", List("correctReturn.select.error.required"), List()))
        }
      }

    }

      behave like returnsoptionsField[ReturnPeriod](
        form,
        fieldName,
        validValues = returnPeriodList,
        invalidError = FormError(fieldName, "error.required")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
  }
}
