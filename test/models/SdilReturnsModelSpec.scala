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
import models.submission.Litreage
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn._

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "SdilReturn" - {

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 1" in {
      val expectedValue: BigDecimal = 6.30
      val data = testSdilReturn(
        packSmall = List.empty,
        ownBrand = Litreage(15, 15),
        packLarge = Litreage(15, 15),
        importLarge = Litreage(15, 15),
        `export` = Litreage(15 ,15),
        wastage = Litreage(15, 15)
      )

      data.total mustBe expectedValue
    }

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 2" in {
      val expectedValue: BigDecimal = 25.20
      val data = testSdilReturn(
        packSmall = List.empty,
        ownBrand = Litreage(30, 30),
        packLarge = Litreage(30, 30),
        importLarge = Litreage(30, 30),
        `export` = Litreage(15, 15),
        wastage = Litreage(15, 15)
      )

      data.total mustBe expectedValue
    }
    "generateFromUserAnswers with userAnswers should default if all answers empty" in {
      SdilReturn.generateFromUserAnswers(emptyUserAnswersForCorrectReturn) mustBe SdilReturn(Litreage(0, 0), Litreage(0, 0),
        List(), Litreage(0, 0), Litreage(0, 0), Litreage(0, 0), Litreage(0, 0), None)
    }
    "generateFromUserAnswers with full user answers should populate correctly" in {
      val userAnswers = {
        emptyUserAnswersForCorrectReturn
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1,1)).success.value
          .set(PackagedAsContractPackerPage, true).success.value
          .set(HowManyPackagedAsContractPackerPage, LitresInBands(3,4)).success.value
          .set(ExemptionsForSmallProducersPage, true).success.value
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUKPage, LitresInBands(5,6)).success.value
          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
          .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(33,22)).success.value
          .set(ClaimCreditsForExportsPage, true).success.value
          .set(HowManyClaimCreditsForExportsPage, LitresInBands(32, 22)).success.value
          .set(ClaimCreditsForLostDamagedPage, true).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(22, 22)).success.value
          .copy(smallProducerList = List(SmallProducer("","", Litreage(1,1))))
      }
      SdilReturn.generateFromUserAnswers(userAnswers) mustBe SdilReturn(Litreage(1, 1), Litreage(3, 4),
        List(SmallProducer("", "", Litreage(1, 1))), Litreage(5, 6), Litreage(33, 22), Litreage(32, 22), Litreage(22, 22), None)
    }
  }
}
