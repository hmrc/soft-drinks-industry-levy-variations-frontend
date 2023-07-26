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

package models.updateRegisteredDetails.Submission

import models.{RetrievedSubscription, UserAnswers}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.{Json, Writes}

case class VariationsPersonalDetails(
                                      name: Option[String] = None,
                                      position: Option[String] = None,
                                      telephoneNumber: Option[String] = None,
                                      emailAddress: Option[String] = None) {
  def nonEmpty: Boolean = Seq(name, position, telephoneNumber, emailAddress).flatten.nonEmpty
}

object VariationsPersonalDetails {

  def apply(userAnswers : UserAnswers, subscription : RetrievedSubscription):Option[VariationsPersonalDetails] = {
    userAnswers.get(UpdateContactDetailsPage).flatMap{ answers =>
      val name = if(answers.fullName == subscription.contact.name.getOrElse("")){None} else {Some(answers.fullName)}
      val position = if(answers.position == subscription.contact.positionInCompany.getOrElse("")){None} else {Some(answers.position)}
      val telephoneNumber = if(answers.phoneNumber == subscription.contact.phoneNumber){None} else {Some(answers.phoneNumber)}
      val emailAddress = if(answers.email == subscription.contact.email){None} else {Some(answers.email)}
      (name, position, telephoneNumber, emailAddress) match {
        case (None,None, None, None) => None
        case _ =>  Some(VariationsPersonalDetails(name, position, telephoneNumber, emailAddress))
      }
    }
  }

  implicit val writes: Writes[VariationsPersonalDetails] = Json.writes[VariationsPersonalDetails]
}

