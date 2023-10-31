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

import base.SpecBase
import models.backend.UkAddress
import models.updateRegisteredDetails.ContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
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

    "generateForBusinessContact" - {
      "should return None" - {
        "when the contact address is the same and variation personal details are not supplied" in {
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = aSubscription.address),
            aSubscription, None)

          res mustEqual None
        }

        "when the contact address is the same and the variation personal details don't have an update phone or email" in {
          val varPDs = VariationsPersonalDetails()
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = aSubscription.address),
            aSubscription, Some(varPDs))

          res mustEqual None
        }
      }

      "should return the expected model" - {
        "when the address is different but contact details are unchanged" in {
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = updatedContactAddress),
            aSubscription, None)

          val expectedResult = VariationsContact(Some(updatedContactAddress))

          res mustEqual Some(expectedResult)
        }

        "when only the phoneNumber has changed" in {
          val updatedPhoneNumber = "0800483922"
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = aSubscription.address),
            aSubscription, Some(VariationsPersonalDetails(telephoneNumber = Some(updatedPhoneNumber))))

          val expectedResult = VariationsContact(telephoneNumber = Some(updatedPhoneNumber))

          res mustEqual Some(expectedResult)
        }

        "when only the email has changed" in {
          val updatedEmail = "test1@example.com"
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = aSubscription.address),
            aSubscription, Some(VariationsPersonalDetails(emailAddress = Some(updatedEmail))))

          val expectedResult = VariationsContact(emailAddress = Some(updatedEmail))

          res mustEqual Some(expectedResult)
        }

        "when address, telephone and email are all updated" in {
          val updatedPhoneNumber = "0800483922"
          val updatedEmail = "test1@example.com"
          val res = VariationsContact.generateForBusinessContact(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = updatedContactAddress),
            aSubscription, Some(VariationsPersonalDetails(telephoneNumber = Some(updatedPhoneNumber), emailAddress = Some(updatedEmail))))

          val expectedResult = VariationsContact(Some(updatedContactAddress), Some(updatedPhoneNumber), Some(updatedEmail))

          res mustEqual Some(expectedResult)
        }
      }
    }

    "generateForSiteContact" - {
      "must return the expected Variations contact" - {
        "when provided with contact details and site" in {
          val contactDetails = ContactDetails("full name", "job", "0897484949", "test@email.com")
          val res = VariationsContact.generateForSiteContact(contactDetails, packingSite)
          val expectedResult = VariationsContact(Some(packingSite.address), Some("0897484949"), Some("test@email.com"))

          res mustEqual expectedResult
        }
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

      "when submitted (with no telephone number or email address) json is formed correctly" in {
        Json.toJson(VariationsContact(
          Some(emptyUserAnswersForUpdateRegisteredDetails.contactAddress),
          None,
          None
        )
        ) mustBe Json.obj(
          "addressLine1" -> "19 Rhes Priordy",
          "addressLine2" -> "East London",
          "addressLine3" -> "",
          "addressLine4" -> "",
          "postCode" -> "E73 2RP",
          "telephoneNumber" -> None,
          "emailAddress" -> None
        )
      }
    }
  }
}
