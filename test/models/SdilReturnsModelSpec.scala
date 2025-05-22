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
//  TODO: Adds tests for total and taxEstimation in here

  override implicit val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  private def getRandomLitres: Long = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: Litreage = Litreage(getRandomLitres, getRandomLitres)
  private def getRandomSdilRef(index: Int): String = s"${Math.floor(Math.random() * 1000).toLong}SdilRef$index"

  private def getLitresJson(boolFieldKey: String, litreageFieldKey: String)(litresOpt: Option[(Long, Long)]): JsObject = {
    litresOpt match {
      case Some((low, high)) =>
        JsObject(Seq(
          boolFieldKey -> JsBoolean(true),
          litreageFieldKey -> Json.obj("lowBand" -> low, "highBand" -> high))
        )
      case None => JsObject(Seq(boolFieldKey -> JsBoolean(false)))
    }
  }

//  private def userAnswersData(
//                               ownBrandsLitres: Option[(Long, Long)] = None,
//                               contractPackerLitres: Option[(Long, Long)] = None,
//                               broughtIntoUKLitres: Option[(Long, Long)] = None,
//                               broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None,
//                               claimCreditsForExportsLitres: Option[(Long, Long)] = None,
//                               claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None,
//                               smallProducerLitres: List[(Long, Long)] = List.empty,
//                               returnPeriod: ReturnPeriod
//                             ): UserAnswers = {
//
//    val ownBrandsJson = getLitresJson(boolFieldKey = "ownBrands", litreageFieldKey = "brandsPackagedAtOwnSites")(ownBrandsLitres)
//    val contractPackerJson = getLitresJson(boolFieldKey = "packagedContractPacker", litreageFieldKey = "howManyAsAContractPacker")(contractPackerLitres)
//    val broughtIntoUKJson = getLitresJson(boolFieldKey = "broughtIntoUK", litreageFieldKey = "HowManyBroughtIntoUk")(broughtIntoUKLitres)
//    val broughtIntoUkFromSmallProducersJson = getLitresJson(boolFieldKey = "broughtIntoUkFromSmallProducers", litreageFieldKey = "howManyBroughtIntoTheUKFromSmallProducers")(broughtIntoUkFromSmallProducersLitres)
//    val claimCreditsForExportsJson = getLitresJson(boolFieldKey = "claimCreditsForExports", litreageFieldKey = "howManyCreditsForExport")(claimCreditsForExportsLitres)
//    val claimCreditsForLostDamagedJson = getLitresJson(boolFieldKey = "claimCreditsForLostDamaged", litreageFieldKey = "howManyCreditsForLostDamaged")(claimCreditsForLostDamagedLitres)
//    val data = JsObject(
//      ownBrandsJson.fields ++
//        contractPackerJson.fields++
//        broughtIntoUKJson.fields ++
//        broughtIntoUkFromSmallProducersJson.fields ++
//        claimCreditsForExportsJson.fields ++
//        claimCreditsForLostDamagedJson.fields
//    )
//    val smallProducerList = smallProducerLitres.size match {
//      case 2 =>
//        val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", Litreage(smallProducerLitres.head._1, smallProducerLitres.head._2))
//        val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", Litreage(smallProducerLitres.last._1, smallProducerLitres.last._2))
//        List(superCola, sparkyJuice)
//      case _ => List.empty
//    }
//    emptyUserAnswersForCorrectReturn.copy(data = data, smallProducerList = smallProducerList, correctReturnPeriod = Option(returnPeriod))
//  }

  private def getSdilReturn(
                     ownBrand: Litreage = Litreage(),
                     packLarge: Litreage = Litreage(),
                     numberOfPackSmall: Int = 0,
                     importLarge: Litreage = Litreage(),
                     importSmall: Litreage = Litreage(),
                     export: Litreage = Litreage(),
                     wastage: Litreage = Litreage()
                   ): SdilReturn = {
    val smallProducers: Seq[SmallProducer] = (0 to numberOfPackSmall)
      .map(index => SmallProducer(getRandomSdilRef(index), getRandomSdilRef(index), getRandomLitreage))
    SdilReturn(ownBrand, packLarge, packSmall = smallProducers.toList, importLarge, importSmall, export, wastage, submittedOn = None)
  }


  "SdilReturn" - {

//    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 1" in {
//      val expectedValue: BigDecimal = 6.30
//      val data = testSdilReturn(
//        packSmall = List.empty,
//        ownBrand = Litreage(15, 15),
//        packLarge = Litreage(15, 15),
//        importLarge = Litreage(15, 15),
//        `export` = Litreage(15 ,15),
//        wastage = Litreage(15, 15)
//      )
//
//      data.total mustBe expectedValue
//    }
//
//    "total returns the sumLitres(ownBrand, packLarge, importLarge) minus sumLitres(export, wastage) example 2" in {
//      val expectedValue: BigDecimal = 25.20
//      val data = testSdilReturn(
//        packSmall = List.empty,
//        ownBrand = Litreage(30, 30),
//        packLarge = Litreage(30, 30),
//        importLarge = Litreage(30, 30),
//        `export` = Litreage(15, 15),
//        wastage = Litreage(15, 15)
//      )
//
//      data.total mustBe expectedValue
//    }
//    "generateFromUserAnswers with userAnswers should default if all answers empty" in {
//      SdilReturn.generateFromUserAnswers(emptyUserAnswersForCorrectReturn) mustBe SdilReturn(Litreage(0, 0), Litreage(0, 0),
//        List(), Litreage(0, 0), Litreage(0, 0), Litreage(0, 0), Litreage(0, 0), None)
//    }
//    "generateFromUserAnswers with full user answers should populate correctly" in {
//      val userAnswers = {
//        emptyUserAnswersForCorrectReturn
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1,1)).success.value
//          .set(PackagedAsContractPackerPage, true).success.value
//          .set(HowManyPackagedAsContractPackerPage, LitresInBands(3,4)).success.value
//          .set(ExemptionsForSmallProducersPage, true).success.value
//          .set(BroughtIntoUKPage, true).success.value
//          .set(HowManyBroughtIntoUKPage, LitresInBands(5,6)).success.value
//          .set(BroughtIntoUkFromSmallProducersPage, true).success.value
//          .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(33,22)).success.value
//          .set(ClaimCreditsForExportsPage, true).success.value
//          .set(HowManyClaimCreditsForExportsPage, LitresInBands(32, 22)).success.value
//          .set(ClaimCreditsForLostDamagedPage, true).success.value
//          .set(HowManyCreditsForLostDamagedPage, LitresInBands(22, 22)).success.value
//          .copy(smallProducerList = List(SmallProducer("","", Litreage(1,1))))
//      }
//      SdilReturn.generateFromUserAnswers(userAnswers) mustBe SdilReturn(Litreage(1, 1), Litreage(3, 4),
//        List(SmallProducer("", "", Litreage(1, 1))), Litreage(5, 6), Litreage(33, 22), Litreage(32, 22), Litreage(22, 22), None)
//    }

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              lowBandLitres mustEqual lowLitres
//              highBandLitres mustBe highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
//              val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
//              val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = BigDecimal("0.00")
//              val expectedHighLevy = BigDecimal("0.00")
//              lowBandLitres mustBe 0L
//              highBandLitres mustBe 0L
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe lowLitres
//              highBandLitres mustBe highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = BigDecimal("0.00")
//              val expectedHighLevy = BigDecimal("0.00")
//              lowBandLitres mustBe 0L
//              highBandLitres mustBe 0L
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe -1 * lowLitres
//              highBandLitres mustBe -1 * highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe -1 * lowLitres
//              highBandLitres mustBe -1 * highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy = BigDecimal("0.00")
//            val expectedHighLevy = BigDecimal("0.00")
//            lowBandLitres mustBe 0L
//            highBandLitres mustBe 0L
//            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) None else Option(getRandomLitreage)
//            val contractPackerLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUKLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              ownBrandLowLitres <- else ownBrandsLitres.map(_._1)
//              contractPackerLowLitres <- contractPackerLitres.map(_._1)
//              broughtIntoUKLowLitres <- broughtIntoUKLitres.map(_._1)
//            } yield (ownBrandLowLitres + contractPackerLowLitres + broughtIntoUKLowLitres) * lowerBandCostPerLitre
//            val expectedHighLevy: Option[BigDecimal] = for {
//              ownBrandHighLitres <- else ownBrandsLitres.map(_._2)
//              contractPackerHighLitres <- contractPackerLitres.map(_._2)
//              broughtIntoUKHighLitres <- broughtIntoUKLitres.map(_._2)
//            } yield (ownBrandHighLitres + contractPackerHighLitres + broughtIntoUKHighLitres) * higherBandCostPerLitre
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitre
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitre
//            totalForQuarter mustBe expectedLowLevy.get + expectedHighLevy.get
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsLowLitres <- claimCreditsForExportsLitres.map(_._1)
//              claimCreditsForLostDamagedLowLitres <- claimCreditsForLostDamagedLitres.map(_._1)
//            } yield -1 * (claimCreditsForExportsLowLitres + claimCreditsForLostDamagedLowLitres) * lowerBandCostPerLitre
//            val expectedHighLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsHighLitres <- claimCreditsForExportsLitres.map(_._2)
//              claimCreditsForLostDamagedHighLitres <- claimCreditsForLostDamagedLitres.map(_._2)
//            } yield -1 * (claimCreditsForExportsHighLitres + claimCreditsForLostDamagedHighLitres) * higherBandCostPerLitre
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitre
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitre
//            totalForQuarter mustBe expectedLowLevy.get + expectedHighLevy.get
//          }
//        }

      s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy =  lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe lowLitres
