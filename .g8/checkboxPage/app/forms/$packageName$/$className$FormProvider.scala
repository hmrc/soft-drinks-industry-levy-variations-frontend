package forms.$packageName$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.set
import models.$packageName$.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Set[$className$]] =
    Form(
      "value" -> set(enumerable[$className$]("$packageName$.$className;format="decap"$.error.required")).verifying(nonEmptySet("$packageName$.$className;format="decap"$.error.required"))
    )
}
