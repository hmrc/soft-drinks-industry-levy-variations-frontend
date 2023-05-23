package forms.$packageName$

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "$packageName$.$className;format="decap"$.error.invalid",
        allRequiredKey = "$packageName$.$className;format="decap"$.error.required.all",
        twoRequiredKey = "$packageName$.$className;format="decap"$.error.required.two",
        requiredKey    = "$packageName$.$className;format="decap"$.error.required"
      )
    )
}
