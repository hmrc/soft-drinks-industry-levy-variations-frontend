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

import models.backend.{Site, UkAddress}
import models.submission.Litreage
import play.api.libs.json.{Format, Json}

case class ReturnsVariation(
                             orgName: String,
                             ppobAddress: UkAddress,
                             importer: (Boolean, Litreage) = (false, Litreage(0, 0)),
                             packer: (Boolean, Litreage) = (false, Litreage(0, 0)),
                             warehouses: List[Site] = Nil,
                             packingSites: List[Site] = Nil,
                             phoneNumber: String,
                             email: String,
                             taxEstimation: BigDecimal)
object ReturnsVariation {

  implicit val bllFormat: Format[(Boolean, Litreage)] = Json.format[(Boolean, Litreage)]
  implicit val format: Format[ReturnsVariation] = Json.format[ReturnsVariation]
}
