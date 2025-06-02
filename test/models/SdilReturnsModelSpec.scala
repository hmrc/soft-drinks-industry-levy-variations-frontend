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
import config.FrontendAppConfig
import models.submission.Litreage
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.correctReturn._
import play.api.libs.json.{JsBoolean, JsObject, Json}

import java.time.LocalDate

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper with ScalaCheckPropertyChecks {
  override implicit val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  private def getRandomLitres: Long = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: Litreage = Litreage(getRandomLitres, getRandomLitres)
  private def getRandomSdilRef(index: Int): String = s"${Math.floor(Math.random() * 1000).toLong}SdilRef$index"

  private def getSdilReturn(
                     ownBrand: Litreage = Litreage(),
                     packLarge: Litreage = Litreage(),
                     numberOfPackSmall: Int = 0,
                     importLarge: Litreage = Litreage(),
                     importSmall: Litreage = Litreage(),
                     export: Litreage = Litreage(),
                     wastage: Litreage = Litreage()
                   )(implicit returnPeriod: ReturnPeriod): SdilReturn = {
    val smallProducers: Seq[SmallProducer] = (0 to numberOfPackSmall)
      .map(index => SmallProducer(getRandomSdilRef(index), getRandomSdilRef(index), getRandomLitreage))
    SdilReturn(ownBrand, packLarge, packSmall = smallProducers.toList, importLarge, importSmall, export, wastage, submittedOn = None)
  }


  "SdilReturn" - {
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

    val posLitresInts = Gen.choose(1000, 10000000)
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {

      val lowerBandCostPerLitre = BigDecimal("0.18")
      val higherBandCostPerLitre = BigDecimal("0.24")

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(wastage = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = BigDecimal("0.00")
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedLeviedLitreage = Litreage.sum(List(ownBrandLitres, packLargeLitres, importLargeLitres))
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = lowerBandCostPerLitre * expectedLeviedLitreage.lower + higherBandCostPerLitre * expectedLeviedLitreage.higher
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * expectedLeviedLitreage.lower + higherBandCostPerLitre * expectedLeviedLitreage.higher)
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage.sum(List(exportLitres, wastageLitres))
          val expectedTotal = -1 * (lowerBandCostPerLitre * expectedCreditedLitreage.lower + higherBandCostPerLitre * expectedCreditedLitreage.higher)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(wastage = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = BigDecimal("0.00")
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedLeviedLitreage = Litreage.sum(List(ownBrandLitres, packLargeLitres, importLargeLitres))
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = lowerBandCostPerLitre * expectedLeviedLitreage.lower + higherBandCostPerLitre * expectedLeviedLitreage.higher
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * expectedLeviedLitreage.lower + higherBandCostPerLitre * expectedLeviedLitreage.higher)
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage.sum(List(exportLitres, wastageLitres))
          val expectedTotal = -1 * (lowerBandCostPerLitre * expectedCreditedLitreage.lower + higherBandCostPerLitre * expectedCreditedLitreage.higher)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val sdilReturn = getSdilReturn(wastage = Litreage(lowLitres, highLitres))
                val expectedLeviedLitreage = Litreage()
                val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
                val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
                val expectedTaxEstimation = BigDecimal("0.00")
                sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
                sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
                sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = BigDecimal("0.00")
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedLeviedLitreage = Litreage.sum(List(ownBrandLitres, packLargeLitres, importLargeLitres))
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = lowerBandCostPerLitreMap(year) * expectedLeviedLitreage.lower + higherBandCostPerLitreMap(year) * expectedLeviedLitreage.higher
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * expectedLeviedLitreage.lower + higherBandCostPerLitreMap(year) * expectedLeviedLitreage.higher)
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage.sum(List(exportLitres, wastageLitres))
          val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * expectedCreditedLitreage.lower + higherBandCostPerLitreMap(year) * expectedCreditedLitreage.higher)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(packLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(importLarge = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage(lowLitres, highLitres)
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(importSmall = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage()
              val expectedTotal = BigDecimal("0.00")
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(`export` = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(wastage = Litreage(lowLitres, highLitres))
              val expectedLeviedLitreage = Litreage()
              val expectedCreditedLitreage = Litreage(lowLitres, highLitres)
              val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)
              val expectedTaxEstimation = BigDecimal("0.00")
              sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
              sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
              sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = BigDecimal("0.00")
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedLeviedLitreage = Litreage.sum(List(ownBrandLitres, packLargeLitres, importLargeLitres))
          val expectedCreditedLitreage = Litreage()
          val expectedTotal = lowerBandCostPerLitreMap(year) * expectedLeviedLitreage.lower + higherBandCostPerLitreMap(year) * expectedLeviedLitreage.higher
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * expectedLeviedLitreage.lower + higherBandCostPerLitreMap(year) * expectedLeviedLitreage.higher)
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedLeviedLitreage = Litreage()
          val expectedCreditedLitreage = Litreage.sum(List(exportLitres, wastageLitres))
          val expectedTotal = -1 * (lowerBandCostPerLitreMap(year) * expectedCreditedLitreage.lower + higherBandCostPerLitreMap(year) * expectedCreditedLitreage.higher)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.leviedLitreage mustBe expectedLeviedLitreage
          sdilReturn.creditedLitreage mustBe expectedCreditedLitreage
          sdilReturn.total mustBe expectedTotal.setScale(2, BigDecimal.RoundingMode.HALF_UP)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }
    })

  }
}
