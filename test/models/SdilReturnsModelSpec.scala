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
import pages.correctReturn.{HowManyBroughtIntoUKPage, HowManyBroughtIntoUkFromSmallProducersPage, HowManyClaimCreditsForExportsPage, HowManyCreditsForLostDamagedPage, HowManyOperatePackagingSiteOwnBrandsPage, HowManyPackagedAsContractPackerPage}

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "SdilReturn" - {

    "totalPacked returns the total of packLarge amounrt and smallPacktotal with one smallProducers literages" in {
      val data = testSdilReturn(
        packSmall = List(testSmallProducer("test", "test", (15, 14)))
      )
      data.totalPacked mustBe(30, 26)
    }

    "totalPacked returns the total of packLarge amounrt and smallPacktotal with all smallProducers literages" in {
      val data = testSdilReturn(
        packSmall = List(testSmallProducer("test", "test", (15, 14)),
          testSmallProducer("test", "test", (15, 14)))
      )
      data.totalPacked mustBe(45, 40)
    }

    "totalImported returns the total of importLarge amount and importSmall amount" in {
      val data = testSdilReturn(
        packSmall = List.empty,
        importLarge = (15,15),
        importSmall = (15,15)
      )
      data.totalImported mustBe(30, 30)
    }

    "sumLitres returns the sum of the low and high levys of the list of one tuple" in {
      val expectedValue: BigDecimal = 6.30
      val data = testSdilReturn(
        packSmall = List.empty,
      )
      val sumList: List[(Long, Long)] = List((15, 15))

      data.sumLitres(sumList) mustBe expectedValue
    }

    "sumLitres returns the sum of the low and high levys of the list of two tuple" in {
      val expectedValue: BigDecimal = 12.60
      val data = testSdilReturn(
        packSmall = List.empty,
      )
      val sumList: List[(Long, Long)] = List((15, 15), (15, 15))

      data.sumLitres(sumList) mustBe expectedValue
    }

    "sumLitres returns the sum of the low and high levys of the list of multiple tuple" in {
      val expectedValue: BigDecimal = 18.90
      val data = testSdilReturn(
        packSmall = List.empty,
      )
      val sumList: List[(Long, Long)] = List((15, 15), (15, 15), (15, 15))

      data.sumLitres(sumList) mustBe expectedValue
    }

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 1" in {
      val expectedValue: BigDecimal = 6.30
      val data = testSdilReturn(
        packSmall = List.empty,
        ownBrand = (15, 15),
        packLarge = (15, 15),
        importLarge = (15, 15),
        `export` = (15 ,15),
        wastage = (15, 15)
      )

      data.total mustBe expectedValue
    }

    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 2" in {
      val expectedValue: BigDecimal = 25.20
      val data = testSdilReturn(
        packSmall = List.empty,
        ownBrand = (30, 30),
        packLarge = (30, 30),
        importLarge = (30, 30),
        `export` = (15, 15),
        wastage = (15, 15)
      )

      data.total mustBe expectedValue
    }
    "apply with userAnswers should default if all answers empty" in {
      SdilReturn.apply(emptyUserAnswersForCorrectReturn) mustBe SdilReturn((0, 0), (0, 0), List(), (0, 0), (0, 0), (0, 0), (0, 0), None)
    }
    "apply with full user answers should populate correctly" in {
      val userAnswers = {
        emptyUserAnswersForCorrectReturn
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1,1)).success.value
          .set(HowManyPackagedAsContractPackerPage, LitresInBands(3,4)).success.value
          .set(HowManyBroughtIntoUKPage, LitresInBands(5,6)).success.value
          .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(33,22)).success.value
          .set(HowManyClaimCreditsForExportsPage, LitresInBands(32, 22)).success.value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(22, 22)).success.value
          .copy(smallProducerList = List(SmallProducer("","", (1,1))))
      }
      SdilReturn.apply(userAnswers) mustBe SdilReturn((1, 1), (3, 4), List(SmallProducer("", "", (1, 1))), (5, 6), (33, 22), (32, 22), (22, 22), None)
    }
  }
}
