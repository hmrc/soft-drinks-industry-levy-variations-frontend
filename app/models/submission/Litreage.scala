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

package models.submission

import models.LitresInBands
import play.api.libs.json.{ Json, OFormat }

case class Litreage(lower: Long = 0L, higher: Long = 0L) {
  val total: BigDecimal = lower + higher

  def combineN(n: Int): Litreage = Litreage(lower * n, higher * n)
}

object Litreage {
  def fromLitresInBands(litresInBands: LitresInBands): Litreage = Litreage(
    lower = litresInBands.lowBand,
    higher = litresInBands.highBand
  )

  def sum(litreages: List[Litreage]): Litreage = {
    val totalLower = litreages.foldLeft[Long](0L)((total, litreage) => total + litreage.lower)
    val totalUpper = litreages.foldLeft[Long](0L)((total, litreage) => total + litreage.higher)
    Litreage(totalLower, totalUpper)
  }

  implicit val format: OFormat[Litreage] = Json.format[Litreage]
}