//              highBandLitres mustBe highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe lowLitres
//              highBandLitres mustBe highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
//              val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
//              val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = BigDecimal("0.00")
//              val expectedHighLevy = BigDecimal("0.00")
//              lowBandLitres mustBe 0L
//              highBandLitres mustBe 0L
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe lowLitres
//              highBandLitres mustBe highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = BigDecimal("0.00")
//              val expectedHighLevy = BigDecimal("0.00")
//              lowBandLitres mustBe 0L
//              highBandLitres mustBe 0L
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe -1 * lowLitres
//              highBandLitres mustBe -1 * highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//              val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//              val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//              val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//              val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
//              lowBandLitres mustBe -1 * lowLitres
//              highBandLitres mustBe -1 * highLitres
//              totalForQuarter mustBe expectedLowLevy + expectedHighLevy

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

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Jan - Mar ${year + 1}" in {
//          forAll(janToMarInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
//            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy = BigDecimal("0.00")
//            val expectedHighLevy = BigDecimal("0.00")
//            lowBandLitres mustBe 0L
//            highBandLitres mustBe 0L
//            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Jan - Mar ${year + 1}" in {
//          forAll(janToMarInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) None else Option(getRandomLitreage)
//            val contractPackerLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUKLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              ownBrandLowLitres <- else ownBrandsLitres.map(_._1)
//              contractPackerLowLitres <- contractPackerLitres.map(_._1)
//              broughtIntoUKLowLitres <- broughtIntoUKLitres.map(_._1)
//            } yield (ownBrandLowLitres + contractPackerLowLitres + broughtIntoUKLowLitres) * lowerBandCostPerLitre
//            val expectedHighLevy: Option[BigDecimal] = for {
//              ownBrandHighLitres <- else ownBrandsLitres.map(_._2)
//              contractPackerHighLitres <- contractPackerLitres.map(_._2)
//              broughtIntoUKHighLitres <- broughtIntoUKLitres.map(_._2)
//            } yield (ownBrandHighLitres + contractPackerHighLitres + broughtIntoUKHighLitres) * higherBandCostPerLitre
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitre
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitre
//            totalForQuarter mustBe expectedLowLevy.get + expectedHighLevy.get
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Jan - Mar ${year + 1}" in {
//          forAll(janToMarInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsLowLitres <- claimCreditsForExportsLitres.map(_._1)
//              claimCreditsForLostDamagedLowLitres <- claimCreditsForLostDamagedLitres.map(_._1)
//            } yield -1 * (claimCreditsForExportsLowLitres + claimCreditsForLostDamagedLowLitres) * lowerBandCostPerLitre
//            val expectedHighLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsHighLitres <- claimCreditsForExportsLitres.map(_._2)
//              claimCreditsForLostDamagedHighLitres <- claimCreditsForLostDamagedLitres.map(_._2)
//            } yield -1 * (claimCreditsForExportsHighLitres + claimCreditsForLostDamagedHighLitres) * higherBandCostPerLitre
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitre
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitre
//            totalForQuarter mustBe expectedLowLevy.get + expectedHighLevy.get
//          }
//        }
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
//                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
//                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = BigDecimal("0.00")
//                val expectedHighLevy = BigDecimal("0.00")
//                lowBandLitres mustBe 0L
//                highBandLitres mustBe 0L
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = BigDecimal("0.00")
//                val expectedHighLevy = BigDecimal("0.00")
//                lowBandLitres mustBe 0L
//                highBandLitres mustBe 0L
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe -1 * lowLitres
//                highBandLitres mustBe -1 * highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Apr - Dec $year" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe -1 * lowLitres
//                highBandLitres mustBe -1 * highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy = BigDecimal("0.00")
//            val expectedHighLevy = BigDecimal("0.00")
//            lowBandLitres mustBe 0L
//            highBandLitres mustBe 0L
//            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) None else Option(getRandomLitreage)
//            val contractPackerLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUKLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              ownBrandLowLitres <- else ownBrandsLitres.map(_._1)
//              contractPackerLowLitres <- contractPackerLitres.map(_._1)
//              broughtIntoUKLowLitres <- broughtIntoUKLitres.map(_._1)
//            } yield (ownBrandLowLitres + contractPackerLowLitres + broughtIntoUKLowLitres) * lowerBandCostPerLitreMap(year)
//            val expectedHighLevy: Option[BigDecimal] = for {
//              ownBrandHighLitres <- else ownBrandsLitres.map(_._2)
//              contractPackerHighLitres <- contractPackerLitres.map(_._2)
//              broughtIntoUKHighLitres <- broughtIntoUKLitres.map(_._2)
//            } yield (ownBrandHighLitres + contractPackerHighLitres + broughtIntoUKHighLitres) * higherBandCostPerLitreMap(year)
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitreMap(year)
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitreMap(year)
//            totalForQuarter mustBe (expectedLowLevy.get + expectedHighLevy.get).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Apr - Dec $year" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsLowLitres <- claimCreditsForExportsLitres.map(_._1)
//              claimCreditsForLostDamagedLowLitres <- claimCreditsForLostDamagedLitres.map(_._1)
//            } yield -1 * (claimCreditsForExportsLowLitres + claimCreditsForLostDamagedLowLitres) * lowerBandCostPerLitreMap(year)
//            val expectedHighLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsHighLitres <- claimCreditsForExportsLitres.map(_._2)
//              claimCreditsForLostDamagedHighLitres <- claimCreditsForLostDamagedLitres.map(_._2)
//            } yield -1 * (claimCreditsForExportsHighLitres + claimCreditsForLostDamagedHighLitres) * higherBandCostPerLitreMap(year)
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitreMap(year)
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitreMap(year)
//            totalForQuarter mustBe (expectedLowLevy.get + expectedHighLevy.get).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
//                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
//                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = BigDecimal("0.00")
//                val expectedHighLevy = BigDecimal("0.00")
//                lowBandLitres mustBe 0L
//                highBandLitres mustBe 0L
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe lowLitres
//                highBandLitres mustBe highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = BigDecimal("0.00")
//                val expectedHighLevy = BigDecimal("0.00")
//                lowBandLitres mustBe 0L
//                highBandLitres mustBe 0L
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe -1 * lowLitres
//                highBandLitres mustBe -1 * highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(posLitresInts) { lowLitres =>
//            forAll(posLitresInts) { highLitres =>
//              forAll(aprToDecInt) { month =>
//                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
//                val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//                val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
//                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
//                lowBandLitres mustBe -1 * lowLitres
//                highBandLitres mustBe -1 * highLitres
//                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              }
//            }
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(janToMarInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
//            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy = BigDecimal("0.00")
//            val expectedHighLevy = BigDecimal("0.00")
//            lowBandLitres mustBe 0L
//            highBandLitres mustBe 0L
//            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(aprToDecInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) None else Option(getRandomLitreage)
//            val contractPackerLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUKLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              ownBrandLowLitres <- else ownBrandsLitres.map(_._1)
//              contractPackerLowLitres <- contractPackerLitres.map(_._1)
//              broughtIntoUKLowLitres <- broughtIntoUKLitres.map(_._1)
//            } yield (ownBrandLowLitres + contractPackerLowLitres + broughtIntoUKLowLitres) * lowerBandCostPerLitreMap(year)
//            val expectedHighLevy: Option[BigDecimal] = for {
//              ownBrandHighLitres <- else ownBrandsLitres.map(_._2)
//              contractPackerHighLitres <- contractPackerLitres.map(_._2)
//              broughtIntoUKHighLitres <- broughtIntoUKLitres.map(_._2)
//            } yield (ownBrandHighLitres + contractPackerHighLitres + broughtIntoUKHighLitres) * higherBandCostPerLitreMap(year)
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitreMap(year)
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitreMap(year)
//            totalForQuarter mustBe (expectedLowLevy.get + expectedHighLevy.get).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//          }
//        }

//        s"calculate leviedLitreage, creditedLitreage, total levy for quarter, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Jan - Mar ${year + 1}" in {
//          forAll(janToMarInt) { month =>
//            val ownBrandsLitres: Option[(Long, Long)] = None
//            val contractPackerLitres: Option[(Long, Long)] = None
//            val broughtIntoUKLitres: Option[(Long, Long)] = None
//            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None
//            val claimCreditsForExportsLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = Option(getRandomLitreage)
//            val smallProducerLitres: List[(Long, Long)] = List.empty
//            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
//            val lowBandLitres = getTotalLowBandLitres(userAnswers, isSmallProducer)
//            val highBandLitres = getTotalHighBandLitres(userAnswers, isSmallProducer)
//            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
//            val expectedLowLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsLowLitres <- claimCreditsForExportsLitres.map(_._1)
//              claimCreditsForLostDamagedLowLitres <- claimCreditsForLostDamagedLitres.map(_._1)
//            } yield -1 * (claimCreditsForExportsLowLitres + claimCreditsForLostDamagedLowLitres) * lowerBandCostPerLitreMap(year)
//            val expectedHighLevy: Option[BigDecimal] = for {
//              claimCreditsForExportsHighLitres <- claimCreditsForExportsLitres.map(_._2)
//              claimCreditsForLostDamagedHighLitres <- claimCreditsForLostDamagedLitres.map(_._2)
//            } yield -1 * (claimCreditsForExportsHighLitres + claimCreditsForLostDamagedHighLitres) * higherBandCostPerLitreMap(year)
//            lowBandLitres mustBe expectedLowLevy.get / lowerBandCostPerLitreMap(year)
//            highBandLitres mustBe expectedHighLevy.get / higherBandCostPerLitreMap(year)
//            totalForQuarter mustBe (expectedLowLevy.get + expectedHighLevy.get).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//          }
//        }
    })

  }
}
