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

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "SdilReturn" - {
    "smallPackTotal returns the total of one smallProducers literages" in {
      val data = testSdilReturn(
        packSmall = List(testSmallProducer("test", "test", (15,14)))
      )
      data.smallPackTotal mustBe (15, 14)
    }

    "smallPackTotal returns the total of all smallProducers literages" in {
      val data = testSdilReturn(
        packSmall = List(testSmallProducer("test", "test", (15, 14)),
          testSmallProducer("test", "test", (15, 14)))
      )
      data.smallPackTotal mustBe(30, 28)
    }

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
  }

  "ReturnPeriod" - {
    "start returns the start date of the quarter" in {
      val quarter1 = testReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = testReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = testReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = testReturnPeriod(year = 2023, quarter = 3)

      quarter1.start mustBe LocalDate.of(2023, 1, 1)
      quarter2.start mustBe LocalDate.of(2023, 4, 1)
      quarter3.start mustBe LocalDate.of(2023, 7, 1)
      quarter4.start mustBe LocalDate.of(2023, 10, 1)
    }

    "end returns the end date of the quarter" in {
      val quarter1 = testReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = testReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = testReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = testReturnPeriod(year = 2023, quarter = 3)

      quarter1.end mustBe LocalDate.of(2023, 3, 31)
      quarter2.end mustBe LocalDate.of(2023, 6, 30)
      quarter3.end mustBe LocalDate.of(2023, 9, 30)
      quarter4.end mustBe LocalDate.of(2023, 12, 31)
    }

    "deadline returns the end deadline of the quarter" in {
      val quarter1 = testReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = testReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = testReturnPeriod(year = 2023, quarter = 2)
      val quarter4 = testReturnPeriod(year = 2023, quarter = 3)

      quarter1.deadline mustBe LocalDate.of(2023, 4, 30)
      quarter2.deadline mustBe LocalDate.of(2023, 7, 30)
      quarter3.deadline mustBe LocalDate.of(2023, 10, 30)
      quarter4.deadline mustBe LocalDate.of(2024, 1, 30)
    }

    "next returns the next Return" in {
      val quarter1 = testReturnPeriod(year = 2023, quarter = 0)
      val quarter2 = testReturnPeriod(year = 2023, quarter = 1)
      val quarter3 = testReturnPeriod(year = 2023, quarter = 2)

      quarter1.next mustBe ReturnPeriod(2023, 1)
      quarter2.next mustBe ReturnPeriod(2023, 2)
      quarter3.next mustBe ReturnPeriod(2023, 3)
    }

    "previous returns the Previous Return" in {
      val quarter1 = testReturnPeriod(year = 2023, quarter = 1)
      val quarter2 = testReturnPeriod(year = 2023, quarter = 2)
      val quarter3 = testReturnPeriod(year = 2023, quarter = 3)

      quarter1.previous mustBe ReturnPeriod(2023, 0)
      quarter2.previous mustBe ReturnPeriod(2023, 1)
      quarter3.previous mustBe ReturnPeriod(2023, 2)
    }

    "count returns the expected value for 2023" in {
      val expectedValue = 20
      val quarter1 = testReturnPeriod(year = 2023, quarter = 1)
      quarter1.count mustBe expectedValue
    }

    "count returns the expected value for 2022" in {
      val expectedValue = 16
      val quarter1 = testReturnPeriod(year = 2022, quarter = 1)
      quarter1.count mustBe expectedValue
    }
  }
}
