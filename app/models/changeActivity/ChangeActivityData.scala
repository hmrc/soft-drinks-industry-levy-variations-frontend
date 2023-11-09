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

package models.changeActivity

import models.LitresInBands
import play.api.libs.json.{Format, Json}

case class ChangeActivityData(
                               amountProduced: AmountProduced,
                               thirdPartyPackagers: Option[Boolean] = None,
                               operatePackagingSiteOwnBrands: Option[Boolean] = None,
                               howManyOperatePackagingSiteOwnBrands: Option[LitresInBands] = None,
                               contractPacking: Option[Boolean] = None,
                               howManyContractPacking: Option[LitresInBands] = None,
                               imports: Option[Boolean] = None,
                               howManyImports: Option[LitresInBands] = None
                             ) {
  def isLarge: Boolean = amountProduced == AmountProduced.Large
  def isSmall: Boolean = amountProduced == AmountProduced.Small
  def hasImported: Boolean = imported.fold(false)(_.total > 0)
  def copackForOthers: Boolean = copackerAll.fold(false)(_.total > 0)

  def isCopackee: Boolean = isSmall && thirdPartyPackagers.contains(true)

  def ownBrandsProduced: Option[LitresInBands] = if(isLarge || isSmall) {
    getOptLitres(operatePackagingSiteOwnBrands, howManyOperatePackagingSiteOwnBrands)
  } else {
    None
  }
  def imported: Option[LitresInBands] = getOptLitres(imports, howManyImports)
  def copackerAll: Option[LitresInBands] = getOptLitres(contractPacking, howManyContractPacking)

  private def getOptLitres(ynAnswer: Option[Boolean], litersAnswer: Option[LitresInBands]): Option[LitresInBands] = {
    ynAnswer.fold[Option[LitresInBands]](None)(hasLitres =>
      if (hasLitres) {
        litersAnswer
      } else {
        None
      })
  }

  def nonEmpty = Seq(ownBrandsProduced, imported, copackerAll).flatten.nonEmpty || isCopackee
//
//  def isLiablePacker: Boolean =
//    isLarge || copackForOthers

  def isLiable: Boolean =
    isLarge || hasImported || copackForOthers

  def isVoluntary: Boolean =
    isCopackee && !isLarge && !isLiable

}

object ChangeActivityData {
  implicit val format: Format[ChangeActivityData] = Json.format[ChangeActivityData]
}
