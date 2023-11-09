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

package models.submission

import models.backend.RetrievedSubscription
import models.updateRegisteredDetails.ContactDetails
import play.api.libs.json.{Format, Json}

case class VariationsPersonalDetails(
                                      name: Option[String] = None,
                                      position: Option[String] = None,
                                      telephoneNumber: Option[String] = None,
                                      emailAddress: Option[String] = None) {
  def nonEmpty: Boolean = Seq(name, position, telephoneNumber, emailAddress).flatten.nonEmpty
}

object VariationsPersonalDetails extends VariationSubmissionHelper {

  def apply(updatedContactDetails: ContactDetails,
            subscription: RetrievedSubscription): Option[VariationsPersonalDetails] = {
    val updatedPDs = VariationsPersonalDetails(
      name = updatedContactDetails.fullName ifDifferentTo subscription.contact.name.getOrElse(""),
      position = updatedContactDetails.position ifDifferentTo subscription.contact.positionInCompany.getOrElse(""),
      telephoneNumber = updatedContactDetails.phoneNumber ifDifferentTo subscription.contact.phoneNumber,
      emailAddress = updatedContactDetails.email ifDifferentTo subscription.contact.email
    )

    if(updatedPDs.nonEmpty) {
      Some(updatedPDs)
    } else {
      None
    }
  }

  implicit val format: Format[VariationsPersonalDetails] = Json.format[VariationsPersonalDetails]
}

