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

import forms.behaviours.OptionFieldBehaviours
import models.correctReturn.ExemptionsForSmallProducers
import play.api.data.FormError

class ExemptionsForSmallProducersFormProviderSpec extends OptionFieldBehaviours {

  val form = new ExemptionsForSmallProducersFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "correctReturn.exemptionsForSmallProducers.error.required"

    behave like optionsField[ExemptionsForSmallProducers](
      form,
      fieldName,
      validValues  = ExemptionsForSmallProducers.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
