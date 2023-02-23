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
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate
import scala.collection.immutable.ListMap

class VariationDataModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "ReturnVariationData" - {
    "return a list of removed Small Producers from the revised sdil return" in {
      val removedSmallProder: SmallProducer = testSmallProducer(alias = "test2", sdilRef = "testRef", litreage = (12, 12))
      val revisedSmallProducerList: List[SmallProducer] = List(removedSmallProder)
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn((12, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)),
            removedSmallProder)
        ),
        revised = testSdilReturn(
          packLarge = (13, 13),
          packSmall = List(testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.removedSmallProducers mustBe revisedSmallProducerList
    }

    "return a empty list if no small provider has been removed" in {
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn((12, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))),
        revised = testSdilReturn(
          packLarge = (13, 13),
          packSmall = List(testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.removedSmallProducers mustBe List.empty
    }

    "return a list of added Small Producers from the revised sdil return" in {
      val addedSmallProvider: SmallProducer = testSmallProducer(alias = "test2", sdilRef = "testRef", litreage = (12, 12))
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn((12, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))
        ),
        revised = testSdilReturn(
          packLarge = (13, 13),
          packSmall = List(testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)),
            addedSmallProvider)),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.addedSmallProducers mustBe List(addedSmallProvider)
    }

    "return a empty list if no small provider has been added" in {
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn((12, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))),
        revised = testSdilReturn(
          packLarge = (13, 13),
          packSmall = List(testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (12, 12)))),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.addedSmallProducers mustBe List.empty
    }

    "return comparison between original and revised SDIL returns with adding one small producer with the same values" in {
      val addedSmallProvider: SmallProducer = testSmallProducer(alias = "test2", sdilRef = "testRef", litreage = (15, 12))
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn((15, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12)))
        ),
        revised = testSdilReturn(
          packLarge = (15, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12)),
            addedSmallProvider)),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.changedLitreages mustBe ListMap("exemptions-for-small-producers" -> ((30, 24), (15, 12)))
    }

    "return comparison between original and revised SDIL returns with adding different values to all sdil return" in {
      val addedSmallProvider: SmallProducer = testSmallProducer(alias = "test2", sdilRef = "testRef", litreage = (15, 12))
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn(
          ownBrand = (15, 12),
          packLarge = (15, 12),
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12))),
          importLarge = (15, 12),
          importSmall = (15, 12),
          export = (15, 12),
          wastage = (15, 12)),
        revised =
          testSdilReturn(ownBrand = (15, 12),
            packLarge = (30, 30),
            packSmall = List(
              testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12)),
              addedSmallProvider),
            importLarge = (30, 30),
            importSmall = (30, 30),
            export = (30, 30),
            wastage = (30, 30)),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.changedLitreages mustBe ListMap(
        "packaged-as-a-contract-packer" -> ((30, 30), (15, 12)),
        "exemptions-for-small-producers" -> ((30, 24), (15, 12)),
        "brought-into-uk" -> ((30, 30), (15, 12)),
        "brought-into-uk-from-small-producers" -> ((30, 30), (15, 12)),
        "claim-credits-for-exports" -> ((30, 30), (15, 12)),
        "claim-credits-for-lost-damaged" -> ((30, 30), (15, 12)))
    }

    "return comparison between original and revised when no changes are made to the figures" in {
      val data: ReturnVariationData = testReturnVariationData(
        original = testSdilReturn(
          packSmall = List(
            testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12)))
        ),
        revised =
          testSdilReturn(
            packSmall = List(
              testSmallProducer(alias = "test1", sdilRef = "testRef", litreage = (15, 12)))
          ),
        period = testReturnPeriod(),
        orgName = "test name",
        address = testUkAddress(),
        reason = "test reason",
        repaymentMethod = None
      )

      data.changedLitreages mustBe ListMap.empty
    }
  }


  "RegistrationVariationData" - {
    val retrievedActivityData = testRetrievedActivity()
    val retrievedSubData = testRetrievedSubscription(
      address = testUkAddress(),
      activity = retrievedActivityData,
      liabilityDate = LocalDate.now(),
      productionSites = List.empty,
      warehouseSites = List.empty,
      contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
    )

    "isLiablePacker returns true if producer is large" in {
        val data: RegistrationVariationData = testRegistrationVariationData(
          original = retrievedSubData,
          updatedBusinessAddress = testAddress(),
          producer = testProducer(isProducer = true, isLarge = Some(true)),
          updatedContactDetails = testContactDetails()
        )

        data.isLiablePacker mustBe true
      }

    "isLiablePacker returns true if copackForOthers is true" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        copackForOthers = true
      )

      data.isLiablePacker mustBe true
    }

    "isLiablePacker returns false if copackForOthers is false and producer is not large" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails()
      )

      data.isLiablePacker mustBe false
    }

    "isLiable returns true if  is producer and the producer is large" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = true, isLarge = Some(true)),
        updatedContactDetails = testContactDetails()
      )

      data.isLiable mustBe true
    }

    "isLiable returns true if imports is set to true" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        imports = true
      )

      data.isLiable mustBe true
    }

    "isLiable returns true if copackForOthers is set to true" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        copackForOthers = true
      )

      data.isLiable mustBe true
    }

    "isLiable returns false if not a producer or imports or copackForOthers are false" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails()
      )

      data.isLiable mustBe false
    }

    "isVoluntary returns true if usesCopacker is true, producer is not larger and not isLiable" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = true, isLarge = Some(false)),
        updatedContactDetails = testContactDetails(),
        usesCopacker = Some(true)
      )

      data.isVoluntary mustBe true
    }

    "isVoluntary returns false if usesCopacker is false, producer is not larger and not isLiable" in {
      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails()
      )

      data.isVoluntary mustBe false
    }


    "isMaterialChange returns true when the updatedContactDetails are not the same as original UpdatedContactDetails" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(fullName = "test name 2")
      )

      data.isMaterialChange mustBe true
    }

    "isMaterialChange returns true current data is isLiable is not the same as the original isLiable" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        copackForOthers = true
      )

      data.isMaterialChange mustBe true
    }

    "isMaterialChange returns true updated warehouse sites is not empty" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        updatedWarehouseSites = Seq(testSite(testUkAddress()))
      )

      data.isMaterialChange mustBe true
    }

    "isMaterialChange returns true updated production sites is not empty" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        updatedProductionSites = Seq(testSite(testUkAddress()))
      )

      data.isMaterialChange mustBe true
    }

    "isMaterialChange returns true if a deregDate is defined" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        deregDate = Some(LocalDate.now())
      )

      data.isMaterialChange mustBe true
    }

    "isMaterialChange returns false if all the above is not set" in {

      val data: RegistrationVariationData = testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails()
      )

      data.isMaterialChange mustBe false
    }
  }
}
