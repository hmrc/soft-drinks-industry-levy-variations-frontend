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

package services

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.changeActivity.AmountProduced.Large
import models.{DataHelper, Litreage, LitresInBands, VariationsSubmission}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.changeActivity._
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import java.time.LocalDate
import scala.concurrent.Future



class ChangeActivityServiceSpec extends SpecBase with MockitoSugar with DataHelper{

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]

  val changeActivityService = new ChangeActivityService(mockConnector)

  "submitVariation" - {

    "must return not through a exception after successful submission " in {
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
        packageOwn = Some(true),
        packageOwnVol= Some(Litreage(100, 100)),
        copackForOthers = true,
        copackForOthersVol = Some(Litreage(100, 100)),
        imports = true,
        importsVol = Some(Litreage(100, 100)),
      ))

      val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, Large).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100L, 100L)).success.value
        .set(ContractPackingPage, true).success.value
        .set(HowManyContractPackingPage, LitresInBands(100 , 100)).success.value
        .set(ImportsPage, true).success.value
        .set(HowManyImportsPage, LitresInBands(100, 100)).success.value
        .copy(packagingSiteList = Map.empty,
              warehouseList = Map.empty)

      when(mockConnector.submitVariation(data, "testref")(hc)).thenReturn(Future.successful(Some(200)))
      lazy val res = changeActivityService.submitVariation(subscription = retrievedSubData, userAnswers = userAnswers)

      whenReady(res) { result =>
        result mustBe ((): Unit)
      }
    }

  }

}
