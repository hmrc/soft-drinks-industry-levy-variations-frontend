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

package models.enums

import play.api.libs.json._

object SiteTypes extends Enumeration {

  val PRODUCTION_SITE = Value("Production Site")
  val WAREHOUSE = Value("Warehouse")

  implicit val format: Format[SiteTypes.Value] = new Format[SiteTypes.Value] {
    def reads(json: JsValue): JsResult[SiteTypes.Value] = json match {
      case JsString(value) =>
        SiteTypes.values.find(_.toString == value) match {
          case Some(siteType) => JsSuccess(siteType)
          case None           => JsError(s"Invalid SiteTypes value: $value")
        }
      case _ => JsError("Expected a string value for SiteTypes")
    }

    def writes(siteType: SiteTypes.Value): JsValue = JsString(siteType.toString)
  }
}
