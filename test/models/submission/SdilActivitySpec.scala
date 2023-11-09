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
import models.backend.{RetrievedActivity, RetrievedSubscription, UkAddress}
import models.changeActivity.{AmountProduced, ChangeActivityData}
import models.{Contact, LitresInBands, VariationsSubmissionDataHelper}
import play.api.libs.json.Json

import java.time.LocalDate

class SdilActivitySpec extends SpecBase with VariationsSubmissionDataHelper {

  val localDate: LocalDate = LocalDate.of(2023, 5, 6)
  val litresInBands = LitresInBands(1000L, 2000L)
  val litreage = Litreage(1000L, 2000L)

  val retrievedActivityLiableLargeProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = true,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = false
  )

  val retrievedActivityLiableSmallProducer = RetrievedActivity(
    smallProducer = true,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = true
  )


  val retrievedActivityLiableNoneProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = true
  )
  def subscription(originalActivity: RetrievedActivity) = RetrievedSubscription(
    "foo", "bar", "wizz", UkAddress(List.empty, ""), originalActivity,
    LocalDate.now(), productionSites = List.empty, warehouseSites = List.empty, contact = Contact(None,None,"",""), deregDate = None)

  def changeActivityData(amountProduced: AmountProduced, liable: Boolean, voluntary: Boolean): ChangeActivityData = {
    val (thirdPartyPackagers, ownBrands, howManyOwnBrands) = amountProduced match {
      case AmountProduced.Large => (None, Some(true), Some(litresInBands))
      case AmountProduced.Small if liable => (Some(false), Some(true), Some(litresInBands))
      case AmountProduced.Small => (Some(true), Some(true), Some(litresInBands))
      case _ => (None, None, None)
    }
    new ChangeActivityData(
      amountProduced = amountProduced,
      thirdPartyPackagers = thirdPartyPackagers,
      operatePackagingSiteOwnBrands = ownBrands,
      howManyOperatePackagingSiteOwnBrands = howManyOwnBrands,
      contractPacking = Some(true),
      howManyContractPacking = Some(litresInBands),
      imports = Some(true),
      howManyImports = Some(litresInBands)
    ) {
      override def isLiable: Boolean = liable

      override def isVoluntary: Boolean = voluntary
    }
  }

  "fromChangeActivityData" - {
    "should return the expected case class" - {
      "when a user who is large producer who is liable" - {
        val retrievedSubscription = subscription(retrievedActivityLiableLargeProducer)
        "change there activity values only" in {
          val caData = changeActivityData(AmountProduced.Large, true, false)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), None, true)
          val expectedModel = SdilActivity(Some(expectedActivity), None, None, None, None, None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }
        "changes to be a small producer who is voluntary" in {
          val caData = changeActivityData(AmountProduced.Small, false, true)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), Some(Litreage(1, 1)), false)
          val expectedModel = SdilActivity(Some(expectedActivity), Some(true), Some(true), Some(true), Some(true), None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }


        "changes to be a None producer" in {
          val caData = changeActivityData(AmountProduced.None, false, false)
          val expectedActivity = Activity(None, Some(litreage), Some(litreage), None, false)
          val expectedModel = SdilActivity(Some(expectedActivity), Some(true), None, None, None, None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }
      }

      "when a user who is small producer who is liable" - {
        val retrievedSubscription = subscription(retrievedActivityLiableSmallProducer)
        "change there activity values only" in {
          val caData = changeActivityData(AmountProduced.Large, true, false)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), None, true)
          val expectedModel = SdilActivity(Some(expectedActivity), Some(false), Some(false), Some(false), Some(false), None,  Some(localDate))

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }

        "changes to be a small producer who is voluntary" in {
          val caData = changeActivityData(AmountProduced.Small, false, true)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), Some(Litreage(1, 1)), false)
          val expectedModel = SdilActivity(Some(expectedActivity), None, None, None, None, None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }

        "changes to be a None producer" in {
          val caData = changeActivityData(AmountProduced.None, false, false)
          val expectedActivity = Activity(None, Some(litreage), Some(litreage), None, false)
          val expectedModel = SdilActivity(Some(expectedActivity), None, Some(false), Some(false), Some(false), None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }
      }


      "when a user who is none producer who is liable" - {
        val retrievedSubscription = subscription(retrievedActivityLiableNoneProducer)
        "change there activity values only" in {
          val caData = changeActivityData(AmountProduced.None, true, false)
          val expectedActivity = Activity(None, Some(litreage), Some(litreage), None, false)
          val expectedModel = SdilActivity(Some(expectedActivity), None, Some(false), Some(false), Some(false), None,  Some(localDate))

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }

        "changes to be a small producer who is voluntary" in {
          val caData = changeActivityData(AmountProduced.Small, false, true)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), Some(Litreage(1, 1)), false)
          val expectedModel = SdilActivity(Some(expectedActivity), None, None, None, None, None, None)

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }

        "changes to be a small producer who is liable" in {
          val caData = changeActivityData(AmountProduced.Small, true, false)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), None, false)
          val expectedModel = SdilActivity(Some(expectedActivity), None, Some(false), Some(false), Some(false), None,  Some(localDate))

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }

        "changes to be a Large producer" in {
          val caData = changeActivityData(AmountProduced.Large, true, false)
          val expectedActivity = Activity(Some(litreage), Some(litreage), Some(litreage), None, true)
          val expectedModel = SdilActivity(Some(expectedActivity), Some(false), Some(false), Some(false), Some(false), None, Some(localDate))

          val res = SdilActivity.fromChangeActivityData(caData, retrievedSubscription, localDate)

          res mustBe expectedModel
        }
      }
    }
  }

  "writes" - {
    "should convert to json correctly" in {
      val date: LocalDate = LocalDate.parse("2023-07-19")
      val activity = SdilActivity(
        activity = Some(
          Activity(
            ProducedOwnBrand = None,
            Imported = Some(Litreage(5,5)),
            CopackerAll = Some(Litreage(10,10)),
            Copackee = Some(Litreage(1,1)),
            isLarge = false)),
        produceLessThanOneMillionLitres = Some(false),
        smallProducerExemption = Some(false),
        usesContractPacker = Some(false),
        voluntarilyRegistered = Some(false),
        reasonForAmendment = None,
        taxObligationStartDate = Some(date))
      Json.toJson(activity) mustBe Json.parse(
        """
          |{"activity":{"Imported":{"lower":5,"upper":5},
          |"CopackerAll":{"lower":10,"upper":10},
          |"Copackee":{"lower":1,"upper":1},
          |"isLarge":false},"produceLessThanOneMillionLitres":false,
          |"smallProducerExemption":false,"usesContractPacker":false,
          |"voluntarilyRegistered":false,"taxObligationStartDate":"2023-07-19"}
          |""".stripMargin)
    }
  }
}
