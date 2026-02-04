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

package forms.cancelRegistration

import config.FrontendAppConfig

import java.time.LocalDate
import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class CancelRegistrationDateFormProvider @Inject() (config: FrontendAppConfig) extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "cancelRegistrationDate" -> localDate(
        invalidKey = "cancelRegistrationDate.error.invalid",
        allRequiredKey = "cancelRegistrationDate.error.required.all",
        twoRequiredKey = "cancelRegistrationDate.error.required.two",
        requiredKey = "cancelRegistrationDate.error.required",
        invalidDay = "cancelRegistrationDate.error.day.nan",
        invalidDayLength = "cancelRegistrationDate.error.day.length",
        invalidMonth = "cancelRegistrationDate.error.month.nan",
        invalidMonthLength = "cancelRegistrationDate.error.month.length",
        invalidYear = "cancelRegistrationDate.error.year.nan",
        invalidYearLength = "cancelRegistrationDate.error.year.length"
      ).verifying(
        minDate(LocalDate.now(), "cancelRegistrationDate.error.registration-date.min"),
        maxDate(config.cancelRegistrationDateMaxDaysInFuture, "cancelRegistrationDate.error.registration-date.max")
      )
    )
}
