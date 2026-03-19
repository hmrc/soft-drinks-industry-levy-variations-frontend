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
import connectors.SoftDrinksIndustryLevyConnector
import models.submission.Litreage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.correctReturn._

import scala.concurrent.Future

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper with ScalaCheckPropertyChecks {

  private def getRandomLitres: Long = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: Litreage = Litreage(getRandomLitres, getRandomLitres)
  private def getRandomSdilRef(index: Int): String = s"${Math.floor(Math.random() * 1000).toLong}SdilRef$index"

  private def getSdilReturn(
    ownBrand: Litreage = Litreage(),
    packLarge: Litreage = Litreage(),
    numberOfPackSmall: Int = 0,
    importLarge: Litreage = Litreage(),
    importSmall: Litreage = Litreage(),
    `export`: Litreage = Litreage(),
    wastage: Litreage = Litreage()
  ): SdilReturn = {
    val smallProducers: Seq[SmallProducer] = (0 until numberOfPackSmall)
      .map(index => SmallProducer(getRandomSdilRef(index), getRandomSdilRef(index), getRandomLitreage))
    SdilReturn(
      ownBrand,
      packLarge,
      packSmall = smallProducers.toList,
      importLarge,
      importSmall,
      `export`,
      wastage,
      submittedOn = None
    )
  }

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val testSdilRef = "XKSDIL000000022"
  val testReturnPeriod: ReturnPeriod = ReturnPeriod(2022, 3)

  "SdilReturn" - {
    "generateFromUserAnswers with userAnswers should default if all answers empty" in {
      SdilReturn.generateFromUserAnswers(emptyUserAnswersForCorrectReturn) mustBe SdilReturn(
        Litreage(0, 0),
        Litreage(0, 0),
        List(),
        Litreage(0, 0),
        Litreage(0, 0),
        Litreage(0, 0),
        Litreage(0, 0),
        None
      )
    }
    "generateFromUserAnswers with full user answers should populate correctly" in {
      val userAnswers =
        emptyUserAnswersForCorrectReturn
          .set(OperatePackagingSiteOwnBrandsPage, true)
          .success
          .value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1))
          .success
          .value
          .set(PackagedAsContractPackerPage, true)
          .success
          .value
          .set(HowManyPackagedAsContractPackerPage, LitresInBands(3, 4))
          .success
          .value
          .set(ExemptionsForSmallProducersPage, true)
          .success
          .value
          .set(BroughtIntoUKPage, true)
          .success
          .value
          .set(HowManyBroughtIntoUKPage, LitresInBands(5, 6))
          .success
          .value
          .set(BroughtIntoUkFromSmallProducersPage, true)
          .success
          .value
          .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(33, 22))
          .success
          .value
          .set(ClaimCreditsForExportsPage, true)
          .success
          .value
          .set(HowManyClaimCreditsForExportsPage, LitresInBands(32, 22))
          .success
          .value
          .set(ClaimCreditsForLostDamagedPage, true)
          .success
          .value
          .set(HowManyCreditsForLostDamagedPage, LitresInBands(22, 22))
          .success
          .value
          .copy(smallProducerList = List(SmallProducer("", "", Litreage(1, 1))))
      SdilReturn.generateFromUserAnswers(userAnswers) mustBe SdilReturn(
        Litreage(1, 1),
        Litreage(3, 4),
        List(SmallProducer("", "", Litreage(1, 1))),
        Litreage(5, 6),
        Litreage(33, 22),
        Litreage(32, 22),
        Litreage(22, 22),
        None
      )
    }

    val posLitresInts = Gen.choose(1000, 10000000)

    "calculate leviedLitreage correctly with litres packed at own site" in {
      forAll(posLitresInts) { lowLitres =>
        forAll(posLitresInts) { highLitres =>
          val sdilReturn = getSdilReturn(ownBrand = Litreage(lowLitres, highLitres))
          sdilReturn.leviedLitreage mustBe Litreage(lowLitres, highLitres)
          sdilReturn.creditedLitreage mustBe Litreage()
        }
      }
    }

    "calculate leviedLitreage correctly with litres contract packed" in {
      forAll(posLitresInts) { lowLitres =>
        forAll(posLitresInts) { highLitres =>
          val sdilReturn = getSdilReturn(packLarge = Litreage(lowLitres, highLitres))
          sdilReturn.leviedLitreage mustBe Litreage(lowLitres, highLitres)
          sdilReturn.creditedLitreage mustBe Litreage()
        }
      }
    }

    "calculate leviedLitreage correctly with litres brought into the uk" in {
      forAll(posLitresInts) { lowLitres =>
        forAll(posLitresInts) { highLitres =>
          val sdilReturn = getSdilReturn(importLarge = Litreage(lowLitres, highLitres))
          sdilReturn.leviedLitreage mustBe Litreage(lowLitres, highLitres)
          sdilReturn.creditedLitreage mustBe Litreage()
        }
      }
    }

    "calculate creditedLitreage correctly with credits for exports" in {
      forAll(posLitresInts) { lowLitres =>
        forAll(posLitresInts) { highLitres =>
          val sdilReturn = getSdilReturn(`export` = Litreage(lowLitres, highLitres))
          sdilReturn.leviedLitreage mustBe Litreage()
          sdilReturn.creditedLitreage mustBe Litreage(lowLitres, highLitres)
        }
      }
    }

    "calculate creditedLitreage correctly with credits for wastage" in {
      forAll(posLitresInts) { lowLitres =>
        forAll(posLitresInts) { highLitres =>
          val sdilReturn = getSdilReturn(wastage = Litreage(lowLitres, highLitres))
          sdilReturn.leviedLitreage mustBe Litreage()
          sdilReturn.creditedLitreage mustBe Litreage(lowLitres, highLitres)
        }
      }
    }

    "total should call connector and return totalRoundedDown" in {
      val sdilReturn = getSdilReturn(ownBrand = Litreage(1000, 2000))
      val expectedCalc =
        LevyCalculation(BigDecimal("180.00"), BigDecimal("480.00"), BigDecimal("660.00"), BigDecimal("660.00"))
      when(mockConnector.calculateLevy(any(), any(), any(), any())(using any()))
        .thenReturn(Future.successful(expectedCalc))

      val result = sdilReturn.total(testSdilRef, mockConnector, testReturnPeriod)
      whenReady(result) { total =>
        total mustBe BigDecimal("660.00")
      }
    }

    "taxEstimation should call connector with combineN(4) litreage and return totalRoundedDown" in {
      val sdilReturn = getSdilReturn(ownBrand = Litreage(1000, 2000))
      val expectedCalc =
        LevyCalculation(BigDecimal("720.00"), BigDecimal("1920.00"), BigDecimal("2640.00"), BigDecimal("2640.00"))
      when(mockConnector.calculateLevy(any(), any(), any(), any())(using any()))
        .thenReturn(Future.successful(expectedCalc))

      val result = sdilReturn.taxEstimation(testSdilRef, mockConnector, testReturnPeriod)
      whenReady(result) { estimation =>
        estimation mustBe BigDecimal("2640.00")
      }
    }
  }
}
