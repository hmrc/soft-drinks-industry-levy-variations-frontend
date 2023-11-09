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
import models.UserAnswers
import models.backend.{RetrievedSubscription, Site}
import models.enums.SiteTypes
import models.updateRegisteredDetails.ContactDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class VariationsSitesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{

  val todaysDate = LocalDate.now()

  val site1 = Site(contactAddress, Some("Site 1"), Some("10"), None)
  val site2 = Site(contactAddress, Some("Site 2"), Some("11"), None)
  val site3 = Site(contactAddress, Some("Site 3"), None, None)
  val site4 = Site(contactAddress, Some("Site 4"), Some("12"), Some(todaysDate.plusYears(1)))
  val site5 = Site(contactAddress, Some("Site 5"), Some("13"), Some(todaysDate.plusYears(1)))
  val site6 = Site(contactAddress, Some("Site 6"), None, Some(todaysDate.plusYears(1)))

  val closedsite1 = Site(contactAddress, Some("Closed Site 1"), Some("14"), Some(todaysDate.minusMonths(1)))
  val closedsite2 = Site(contactAddress, Some("Closed Site 2"), Some("15"), Some(todaysDate.minusMonths(1)))

  val contactDetailsFromSubscription = ContactDetails.fromContact(aSubscription.contact)

  def getSubscription(packagingSites: List[Site] = List.empty, warehouses: List[Site] = List.empty): RetrievedSubscription = {
    aSubscription.copy(
      warehouseSites = warehouses,
      productionSites = packagingSites
    )
  }

  def getUserAnswers(packagingSites: List[Site] = List.empty, warehouses: List[Site] = List.empty): UserAnswers = {
    emptyUserAnswersForCancelRegistration.copy(
      warehouseList = warehouses.zipWithIndex.map{case(site, index) => index.toString -> site}.toMap,
      packagingSiteList = packagingSites.zipWithIndex.map{case(site, index) => index.toString -> site}.toMap
    )
  }


  "fromUserAnswers" - {
    "when the retrieved subscription has a closure date in the past" - {
      val subscription = getSubscription(List(site1, site4, closedsite1), List(site2, site5, closedsite2))
      "and no new sites are added or removed" - {
        "should return variationSites with only closed sites" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.isEmpty mustBe true
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual(List("14", "15"))
        }
      }
      "and a new warehouse site is added and none removed" - {
        "should return variationSites with the new site and closed sites" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2, site5, site3)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "16"
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual(List("14", "15"))
        }
      }
      "and a new packaging site is added and none removed" - {
        "should return variationSites with the new site and closed sites" in {
          val packagingSites = List(site1, site4, site3)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "16"
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual(List("14", "15"))
        }
      }

      "and a new packaging site and warehouse is added and none removed" - {
        "should return variationSites with the new sites and closed sites" in {
          val packagingSites = List(site1, site4, site3)
          val warehouses = List(site2, site5, site6)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 2
          res.newSites.map(_.siteReference) mustBe List("16", "17")
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual(List("14", "15"))
        }
      }

      "and a warehouse site is removed" - {
        "should return variationSites with no new site and closed sites" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 3
          res.closedSites.map(_.siteReference) mustEqual List("14", "13", "15")
        }
      }
      "and packaging site is removed" - {
        "should return variationSites with no new site and closed sites" in {
          val packagingSites = List(site1)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 3
          res.closedSites.map(_.siteReference) mustEqual (List("12", "14", "15"))
        }
      }

      "and a packaging site and warehouse is removed" - {
        "should return variationSites with no new sites and closed sites" in {
          val packagingSites = List(site1)
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 4
          res.closedSites.map(_.siteReference) mustEqual List("12", "14", "13", "15")
        }
      }

      "and a packaging site and warehouse both have one added and one removed" - {
        "should return variationSites with the new sites and closed sites" in {
          val packagingSites = List(site1, site3)
          val warehouses = List(site2, site6)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 2
          res.newSites.map(_.siteReference) mustBe List("16", "17")
          res.closedSites.size mustBe 4
          res.closedSites.map(_.siteReference) mustEqual (List("12", "14", "13", "15"))
        }
      }

      "and all packaging sites and warehouse have been removed" - {
        "should return variationSites with no new sites and all closed sites" in {
          val packagingSites = List()
          val warehouses = List()
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 6
          res.closedSites.map(_.siteReference) mustEqual (List("10", "12", "14", "11", "13", "15"))
        }
      }
    }

    "when the retrieved subscription has no closure date in the past" - {
      val subscription = getSubscription(List(site1, site4), List(site2, site5))
      "and no new sites are added or removed" - {
        "should return variationSites with no sites" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.isEmpty mustBe true
          res.closedSites.size mustBe 0
        }
      }
      "and a new warehouse site is added and none removed" - {
        "should return variationSites with the new site and no closed sites" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2, site5, site3)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "14"
          res.closedSites.size mustBe 0
        }
      }
      "and a new packaging site is added and none removed" - {
        "should return variationSites with the new site and no closed sites" in {
          val packagingSites = List(site1, site4, site3)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "14"
          res.closedSites.size mustBe 0
        }
      }

      "and a new packaging site and warehouse is added and none removed" - {
        "should return variationSites with the new sites and no closed sites" in {
          val packagingSites = List(site1, site4, site3)
          val warehouses = List(site2, site5, site6)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 2
          res.newSites.map(_.siteReference) mustBe List("14", "15")
          res.closedSites.size mustBe 0
        }
      }

      "and a warehouse site is removed" - {
        "should return variationSites with no new sites and the closed site" in {
          val packagingSites = List(site1, site4)
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 1
          res.closedSites.map(_.siteReference) mustEqual List("13")
        }
      }
      "and packaging site is removed" - {
        "should return variationSites with no new sites and the closed site" in {
          val packagingSites = List(site1)
          val warehouses = List(site2, site5)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 1
          res.closedSites.map(_.siteReference) mustEqual List("12")
        }
      }

      "and a packaging site and warehouse is removed" - {
        "should return variationSites with no new sites and closed sites" in {
          val packagingSites = List(site1)
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 0
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual List("12","13")
        }
      }

      "and a packaging site and warehouse both have one added and one removed" - {
        "should return variationSites with the new sites and closed sites" in {
          val packagingSites = List(site1, site3)
          val warehouses = List(site2, site6)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 2
          res.newSites.map(_.siteReference) mustBe List("14", "15")
          res.closedSites.size mustBe 2
          res.closedSites.map(_.siteReference) mustEqual List("12", "13")
        }
      }
    }

    "when the retrieved subscription has no sites" - {
      val subscription = getSubscription()
      "and no new sites are added or removed" - {
        "should return variationSites with no sites" in {
          val packagingSites = List()
          val warehouses = List()
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.isEmpty mustBe true
          res.closedSites.size mustBe 0
        }
      }
      "and a new warehouse site is added and none removed" - {
        "should return variationSites with the new site and no closed sites" in {
          val packagingSites = List()
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "1"
          res.closedSites.size mustBe 0
        }
      }
      "and a new packaging site is added and none removed" - {
        "should return variationSites with the new site and no closed sites" in {
          val packagingSites = List(site1)
          val warehouses = List()
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 1
          res.newSites.head.siteReference mustBe "1"
          res.closedSites.size mustBe 0
        }
      }

      "and a new packaging site and warehouse is added and none removed" - {
        "should return variationSites with the new sites and no closed sites" in {
          val packagingSites = List(site1)
          val warehouses = List(site2)
          val userAnswers = getUserAnswers(packagingSites, warehouses)
          val res = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetailsFromSubscription)
          res.newSites.size mustBe 2
          res.newSites.map(_.siteReference) mustBe List("1", "2")
          res.closedSites.size mustBe 0
        }
      }
    }

  }
}
