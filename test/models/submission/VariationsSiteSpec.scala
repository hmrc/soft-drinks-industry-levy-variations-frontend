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
import models.backend.Site
import models.enums.SiteTypes
import models.updateRegisteredDetails.ContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class VariationsSiteSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{

  "generateFromSite" -{
    val site = Site(updatedContactAddress, Some("NAME"), Some("100"), None)
    val contactDetailsFromSubscription = ContactDetails.fromContact(aSubscription.contact)
    val variationsContact = VariationsContact(Some(site.address), Some(contactDetailsFromSubscription.phoneNumber), Some(contactDetailsFromSubscription.email))
    "should return the expected model" - {
      "when the site type is Warehouse" in {
        val res = VariationsSite.generateFromSite(site, contactDetailsFromSubscription, 10, SiteTypes.WAREHOUSE)
        res mustEqual VariationsSite("NAME", "10", variationsContact, SiteTypes.WAREHOUSE)
      }

      "when the site type is ProductionSite" in {
        val res = VariationsSite.generateFromSite(site, contactDetailsFromSubscription, 10, SiteTypes.PRODUCTION_SITE)
        res mustEqual VariationsSite("NAME", "10", variationsContact, SiteTypes.PRODUCTION_SITE)
      }
    }
  }
}
