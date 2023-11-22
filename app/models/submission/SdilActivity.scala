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

import models.backend.RetrievedSubscription
import models.changeActivity.ChangeActivityData
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

case class SdilActivity(
  activity: Option[Activity] = None,
  produceLessThanOneMillionLitres: Option[Boolean] = None,
  smallProducerExemption: Option[Boolean] = None,
  usesContractPacker: Option[Boolean] = None,
  voluntarilyRegistered: Option[Boolean] = None,
  reasonForAmendment: Option[String] = None,
  taxObligationStartDate: Option[LocalDate] = None)

object SdilActivity extends VariationSubmissionHelper {

  implicit val writes: Writes[SdilActivity] = Json.writes[SdilActivity]

  def fromChangeActivityData(changeActivityData: ChangeActivityData, subscription: RetrievedSubscription, todaysDate: LocalDate): SdilActivity = {
    val changeActivityVoluntaryChangedValue = changeActivityData.isVoluntary ifDifferentTo subscription.activity.voluntaryRegistration
    val taxObligationStartDate = if (subscription.activity.voluntaryRegistration && changeActivityData.isLiable) {
      Some(todaysDate)
    } else {
      None
    }
    SdilActivity(
      Some(Activity.fromChangeActivityData(changeActivityData)),
      !changeActivityData.isLarge ifDifferentTo !subscription.activity.largeProducer,
      smallProducerExemption = changeActivityVoluntaryChangedValue,
      usesContractPacker = changeActivityVoluntaryChangedValue,
      voluntarilyRegistered = changeActivityVoluntaryChangedValue,
      None,
      taxObligationStartDate
    )
  }
}
