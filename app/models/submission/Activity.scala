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

import models.changeActivity.ChangeActivityData
import play.api.libs.json.{Format, Json}

case class Activity(
                     ProducedOwnBrand: Option[Litreage] = None,
                     Imported: Option[Litreage] = None,
                     CopackerAll: Option[Litreage] = None,
                     Copackee: Option[Litreage] = None,
                     isLarge: Boolean) {
  def nonEmpty: Boolean = Seq(ProducedOwnBrand, Imported, CopackerAll, Copackee).flatten.nonEmpty
}

object Activity {
  implicit val format: Format[Activity] = Json.format[Activity]
  def fromChangeActivityData(changeActivityData: ChangeActivityData): Activity = {
    Activity(
      changeActivityData.ownBrandsProduced.map(Litreage.fromLitresInBands),
      changeActivityData.imported.map(Litreage.fromLitresInBands),
      changeActivityData.copackerAll.map(Litreage.fromLitresInBands),
      Copackee = if (changeActivityData.isCopackee) Some(Litreage(1, 1)) else None,
      changeActivityData.isLarge
    )
  }
}
