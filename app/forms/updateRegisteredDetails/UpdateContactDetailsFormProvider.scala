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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.updateRegisteredDetails.UpdateContactDetails

class UpdateContactDetailsFormProvider @Inject() extends Mappings {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
  private val nameRegex = """^[a-zA-Z &\.\`\'\-\^]+$"""
  private val position = """^[a-zA-Z &\.\`\'\-\^]+$"""
  private val phoneNumberRegex = """^[A-Z0-9 )/(\\#+*\-]+$"""

  def apply(): Form[UpdateContactDetails] = Form(
    mapping(
      "fullName" -> text("updateRegisteredDetails.updateContactDetails.error.fullName.required")
        .verifying(maxLength(40, "updateRegisteredDetails.updateContactDetails.error.fullName.length"))
        .verifying(regexp(nameRegex, "updateRegisteredDetails.updateContactDetails.error.fullName.invalid")),
      "position" -> text("updateRegisteredDetails.updateContactDetails.error.position.required")
        .verifying(maxLength(155, "updateRegisteredDetails.updateContactDetails.error.position.length"))
        .verifying(regexp(position, "updateRegisteredDetails.updateContactDetails.error.position.invalid")),
      "phoneNumber" -> text("updateRegisteredDetails.updateContactDetails.error.phoneNumber.required")
        .verifying(maxLength(24, "updateRegisteredDetails.updateContactDetails.error.phoneNumber.length"))
        .verifying(regexp(phoneNumberRegex, "updateRegisteredDetails.updateContactDetails.error.phoneNumber.invalid")),
      "email" -> text("updateRegisteredDetails.updateContactDetails.error.email.required")
        .verifying(maxLength(132, "updateRegisteredDetails.updateContactDetails.error.email.length"))
        .verifying(regexp(emailRegex, "updateRegisteredDetails.updateContactDetails.error.email.invalid"))
    )(UpdateContactDetails.apply)(UpdateContactDetails.unapply)
  )
}
