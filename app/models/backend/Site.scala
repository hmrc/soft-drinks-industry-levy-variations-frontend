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

package models.backend

import play.api.libs.json.{ Format, Json }

import java.time.LocalDate

case class Site(
  address: UkAddress,
  tradingName: Option[String] = None,
  ref: Option[String] = None,
  closureDate: Option[LocalDate] = None
) {
  def isSame(site: Site): Boolean =
    address.isSame(site.address) && tradingName == site.tradingName && closureDate == site.closureDate

  def isNew(originalSites: List[Site]): Boolean =
    !originalSites.exists(site => isSame(site))

  def isClosed(updatedSites: List[Site]): Boolean =
    closureDate.fold(false)(_.isBefore(LocalDate.now())) || !updatedSites.exists(site => isSame(site))
}

object Site {
  implicit val format: Format[Site] = Json.format[Site]
}
