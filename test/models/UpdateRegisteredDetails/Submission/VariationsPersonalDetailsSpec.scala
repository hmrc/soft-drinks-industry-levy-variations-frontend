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

package models.UpdateRegisteredDetails.Submission

import base.SpecBase
import models.updateRegisteredDetails.Submission.VariationsPersonalDetails
import models.updateRegisteredDetails.ContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.updateRegisteredDetails.UpdateContactDetailsPage

class VariationsPersonalDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{
  "VariationsPersonalDetails" -{
    "apply" -{
      "when i have made no changes" in {
          VariationsPersonalDetails.apply(
            emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = aSubscription.address),aSubscription
          ) mustEqual None
        }

      "when i have changed the name" in {
        val contactDetails = ContactDetails("Tom Jeffery", "Chief Infrastructure Agent", "04495 206189", "Adeline.Greene@gmail.com")

        VariationsPersonalDetails.apply(
          emptyUserAnswersForUpdateRegisteredDetails.copy(
            contactAddress = aSubscription.address
          ).set(UpdateContactDetailsPage, contactDetails).success.value,aSubscription
        ) mustEqual Some(VariationsPersonalDetails(Some("Tom Jeffery"), None, None, None))
      }

      "when i have changed the position" in {
        val contactDetails = ContactDetails("Ava Adams", "Chief Data Analyst", "04495 206189", "Adeline.Greene@gmail.com")

        VariationsPersonalDetails.apply(
          emptyUserAnswersForUpdateRegisteredDetails.copy(
            contactAddress = aSubscription.address
          ).set(UpdateContactDetailsPage, contactDetails).success.value,aSubscription
        ) mustEqual Some(VariationsPersonalDetails(None, Some("Chief Data Analyst"), None, None))
      }

      "when i have changed the telephone number" in {
        val contactDetails = ContactDetails("Ava Adams", "Chief Infrastructure Agent", "02246 259761", "Adeline.Greene@gmail.com")

        VariationsPersonalDetails.apply(
          emptyUserAnswersForUpdateRegisteredDetails.copy(
            contactAddress = aSubscription.address
          ).set(UpdateContactDetailsPage, contactDetails).success.value,aSubscription
        ) mustEqual Some(VariationsPersonalDetails(None, None, Some("02246 259761"), None))
      }

      "when i have changed the email" in {
        val contactDetails = ContactDetails("Ava Adams", "Chief Infrastructure Agent", "04495 206189", "new.email@gmail.com")

        VariationsPersonalDetails.apply(
          emptyUserAnswersForUpdateRegisteredDetails.copy(
            contactAddress = aSubscription.address
          ).set(UpdateContactDetailsPage, contactDetails).success.value,aSubscription
        ) mustEqual Some(VariationsPersonalDetails(None, None, None, Some("new.email@gmail.com")))
      }
    }
  }
}
