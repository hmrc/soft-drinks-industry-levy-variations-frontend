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

import connectors.SoftDrinksIndustryLevyConnector
import models.UserAnswers
import models.backend.RetrievedSubscription
import models.submission.{VariationsContact, VariationsPersonalDetails, VariationsSites, VariationsSubmission}
import models.updateRegisteredDetails.ContactDetails
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import service.VariationResult
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UpdateRegisteredDetailsOrchestrator @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector) {

  private def getVariationToBeSubmitted(subscription: RetrievedSubscription,
                                                   userAnswers: UserAnswers): VariationsSubmission = {

    val optUpdatedContact = userAnswers.get(UpdateContactDetailsPage)
    val optNewPDs = optUpdatedContact.map(updatedContact => VariationsPersonalDetails.apply(updatedContact, subscription))
    val optNewBusinessContact = VariationsContact.generateForBusinessContact(userAnswers, subscription, optNewPDs)

    val siteContact = optUpdatedContact.getOrElse(ContactDetails.fromContact(subscription.contact))
    val variationSites = VariationsSites.fromUserAnswers(userAnswers, subscription, siteContact)
    VariationsSubmission(
      displayOrgName = subscription.orgName,
      ppobAddress = subscription.address,
      businessContact = optNewBusinessContact,
      correspondenceContact = optNewBusinessContact,
      primaryPersonContact = optNewPDs,
      newSites = variationSites.newSites,
      closeSites = variationSites.closedSites
    )
  }

  def submitVariation(subscription: RetrievedSubscription, userAnswers: UserAnswers)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Unit] = {
    val variationSubmission = getVariationToBeSubmitted(subscription, userAnswers)
    for {
      variation <- sdilConnector.submitVariation(variationSubmission, subscription.sdilRef)
    } yield variation
  }
}
