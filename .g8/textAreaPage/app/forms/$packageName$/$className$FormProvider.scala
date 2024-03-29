package forms.$packageName$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text()
        .verifying(maxLength($maxLength$, "$packageName$.$className;format="decap"$.error.length"))
    )
}
