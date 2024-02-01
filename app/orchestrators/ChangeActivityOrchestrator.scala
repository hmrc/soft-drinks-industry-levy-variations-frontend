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

package orchestrators

import cats.data.EitherT
import connectors.SoftDrinksIndustryLevyConnector
import models.UserAnswers
import models.backend.RetrievedSubscription
import models.submission.{SdilActivity, VariationsSites, VariationsSubmission}
import models.updateRegisteredDetails.ContactDetails
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ChangeActivityOrchestrator @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                                           sessionService: SessionService){

  def todaysDate: LocalDate = LocalDate.now()
  def changeActivityVariationToBeSubmitted(subscription: RetrievedSubscription,
                                                   userAnswers: UserAnswers): VariationsSubmission = {
    val changeActivityData = userAnswers.getChangeActivityData
    val contactDetails = ContactDetails.fromContact(subscription.contact)
    val variationSites = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetails)
    VariationsSubmission(
      displayOrgName = subscription.orgName,
      ppobAddress = subscription.address,
      sdilActivity = changeActivityData.map(SdilActivity.fromChangeActivityData(_, subscription, todaysDate)),
      newSites = variationSites.newSites,
      closeSites = variationSites.closedSites
    )
  }

  def submitVariation(subscription: RetrievedSubscription, userAnswers: UserAnswers)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    val changeActivityVariation = changeActivityVariationToBeSubmitted(subscription, userAnswers)

    for {
      variationSubmitted <- sdilConnector.submitVariation(changeActivityVariation, subscription.sdilRef)
        _ <- EitherT(sessionService.set(userAnswers.copy(submitted = true, submittedOn = Some(Instant.now()))))
    } yield variationSubmitted
  }
}
