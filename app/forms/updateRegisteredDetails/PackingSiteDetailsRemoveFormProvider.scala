package forms.updateRegisteredDetails

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class PackingSiteDetailsRemoveFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("updateRegisteredDetails.packingSiteDetailsRemove.error.required")
    )
}
