package forms.correctReturn

import forms.behaviours.OptionFieldBehaviours
import models.correctReturn.Select
import play.api.data.FormError

class SelectFormProviderSpec extends OptionFieldBehaviours {

  val form = new SelectFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "correctReturn.select.error.required"

    behave like optionsField[Select](
      form,
      fieldName,
      validValues  = Select.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
