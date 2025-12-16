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
import models.submission.VariationsSubmission
import pages.cancelRegistration.{ CancelRegistrationDatePage, ReasonPage }
import service.VariationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CancelRegistrationOrchestrator @Inject() (
  sdilConnector: SoftDrinksIndustryLevyConnector,
  sessionService: SessionService
) {

  private def getVariationToBeSubmitted(
    subscription: RetrievedSubscription,
    userAnswers: UserAnswers
  ): VariationsSubmission =
    VariationsSubmission(
      displayOrgName = subscription.orgName,
      ppobAddress = subscription.address,
      deregistrationText = userAnswers.get(ReasonPage),
      deregistrationDate = userAnswers.get(CancelRegistrationDatePage),
      sdilActivity = subscription.defaultSdilAcivity
    )

  def submitVariationAndUpdateSession(subscription: RetrievedSubscription, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): VariationResult[Unit] = {
    val cancelRegistrationVariation = getVariationToBeSubmitted(subscription, userAnswers)
    for {
      variation <- sdilConnector.submitVariation(cancelRegistrationVariation, subscription.sdilRef)
      _         <- EitherT(sessionService.set(userAnswers.copy(submitted = true, submittedOn = Some(Instant.now))))
    } yield variation
  }
}
