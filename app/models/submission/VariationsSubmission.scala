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

import models.backend.UkAddress
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

object VariationsSubmission {
  implicit val writes: Writes[VariationsSubmission] = Json.writes[VariationsSubmission]
}

/** The payload that is sent to GForms */
case class VariationsSubmission(
                                 tradingName: Option[String] = None,
                                 displayOrgName: String,
                                 ppobAddress: UkAddress,
                                 businessContact: Option[VariationsContact] = None,
                                 correspondenceContact: Option[VariationsContact] = None,
                                 primaryPersonContact: Option[VariationsPersonalDetails] = None,
                                 sdilActivity: Option[SdilActivity] = None,
                                 deregistrationText: Option[String] = None,
                                 deregistrationDate: Option[LocalDate] = None,
                                 newSites: List[VariationsSite] = Nil,
                                 amendSites: List[VariationsSite] = Nil,
                                 closeSites: List[ClosedSite] = Nil) {
  def nonEmpty: Boolean =
    (Seq(
      tradingName,
      businessContact,
      correspondenceContact,
      primaryPersonContact,
      sdilActivity,
      deregistrationDate,
      deregistrationText
    ).flatten ++ newSites ++ amendSites ++ closeSites).nonEmpty

  def isEmpty: Boolean = !nonEmpty
}
