package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends StringFieldBehaviours {

  val lengthKey = "$className;format="decap"$.error.length"
  val maxLength = $maxLength$

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}
