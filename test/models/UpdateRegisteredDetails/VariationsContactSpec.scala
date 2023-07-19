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

package models.UpdateRegisteredDetails

import base.SpecBase
import models.backend.UkAddress
import models.updateRegisteredDetails.Submission.VariationsContact
import models.updateRegisteredDetails.UpdateContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.Json

class VariationsContactSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{
  "VariationsContact" - {
    "findDifInAddress" - {
      "when i have submitted no changes to an address" in {

        lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

        VariationsContact.findDifInAddress(contactAddress, subscriptionAddress) mustEqual None

      }

      "when I have edited the contact address for line 1" in {
        lazy val contactAddress = UkAddress(List("12 Bishop Street", "East London"), "E73 2RP")
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

        VariationsContact.findDifInAddress(contactAddress, subscriptionAddress) mustEqual Some(contactAddress)
      }

      "when I have edited the contact postcode" in {
        lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "F23 9RJ")
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

        VariationsContact.findDifInAddress(contactAddress, subscriptionAddress) mustEqual Some(contactAddress)
      }

      "when alf id doesn't match" in {
        lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP", alfId = Some("alf"))
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

        VariationsContact.findDifInAddress(contactAddress, subscriptionAddress) mustEqual None
      }
    }

    "generateBusinessContact" - {
      "when i have submitted no changes to an address" in {



       VariationsContact.generateBusinessContact(
         emptyUserAnswersForUpdateRegisteredDetails,
         aSubscription.copy(address = emptyUserAnswersForUpdateRegisteredDetails.contactAddress)
       ) mustEqual None
      }

      "when i have submitted a change to address line 1" in {
        lazy val subscriptionAddress = UkAddress(List("12 Bishop Street", "East London"), "E73 2RP")

        VariationsContact.generateBusinessContact(
          emptyUserAnswersForUpdateRegisteredDetails,
          aSubscription.copy(address = subscriptionAddress)
        ) mustEqual Some(VariationsContact(Some(emptyUserAnswersForUpdateRegisteredDetails.contactAddress),None, None))
      }

      "when i have submitted a change to postcode" in {
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "F23 9RJ")

        VariationsContact.generateBusinessContact(
          emptyUserAnswersForUpdateRegisteredDetails,
          aSubscription.copy(address = subscriptionAddress)
        ) mustEqual Some(VariationsContact(Some(emptyUserAnswersForUpdateRegisteredDetails.contactAddress),None, None))
      }

      "when i have submitted no changes to an address but have made changes to email address and phone number" in {
        val contactDetails = UpdateContactDetails("foo", "bar", "123456789", "email@test.com")

        VariationsContact.generateBusinessContact(
          emptyUserAnswersForUpdateRegisteredDetails.set(UpdateContactDetailsPage, contactDetails).success.value,
          aSubscription.copy(address = emptyUserAnswersForUpdateRegisteredDetails.contactAddress)
        ) mustEqual None
      }

      "when i have submitted changes to the address line 1,  email address and phone number" in {
        lazy val contactDetails = UpdateContactDetails("foo", "bar", "123456789", "email@test.com")
        lazy val subscriptionAddress = UkAddress(List("19 Rhes Priordy", "East London"), "F23 9RJ")

        VariationsContact.generateBusinessContact(
          emptyUserAnswersForUpdateRegisteredDetails.set(UpdateContactDetailsPage, contactDetails).success.value,
          aSubscription.copy(address = subscriptionAddress)
        ) mustEqual
          Some(
            VariationsContact(
              Some(emptyUserAnswersForUpdateRegisteredDetails.contactAddress),
              Some("123456789"),
              Some("email@test.com")
            )
          )
      }
    }

    "writes"-{
      "when submitted json is formed correctly" in {
        Json.toJson(VariationsContact(
            Some(emptyUserAnswersForUpdateRegisteredDetails.contactAddress),
            Some("123456789"),
            Some("email@test.com")
          )
        ) mustBe Json.obj(
          "addressLine1" -> "19 Rhes Priordy",
                 "addressLine2" -> "East London",
                 "addressLine3" -> "",
                 "addressLine4" -> "",
                 "postCode" -> "E73 2RP",
                 "telephoneNumber" -> "123456789",
                 "emailAddress" -> "email@test.com"
          )
      }
    }
  }
}
