package forms.updateRegisteredDetails

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PackingSiteDetailsRemoveFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "updateRegisteredDetails.packingSiteDetailsRemove.error.required"
  val invalidKey = "error.boolean"

  val form = new PackingSiteDetailsRemoveFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
