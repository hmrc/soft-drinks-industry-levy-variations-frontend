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
import models.updateRegisteredDetails.ContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class VariationsPersonalDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{
  "VariationsPersonalDetails" -{
    val contactDetailsFromSubscription = ContactDetails.fromContact(aSubscription.contact)
    "apply should return the None" - {
      "when i have made no changes" in {
        val res = VariationsPersonalDetails.apply(contactDetailsFromSubscription, aSubscription)
        res mustEqual None
      }
    }

    "apply should return the expected model" - {
      "when i have changed only the name" in {
        val contactDetails = contactDetailsFromSubscription.copy(fullName = "Tom Jeffery")

        val res = VariationsPersonalDetails.apply(contactDetails, aSubscription)
        res mustEqual Some(VariationsPersonalDetails(Some("Tom Jeffery"), None, None, None))
      }

      "when i have changed the position" in {
        val contactDetails = contactDetailsFromSubscription.copy(position = "Chief Data Analyst")

        val res = VariationsPersonalDetails.apply(contactDetails, aSubscription)
        res mustEqual Some(VariationsPersonalDetails(None, Some("Chief Data Analyst"), None, None))
      }

      "when i have changed the telephone number" in {
        val contactDetails = contactDetailsFromSubscription.copy(phoneNumber = "02246 259761")

        val res = VariationsPersonalDetails.apply(contactDetails, aSubscription)
        res mustEqual Some(VariationsPersonalDetails(None, None, Some("02246 259761"), None))
      }

      "when i have changed the email" in {
        val contactDetails = contactDetailsFromSubscription.copy(email = "new.email@gmail.com")
        val res = VariationsPersonalDetails.apply(contactDetails, aSubscription)
        res mustEqual Some(VariationsPersonalDetails(None, None, None, Some("new.email@gmail.com")))
      }

      "when I have changed all fields" in {
        val contactDetails = ContactDetails("Tom Jeffery", "Chief Data Analyst", "02246 259761", "new.email@gmail.com")

        val res = VariationsPersonalDetails.apply(contactDetails, aSubscription)
        res mustEqual Some(VariationsPersonalDetails(Some("Tom Jeffery"), Some("Chief Data Analyst"), Some("02246 259761"), Some("new.email@gmail.com"))
        )
      }
    }
  }
}
