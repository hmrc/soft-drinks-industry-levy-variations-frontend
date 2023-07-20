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

package models.changeActivity.submission

import base.SpecBase
import models.backend.UkAddress
import models.changeActivity.AmountProduced
import models.{Contact, Litreage, LitresInBands, RetrievedActivity, RetrievedSubscription}
import pages.changeActivity._
import play.api.libs.json.Json

import java.time.LocalDate

class SdilActivitySpec extends SpecBase {

  val localDate: LocalDate = LocalDate.now()

  val subscription = RetrievedSubscription(
    "foo", "bar", "wizz", UkAddress(List.empty, ""),
    RetrievedActivity(smallProducer = false, largeProducer = false, contractPacker = false, importer = false, voluntaryRegistration = false),
    LocalDate.now(), productionSites = List.empty, warehouseSites = List.empty, contact = Contact(None,None,"",""), deregDate = None)

  "convert" - {
  "should return the case class if they are NOT liable but voluntary" in {

    val userAnswers = {
      emptyUserAnswersForChangeActivity
        .set(AmountProducedPage, AmountProduced.None).success.value
        .set(ImportsPage, false).success.value
        .set(ContractPackingPage, false).success.value
        .set(ThirdPartyPackagersPage, false).success.value
    }
    val res = SdilActivity.convert(userAnswers, subscription, localDate)
    res.isDefined mustBe true
  }
    "should return the case class if they are liable but NOT voluntary" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.Large).success.value
          .set(ImportsPage, false).success.value
          .set(ContractPackingPage, false).success.value
          .set(ThirdPartyPackagersPage, true).success.value
      }

      val res = SdilActivity.convert(userAnswers, subscription, localDate)
      res.isDefined mustBe true
    }
    "should not return case class if NOT liable and NOT voluntary" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.None).success.value
          .set(ImportsPage, false).success.value
          .set(ContractPackingPage, false).success.value
          .set(ThirdPartyPackagersPage, true).success.value
      }

      val res = SdilActivity.convert(userAnswers, subscription, localDate)
      res.isDefined mustBe false
    }
    "should return case class if all answers different to subscription and is voluntary" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.None).success.value
          .set(ImportsPage, false).success.value
          .set(ContractPackingPage, false).success.value
          .set(ThirdPartyPackagersPage, false).success.value
      }
      val subscriptionUpdated = subscription.copy(
        activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = false)
      )

      val res = SdilActivity.convert(userAnswers, subscriptionUpdated, localDate)
      res.get mustBe SdilActivity(
        activity = Some(
          Activity(
            ProducedOwnBrand = None,
            Imported = None,
            CopackerAll = None,
            Copackee = None,
            isLarge = false)),
        produceLessThanOneMillionLitres = Some(false),
        smallProducerExemption = Some(true),
        usesContractPacker = Some(true),
        voluntarilyRegistered = Some(true),
        reasonForAmendment = None,
        taxObligationStartDate = None)
    }
    "should return case class if all answers different to subscription and is not voluntary" in {

      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.Large).success.value
          .set(ImportsPage, false).success.value
          .set(ContractPackingPage, false).success.value
          .set(ThirdPartyPackagersPage, true).success.value
      }
      val subscriptionUpdated = subscription.copy(
        activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true)
      )

      val res = SdilActivity.convert(userAnswers, subscriptionUpdated, localDate)
      res.get mustBe SdilActivity(
        activity = Some(
          Activity(
            ProducedOwnBrand = None,
            Imported = None,
            CopackerAll = None,
            Copackee = Some(Litreage(1,1)),
            isLarge = true)),
        produceLessThanOneMillionLitres = None,
        smallProducerExemption = Some(false),
        usesContractPacker = Some(false),
        voluntarilyRegistered = Some(false),
        reasonForAmendment = None,
        taxObligationStartDate = Some(localDate))
    }
    "return case class if all answered with litres with amount produced large" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.Large).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(5,5)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(10,10)).success.value
          .set(ThirdPartyPackagersPage, true).success.value
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(11,11)).success.value
      }
      val subscriptionUpdated = subscription.copy(
        activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true)
      )

      val res = SdilActivity.convert(userAnswers, subscriptionUpdated, localDate)
      res.get mustBe SdilActivity(
        activity = Some(
          Activity(
            ProducedOwnBrand = Some(Litreage(11,11)),
            Imported = Some(Litreage(5,5)),
            CopackerAll = Some(Litreage(10,10)),
            Copackee = Some(Litreage(1,1)),
            isLarge = true)),
        produceLessThanOneMillionLitres = None,
        smallProducerExemption = Some(false),
        usesContractPacker = Some(false),
        voluntarilyRegistered = Some(false),
        reasonForAmendment = None,
        taxObligationStartDate = Some(localDate))
    }
    "return case class if all answered with litres with amount produced Small" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.Small).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(5,5)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(10,10)).success.value
          .set(ThirdPartyPackagersPage, true).success.value
      }
      val subscriptionUpdated = subscription.copy(
        activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true)
      )

      val res = SdilActivity.convert(userAnswers, subscriptionUpdated, localDate)
      res.get mustBe SdilActivity(
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
        taxObligationStartDate = Some(localDate))
    }
    "return exception if amount produced is not answered" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(5,5)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(10,10)).success.value
          .set(ThirdPartyPackagersPage, true).success.value
      }

      intercept[Exception](SdilActivity.convert(userAnswers, subscription, localDate))
    }
  }
  "activity.nonEmpty" - {
    "should return empty when all fields are empty" in {
      val activity = Activity(
            ProducedOwnBrand = None,
            Imported = None,
            CopackerAll = None,
            Copackee = None,
            isLarge = false)

      activity.nonEmpty mustBe false
    }
    "should not return empty when all fields are not empty" in {
      val activity = Activity(
        ProducedOwnBrand = Some(Litreage(1,1)),
        Imported = Some(Litreage(1,1)),
        CopackerAll = Some(Litreage(1,1)),
        Copackee = Some(Litreage(1,1)),
        isLarge = false)

      activity.nonEmpty mustBe true
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
