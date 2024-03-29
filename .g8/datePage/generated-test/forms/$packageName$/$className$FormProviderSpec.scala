package forms.$packageName$

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class $className$FormProviderSpec extends DateBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "$packageName$.$className;format="decap"$.error.required.all")
  }
}
