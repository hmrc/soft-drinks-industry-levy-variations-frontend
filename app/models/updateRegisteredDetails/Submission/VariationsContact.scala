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

import models.backend.UkAddress
import models.{RetrievedSubscription, UserAnswers}
import play.api.libs.json.{JsString, JsValue, Json, Writes}

import scala.util.Try

case class VariationsContact(
                              address: Option[UkAddress] = None,
                              telephoneNumber: Option[String] = None,
                              emailAddress: Option[String] = None
                            )

object VariationsContact {

  private[models]  def findDifInAddress(userAnswersAddress: UkAddress, subscriptionAddress: UkAddress): Option[UkAddress] = {
    if(subscriptionAddress.lines == userAnswersAddress.lines && subscriptionAddress.postCode == userAnswersAddress.postCode){
      None
    } else {
      Some(userAnswersAddress)
    }
  }

  def generateBusinessContact(userAnswers: UserAnswers, subscription: RetrievedSubscription): Option[VariationsContact] = {

    val variationsContact: Option[UkAddress] = findDifInAddress(userAnswers.contactAddress, subscription.address)

    val personalDetails:Option[VariationsPersonalDetails] =  VariationsPersonalDetails.apply(userAnswers, subscription)

    variationsContact match {
      case None => None
      case Some(answers) => Some(VariationsContact(
        variationsContact,
        personalDetails.fold(Option.empty[String])(_.telephoneNumber),
        personalDetails.fold(Option.empty[String])(_.emailAddress)
      ))
    }
  }

  implicit val writes: Writes[VariationsContact] = new Writes[VariationsContact] {
    override def writes(o: VariationsContact): JsValue = Json.obj(
      ("addressLine1", JsString(o.address.fold("")(_.lines.headOption.getOrElse("")))),
      ("addressLine2", JsString(Try(o.address.fold("")(_.lines(1))).getOrElse(""))),
      ("addressLine3", JsString(Try(o.address.fold("")(_.lines(2))).getOrElse(""))),
      ("addressLine4", JsString(Try(o.address.fold("")(_.lines(3))).getOrElse(""))),
      ("postCode", JsString(o.address.fold("")(_.postCode))),
      "telephoneNumber" -> o.telephoneNumber,
      "emailAddress"    -> o.emailAddress
    )
  }
}
