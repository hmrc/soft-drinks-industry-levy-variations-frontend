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
import base.SpecBase.{oneProductionSite, oneWarehouses}
import models.backend.Site
import models.changeActivity.submission.{Activity, SdilActivity}
import models.updateRegisteredDetails.Submission.VariationsContact
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class ConvertSpec extends SpecBase with MockitoSugar with DataHelper {

  "Convert" - {
    "Submit variation with No changes if user hasn't made changes" in {
      val retrievedActivityData = testRetrievedActivity()
      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )
      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        deregDate = Some(LocalDate.now())))
      data.amendSites mustBe List.empty
      data.newSites mustBe List.empty
      data.closeSites mustBe List.empty
      data.sdilActivity mustBe None
    }

    "Submit variation with new warehouse" in {
      val retrievedActivityData = testRetrievedActivity()
      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )
      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        updatedWarehouseSites =   oneWarehouses.values.map(warehouse => Site(address = warehouse.address, ref = None, tradingName = warehouse.tradingName, closureDate = None)).toSeq,
        deregDate = Some(LocalDate.now())))
      data.amendSites mustBe List.empty
      data.newSites mustBe oneWarehouses.values.map(warehouse =>
        VariationsSite(
          tradingName = warehouse.tradingName.get,
          siteReference = "1",
          variationsContact = VariationsContact(
            address = Some(warehouse.address),
            telephoneNumber = Some(retrievedSubData.contact.phoneNumber),
            emailAddress = Some(retrievedSubData.contact.email)
        ),
          typeOfSite = "Warehouse")).toList
      data.closeSites mustBe List.empty
      data.sdilActivity mustBe None
    }

    "Submit variation with new packing site" in {
      val retrievedActivityData = testRetrievedActivity()
      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )
      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        updatedProductionSites =   oneProductionSite.values.toSeq,
        deregDate = Some(LocalDate.now())))
      data.amendSites mustBe List.empty
      data.newSites mustBe oneProductionSite.values.map(productionSite =>
        VariationsSite(
          tradingName = productionSite.tradingName.get,
          siteReference = "88",
          variationsContact = VariationsContact(
            address = Some(productionSite.address),
            telephoneNumber = Some(retrievedSubData.contact.phoneNumber),
            emailAddress = Some(retrievedSubData.contact.email)
          ),
          typeOfSite = "Production Site")).toList
      data.closeSites mustBe List.empty
      data.sdilActivity mustBe None
    }

    "Submit variation with new activity" in {
      val retrievedActivityData = testRetrievedActivity()
      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )
      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        deregDate = Some(LocalDate.now()),
        packageOwn = Some(true),
        packageOwnVol= Some(Litreage(100, 100)),
        copackForOthers = true,
        copackForOthersVol = Some(Litreage(200, 200)),
        imports = true,
        importsVol = Some(Litreage(300, 300)),
      ))
      data.amendSites mustBe List.empty
      data.closeSites mustBe List.empty
      data.sdilActivity mustEqual Some(SdilActivity(Some(Activity(None, Some(Litreage(300, 300)), Some(Litreage(200, 200)), None, false)), None, None, None, None, None, None))
    }
  }
}
