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

package models.correctReturn

import models.{LitresInBands, SdilReturn}
import play.api.libs.json.Json

case class CorrectReturnUserAnswersData(
                                         operatePackagingSiteOwnBrands: Boolean,
                                         howManyOperatePackagingSiteOwnBrands: Option[LitresInBands],
                                         packagedAsContractPacker: Boolean,
                                         howManyPackagedAsContractPacker: Option[LitresInBands],
                                         exemptionsForSmallProducers: Boolean,
                                         broughtIntoUK: Boolean,
                                         howManyBroughtIntoUK: Option[LitresInBands],
                                         broughtIntoUkFromSmallProducers: Boolean,
                                         howManyBroughtIntoUkFromSmallProducers: Option[LitresInBands],
                                         claimCreditsForExports: Boolean,
                                         howManyClaimCreditsForExports: Option[LitresInBands],
                                         claimCreditsForLostDamaged: Boolean,
                                         howManyCreditsForLostDamaged: Option[LitresInBands]
                                       )

object CorrectReturnUserAnswersData {

  implicit val format = Json.format[CorrectReturnUserAnswersData]

  def fromSdilReturn(sdilReturn: SdilReturn): CorrectReturnUserAnswersData = {
    val (ownBrands, howManyOwnBrands) = getBooleanAndLitresInBands(sdilReturn.ownBrand)
    val (packLarge, howManyPackLarge) = getBooleanAndLitresInBands(sdilReturn.packLarge)
    val packSmall = sdilReturn.packSmall.nonEmpty
    val (importLarge, howManyImportLarge) = getBooleanAndLitresInBands(sdilReturn.importLarge)
    val (importSmall, howManyImportSmall) = getBooleanAndLitresInBands(sdilReturn.importSmall)
    val (exported, howManyExport) = getBooleanAndLitresInBands(sdilReturn.`export`)
    val (wastage, howManyWastage) = getBooleanAndLitresInBands(sdilReturn.wastage)

    CorrectReturnUserAnswersData(
      operatePackagingSiteOwnBrands = ownBrands,
      howManyOperatePackagingSiteOwnBrands = howManyOwnBrands,
      packagedAsContractPacker = packLarge,
      howManyPackagedAsContractPacker = howManyPackLarge,
      exemptionsForSmallProducers = packSmall,
      broughtIntoUK = importLarge,
      howManyBroughtIntoUK = howManyImportLarge,
      broughtIntoUkFromSmallProducers = importSmall,
      howManyBroughtIntoUkFromSmallProducers = howManyImportSmall,
      claimCreditsForExports = exported,
      howManyClaimCreditsForExports = howManyExport,
      claimCreditsForLostDamaged = wastage,
      howManyCreditsForLostDamaged = howManyWastage
    )
  }

  private def getBooleanAndLitresInBands(literage: (Long, Long)): (Boolean, Option[LitresInBands]) = {
    val totalLitres = literage._1 + literage._2
    if(totalLitres == 0) {
      (false, None)
    } else {
      (true, Option(LitresInBands(literage._1, literage._2)))
    }
  }

}
