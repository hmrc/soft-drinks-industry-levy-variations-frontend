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

package models.changeActivity.submission

import models.changeActivity.AmountProduced
import models.{Litreage, RetrievedSubscription, UserAnswers}
import pages.Page
import pages.changeActivity._
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

case class SdilActivity(
  activity: Option[Activity],
  produceLessThanOneMillionLitres: Option[Boolean],
  smallProducerExemption: Option[Boolean],
  usesContractPacker: Option[Boolean],
  voluntarilyRegistered: Option[Boolean],
  reasonForAmendment: Option[String],
  taxObligationStartDate: Option[LocalDate])

object SdilActivity {

  implicit val writes: Writes[SdilActivity] = Json.writes[SdilActivity]

  def convert(userAnswers: UserAnswers, subscription: RetrievedSubscription, todaysDate: LocalDate): Option[SdilActivity] = {

    val exception = (page: Page) => new Exception(s"$page not answered for ${subscription.sdilRef} cannot formulate model SDilActivity for submission")
    val isProducer: Boolean = userAnswers.get(AmountProducedPage).map(answer => answer != AmountProduced.None).getOrElse(throw exception(AmountProducedPage))
    val isLargeProducer: Boolean = userAnswers.get(AmountProducedPage).map(answer => answer == AmountProduced.Large).getOrElse(false)
    val coPackForOthers: Boolean = userAnswers.get(HowManyContractPackingPage).map(answer => answer.lowBand + answer.highBand > 0).getOrElse(false)
    val imports: Boolean = userAnswers.get(HowManyImportsPage).map(answer => answer.lowBand + answer.highBand > 0).getOrElse(false)
    val thirdParty: Option[Litreage] = userAnswers.get(ThirdPartyPackagersPage).flatMap(answer => if(answer) Some(Litreage(1,1)) else None)

    def isLiable: Boolean =
      (isProducer && isLargeProducer) || imports || coPackForOthers
    def isVoluntary: Boolean =
      thirdParty.isEmpty && !isLargeProducer && !isLiable

    val activity: Activity = Activity(
      if (userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true) && isProducer) userAnswers.get(HowManyOperatePackagingSiteOwnBrandsPage).map(Litreage(_)) else None,
      if (imports) userAnswers.get(HowManyImportsPage).map(Litreage(_)) else None,
      if (coPackForOthers) userAnswers.get(HowManyContractPackingPage).map(Litreage(_)) else None,
      thirdParty,
      isLargeProducer
    )

    val sdilActivity: SdilActivity = SdilActivity(
      activity = if (activity.nonEmpty || activity.isLarge != subscription.activity.largeProducer) Some(activity) else None,
      produceLessThanOneMillionLitres = if (!activity.isLarge != !subscription.activity.largeProducer) Some(activity.isLarge) else None,
      smallProducerExemption = if (isVoluntary != subscription.activity.voluntaryRegistration) Some(isVoluntary) else None,
      usesContractPacker = if (isVoluntary != subscription.activity.voluntaryRegistration) Some(isVoluntary) else None,
      voluntarilyRegistered = if (isVoluntary != subscription.activity.voluntaryRegistration) Some(isVoluntary) else None,
      reasonForAmendment = None,
      taxObligationStartDate = if (subscription.activity.voluntaryRegistration && isLiable) Some(todaysDate) else None
    )
    if(isLiable || isVoluntary) {
      Some(sdilActivity)
    } else {
      None
    }
  }
}
