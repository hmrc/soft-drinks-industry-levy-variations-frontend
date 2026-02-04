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

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.VariationsSubmissionDataHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.cancelRegistration.{ CancelRegistrationDatePage, ReasonPage }
import services.SessionService

import scala.concurrent.Future

class CancelRegistrationOrchestratorSpec extends SpecBase with MockitoSugar with VariationsSubmissionDataHelper {

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService = mock[SessionService]

  val cancelRegistrationOrchestrator = new CancelRegistrationOrchestrator(mockConnector, mockSessionService)

  "submitVariation" - {
    "should send the submission variation including the dereg reason and date" - {
      "then return unit" in {

        val userAnswers = emptyUserAnswersForCancelRegistration
          .set(ReasonPage, DEREG_REASON)
          .success
          .value
          .set(CancelRegistrationDatePage, DEREG_DATE)
          .success
          .value

        val expectedSubscription = testVariationSubmission(isDeregistered = true)

        when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))
        when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

        val res = cancelRegistrationOrchestrator.submitVariationAndUpdateSession(aSubscription, userAnswers)(hc, ec)
        whenReady(res.value) { result =>
          result mustEqual Right((): Unit)
        }
      }
    }
  }
}
