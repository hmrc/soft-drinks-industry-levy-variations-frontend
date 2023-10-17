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

package models

import base.SpecBase
import models.backend.{Site, UkAddress}
import play.api.libs.json.{JsObject, Json}
import repositories.DatedCacheMap
import services.Encryption

import java.time.{Instant, LocalDate}

class ModelEncryptionSpec extends SpecBase {

  implicit val encryption: Encryption = application.injector.instanceOf[Encryption]

  "encryptUserAnswers" - {
    "should encrypt userAnswers" in {
      val userAnswers = UserAnswers("id",
        SelectChange.UpdateRegisteredDetails,
        Json.obj("foo" -> "bar"),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")), Some("foo"))),
        UkAddress(List("123 Main Street", "Anytown"), "AB12 C34", alfId = Some("123456")),
        None,
        false,
        Option(Instant.ofEpochSecond(1)),
        Instant.ofEpochSecond(1))

      val result = ModelEncryption.encryptUserAnswers(userAnswers)
      result._1 mustBe userAnswers.id
      result._2 mustBe userAnswers.journeyType
      Json.parse(encryption.crypto.decrypt(result._3, userAnswers.id)).as[JsObject] mustBe userAnswers.data
      Json.parse(encryption.crypto.decrypt(result._4, userAnswers.id)).as[List[SmallProducer]] mustBe userAnswers.smallProducerList
      Json.parse(encryption.crypto.decrypt(result._5.head._2, userAnswers.id)).as[Site] mustBe userAnswers.packagingSiteList.head._2
      result._5.head._1 mustBe userAnswers.packagingSiteList.head._1
      Json.parse(encryption.crypto.decrypt(result._6.head._2, userAnswers.id)).as[Site] mustBe userAnswers.warehouseList.head._2
      result._6.head._1 mustBe userAnswers.warehouseList.head._1
      Json.parse(encryption.crypto.decrypt(result._7, userAnswers.id)).as[UkAddress] mustBe userAnswers.contactAddress
      result._9 mustBe userAnswers.submitted
      result._10 mustBe userAnswers.submittedOn
      result._11 mustBe userAnswers.lastUpdated
    }
  }
  "decryptUserAnswers" - {
    "should decrypt userAnswers in tuple form" in {
      val userAnswers = UserAnswers("id",
        SelectChange.UpdateRegisteredDetails,
        Json.obj("foo" -> "bar"),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")), Some("foo"))),
        UkAddress(List("123 Main Street", "Anytown"), "AB12 C34", alfId = Some("123456")), Some(ReturnPeriod(2023, 1)),
        false,
        Option(Instant.ofEpochSecond(1)),
        Instant.ofEpochSecond(1))

     val result = ModelEncryption.decryptUserAnswers(
        userAnswers.id,
        userAnswers.journeyType,
        encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
        encryption.crypto.encrypt(Json.toJson(userAnswers.smallProducerList).toString(), userAnswers.id),
        userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
        userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
        encryption.crypto.encrypt(Json.toJson(userAnswers.contactAddress).toString(), userAnswers.id), userAnswers.correctReturnPeriod,
        userAnswers.submitted, userAnswers.submittedOn, userAnswers.lastUpdated
      )
      result mustBe userAnswers

    }
  }

  "encryptDatedCacheMap" - {
    "should encrypt correctly" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.encryptDatedCacheMap(datedCacheMap)
      result._1 mustBe datedCacheMap.id
      result._2.head._1 mustBe datedCacheMap.data.head._1
      Json.parse(encryption.crypto.decrypt(result._2.head._2, datedCacheMap.id)) mustBe datedCacheMap.data.head._2
      result._3 mustBe result._3
    }
  }

  "decryptDatedCacheMap" - {
    "should decrypt correctly" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.decryptDatedCacheMap(
        datedCacheMap.id,
        datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
        datedCacheMap.lastUpdated
      )
      result mustBe datedCacheMap
    }
  }
}
