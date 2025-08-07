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

import config.FrontendAppConfig
import models.LevyCalculator.getLevyCalculation
import models.submission.Litreage
import pages.correctReturn.ExemptionsForSmallProducersPage
import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDateTime, ZoneId}

case class SdilReturn(
                       ownBrand: Litreage,
                       packLarge: Litreage,
                       packSmall: List[SmallProducer],
                       importLarge: Litreage,
                       importSmall: Litreage,
                       export: Litreage,
                       wastage: Litreage,
                       submittedOn: Option[LocalDateTime] = None
                     ) {
  private[models] val leviedLitreage: Litreage = Litreage.sum(List(ownBrand, packLarge, importLarge))

  private[models] val creditedLitreage: Litreage = Litreage.sum(List(export, wastage))

  private [models] def calculatelevy(litreage: Litreage)
                                    (implicit config: FrontendAppConfig, returnPeriod: ReturnPeriod): BigDecimal = {
    val levyCalculation: LevyCalculation = getLevyCalculation(litreage.lower, litreage.higher, returnPeriod)(config)
    levyCalculation.totalRoundedDown
  }

  def total(implicit config: FrontendAppConfig, returnPeriod: ReturnPeriod): BigDecimal = {
    val totalLiterage = Litreage(
      leviedLitreage.lower - creditedLitreage.lower,
      leviedLitreage.higher - creditedLitreage.higher
    )
    calculatelevy(totalLiterage)
  }

  def taxEstimation(implicit config: FrontendAppConfig, returnPeriod: ReturnPeriod): BigDecimal = calculatelevy(leviedLitreage.combineN(4))
}

object SdilReturn {

  def packSmallValueFromUserAnswers(userAnswers: UserAnswers): List[SmallProducer] = {
    userAnswers.get(ExemptionsForSmallProducersPage) match {
      case Some(true) => userAnswers.smallProducerList
      case _ => List.empty
    }
  }

  def generateFromUserAnswers(userAnswers: UserAnswers, submitted: Option[Instant] = None): SdilReturn = {
    userAnswers.getCorrectReturnData.map(correctReturnData => {
      SdilReturn(
        ownBrand = correctReturnData.ownBrandsLitreage,
        packLarge = correctReturnData.contractPackerLitreage,
        packSmall = packSmallValueFromUserAnswers(userAnswers),
        importLarge = correctReturnData.broughtIntoUkLitreage,
        importSmall = correctReturnData.broughtIntoUkFromSmallProducerLitreage,
        export = correctReturnData.exportsLitreage,
        wastage = correctReturnData.lostDamagedLitreage,
        submittedOn = submitted.map(instant => LocalDateTime.ofInstant(instant, ZoneId.of("Europe/London")))
      )
    }).getOrElse(emptySdilReturn(userAnswers))
  }

  def emptySdilReturn(userAnswers: UserAnswers) = SdilReturn(
    ownBrand = Litreage(0L, 0L),
    packLarge = Litreage(0L, 0L),
    packSmall = packSmallValueFromUserAnswers(userAnswers),
    importLarge = Litreage(0L, 0L),
    importSmall = Litreage(0L, 0L),
    export = Litreage(0L, 0L),
    wastage = Litreage(0L, 0L),
    submittedOn = None
  )

  implicit val smallProducerJson: OFormat[SmallProducer] = Json.format[SmallProducer]

  implicit val format: OFormat[SdilReturn] = Json.format[SdilReturn]
}
