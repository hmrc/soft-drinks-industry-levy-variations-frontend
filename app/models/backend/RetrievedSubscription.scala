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

import models.Contact
import models.submission.SdilActivity
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class RetrievedSubscription(
                                  utr: String,
                                  sdilRef: String,
                                  orgName: String,
                                  address: UkAddress,
                                  activity: RetrievedActivity,
                                  liabilityDate: LocalDate,
                                  productionSites: List[Site],
                                  warehouseSites: List[Site],
                                  contact: Contact,
                                  deregDate: Option[LocalDate] = None) {

  val defaultSdilAcivity: Option[SdilActivity] = if(
    activity.largeProducer || activity.importer || activity.contractPacker || activity.voluntaryRegistration
  ) {
    Some(SdilActivity())
  } else {
    None
  }
}

object RetrievedSubscription {
  implicit val format: OFormat[RetrievedSubscription] = Json.format[RetrievedSubscription]
}
