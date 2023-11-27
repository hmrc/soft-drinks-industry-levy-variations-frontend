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

import models.backend.{RetrievedSubscription, Site, UkAddress}
import models.updateRegisteredDetails.ContactDetails
import models.UserAnswers
import play.api.libs.json.{JsObject, JsString, JsValue, Json, Writes}

import scala.util.Try

case class VariationsContact(
                              address: Option[UkAddress] = None,
                              telephoneNumber: Option[String] = None,
                              emailAddress: Option[String] = None){
  def nonEmpty: Boolean = Seq(address, telephoneNumber, emailAddress).flatten.nonEmpty
}

object VariationsContact {

  private[models] def findDiffInAddress(userAnswersAddress: UkAddress, subscriptionAddress: UkAddress): Option[UkAddress] = {
    val diffInAddress = subscriptionAddress.lines != userAnswersAddress.lines ||
      subscriptionAddress.postCode != userAnswersAddress.postCode
    if (diffInAddress) Some(userAnswersAddress) else None
  }

  def generateForSiteContact(contactDetails: ContactDetails, site: Site): VariationsContact = {
    VariationsContact(
      Some(site.address),
      Some(contactDetails.phoneNumber),
      Some(contactDetails.email)
    )
  }

  def generateForBusinessContact(userAnswers: UserAnswers,
                                 subscription: RetrievedSubscription,
                                 updatedContactDetails: Option[VariationsPersonalDetails]): Option[VariationsContact] = {
    val variationsContact: Option[UkAddress] = findDiffInAddress(userAnswers.contactAddress, subscription.address)
    lazy val telephoneNumber = updatedContactDetails.fold[Option[String]](None)(_.telephoneNumber)
    lazy val emailAddress = updatedContactDetails.flatMap(_.emailAddress)
    if (variationsContact.nonEmpty || Seq(telephoneNumber, emailAddress).flatten.nonEmpty) {
      Some(VariationsContact(variationsContact, telephoneNumber, emailAddress))
    } else {
      None
    }
  }

  implicit val writes: Writes[VariationsContact] = new Writes[VariationsContact] {
    override def writes(o: VariationsContact): JsValue = {
      val optAddressJson: Option[JsObject] = o.address.map { address =>
        Json.obj(
          ("addressLine1", JsString(address.lines.headOption.getOrElse(""))),
          ("addressLine2", JsString(Try(address.lines(1)).getOrElse(""))),
          ("addressLine3", JsString(Try(address.lines(2)).getOrElse(""))),
          ("addressLine4", JsString(Try(address.lines(3)).getOrElse(""))),
          ("postCode", JsString(address.postCode))
        )
      }
      val phoneAndEmailJson: JsObject = Json.obj(
        "telephoneNumber" -> o.telephoneNumber,
        "emailAddress" -> o.emailAddress
      )
      optAddressJson match {
        case Some(jsObject) => jsObject ++ phoneAndEmailJson
        case None => phoneAndEmailJson
      }
    }
  }
}
