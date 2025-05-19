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
//  TODO: total, taxEstimation, and calculateLevy will be updated - can change visibility on calculateLevy
  def total(implicit config: FrontendAppConfig): BigDecimal = {
    val litresToAdd = Litreage.sum(List(ownBrand, packLarge, importLarge))
    val litresToSubtract = Litreage.sum(List(export, wastage))
    val totalLiterage = Litreage(
      litresToAdd.lower - litresToSubtract.lower,
      litresToAdd.higher - litresToSubtract.higher
    )
    calculatelevy(totalLiterage)
  }

  def taxEstimation(implicit config: FrontendAppConfig): BigDecimal = {
    val t = Litreage.sum(List(packLarge, importLarge, ownBrand))
    calculatelevy(t.combineN(4))
  }
  def calculatelevy(litreage: Litreage)
                   (implicit config: FrontendAppConfig): BigDecimal = {
    val costLower = config.lowerBandCostPerLitre
    val costHigher = config.higherBandCostPerLitre
    (litreage.lower * costLower) + (litreage.higher * costHigher)
  }
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
