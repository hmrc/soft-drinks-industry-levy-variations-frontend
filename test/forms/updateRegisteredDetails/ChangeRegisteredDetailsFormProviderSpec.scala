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

package forms.updateRegisteredDetails

import forms.behaviours.CheckboxFieldBehaviours
import models.updateRegisteredDetails.ChangeRegisteredDetails
import play.api.data.FormError

class ChangeRegisteredDetailsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new ChangeRegisteredDetailsFormProvider

  ".value" - {

    val fieldName = "value"
    val requiredKey = "updateRegisteredDetails.changeRegisteredDetails.error.required"

    "for isVoluntary false" - {
      behave like checkboxField[ChangeRegisteredDetails](
        form.apply(isVoluntary = false),
        fieldName,
        validValues = ChangeRegisteredDetails.values,
        invalidError = FormError(s"$fieldName[0]", "error.invalid")
      )
      behave like mandatoryCheckboxField(
        form.apply(isVoluntary = false),
        fieldName,
        requiredKey
      )
    }
    "for isVoluntary true" - {
      behave like checkboxField[ChangeRegisteredDetails](
        form.apply(isVoluntary = true),
        fieldName,
        validValues = ChangeRegisteredDetails.voluntaryValues,
        invalidError = FormError(s"$fieldName[0]", "error.invalid")
      )

      behave like mandatoryCheckboxField(
        form.apply(isVoluntary = true),
        fieldName,
        requiredKey
      )
    }
  }
}
