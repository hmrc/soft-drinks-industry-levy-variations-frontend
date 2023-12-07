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
import models.backend.RetrievedActivity
import models.changeActivity.{AmountProduced, ChangeActivityData}
import models.submission.{Activity, Litreage, SdilActivity}
import models.{LitresInBands, VariationsSubmissionDataHelper}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import services.SessionService
import utilities.GenericLogger

import java.time.LocalDate
import scala.concurrent.Future

class ChangeActivityOrchestratorSpec extends SpecBase with MockitoSugar with VariationsSubmissionDataHelper{

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val localDate: LocalDate = LocalDate.of(2023, 5, 6)
  val mockSessionService = mock[SessionService]
  val genericLogger = new GenericLogger

  val changeActivityOrchestrator = new ChangeActivityOrchestrator(mockConnector, mockSessionService) {
    override def todaysDate: LocalDate = localDate
  }

  val litresInBands = LitresInBands(1000L, 2000L)
  val litreage = Litreage(1000L, 2000L)

  val retrievedActivityLiableLargeProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = true,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = false
  )

  val retrievedActivityLiableSmallProducer = RetrievedActivity(
    smallProducer = true,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = true
  )


  val retrievedActivityLiableNoneProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = false
  )

  "submitVariation" - {
    "should send the expected variation and return unit" - {
      "when the user is currently a large producer" - {
        "and changes their activity levels only" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Large
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableLargeProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be small and liable" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            Some(true),
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableLargeProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
        "and changes their activity to be small and voluntary" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = true
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            Some(true),
            Some(true),
            Some(true),
            Some(true),
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableLargeProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be None" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.None
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            Some(true),
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableLargeProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }

      "when the user is currently a small voluntary producer" - {
        "and changes their activity levels only" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = true
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableSmallProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be small and liable" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            Some(false),
            Some(false),
            Some(false),
            None,
            Some(localDate)
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableSmallProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
        "and changes their activity to be large" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Large
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            Some(false),
            Some(false),
            Some(false),
            Some(false),
            None,
            Some(localDate)
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableSmallProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be None" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.None
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            Some(false),
            Some(false),
            Some(false),
            None,
            Some(localDate)
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableSmallProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }

      "when the user is currently a None producer" - {
        "and changes their activity levels only" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.None
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableNoneProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be small and liable" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity), None, None, None, None, None, None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableNoneProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
        "and changes their activity to be small and voluntary" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = true
          override val newAmountProduced: AmountProduced = AmountProduced.Small
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            None,
            Some(true),
            Some(true),
            Some(true),
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableNoneProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and changes their activity to be Large" in new ChangeActivityOrchestratorHelper {
          override val isVoluntary: Boolean = false
          override val newAmountProduced: AmountProduced = AmountProduced.Large
          override val expectedSdilActivity = SdilActivity(
            Some(expectedNewActivity),
            Some(false),
            None,
            None,
            None,
            None
          )
          val expectedSubscription = testVariationSubmission(sdilActivity = Some(expectedSdilActivity))
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val res = changeActivityOrchestrator.submitVariation(getSubscription(retrievedActivityLiableNoneProducer), userAnswers)(hc, ec)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }
    }
  }

  abstract class ChangeActivityOrchestratorHelper {
    val newAmountProduced: AmountProduced
    val isVoluntary: Boolean
    val expectedSdilActivity: SdilActivity

    def changeActivityData: ChangeActivityData = {
      if(isVoluntary) {
        ChangeActivityData(newAmountProduced, Some(true))
      } else {
        val ownBrandsLitreage = if (newAmountProduced == AmountProduced.None) {
          None
        } else {
          Some(litresInBands)
        }
        ChangeActivityData(
          newAmountProduced,
          None,
          ownBrandsLitreage.map(_ => true),
          ownBrandsLitreage,
          Some(true),
          Some(litresInBands),
          Some(true),
          Some(litresInBands)
        )
      }
    }
    def expectedNewActivity = {
      if (isVoluntary) {
        Activity(
          Copackee = Some(Litreage(1, 1)),
          isLarge = false
        )
      } else {
        val ownBrandsLitreage = if (newAmountProduced == AmountProduced.None) {
          None
        } else {
          Some(litreage)
        }
        Activity(
          ownBrandsLitreage,
          Some(litreage),
          Some(litreage),
          None, newAmountProduced == AmountProduced.Large
        )
      }
    }

    def userAnswers = emptyUserAnswersForChangeActivity.copy(
      data = Json.obj(
        "changeActivity" -> Json.toJson(changeActivityData)
      )
    )

    def getSubscription(originalActivity: RetrievedActivity) = aSubscription.copy(
      activity = originalActivity,
      productionSites = List.empty,
      warehouseSites = List.empty
    )
  }
}
