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
import cats.data.EitherT
import connectors.SoftDrinksIndustryLevyConnector
import errors.*
import models.correctReturn.{ CorrectReturnUserAnswersData, RepaymentMethod }
import models.submission.Litreage
import models.{ LitresInBands, ReturnPeriod, SdilReturn, SelectChange, SmallProducer, UserAnswers }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.*
import play.api.libs.json.{ JsString, Json, Writes }
import services.{ ReturnService, SessionService }

import java.time.{ Instant, LocalDateTime, ZoneId, ZoneOffset }
import scala.concurrent.Future
import scala.util.{ Failure, Try }

class CorrectReturnOrchestratorSpec extends SpecBase with MockitoSugar {

  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockReturnsService: ReturnService = mock[ReturnService]
  val mockSessionService: SessionService = mock[SessionService]

  val emptyReturn: SdilReturn = SdilReturn(
    Litreage(),
    Litreage(),
    List.empty,
    Litreage(),
    Litreage(),
    Litreage(),
    Litreage(),
    submittedOn = Some(submittedDateTime)
  )
  val populatedReturn: SdilReturn = SdilReturn(
    Litreage(100, 200),
    Litreage(200, 100),
    smallProducerList,
    Litreage(300, 400),
    Litreage(400, 300),
    Litreage(50, 60),
    Litreage(60, 50),
    submittedOn = Some(submittedDateTime)
  )
  val sdilReturnsExamples: Map[String, SdilReturn] =
    Map("a nilReturn" -> emptyReturn, "not a nilReturn" -> populatedReturn)
  val requestReturnPeriod: ReturnPeriod = ReturnPeriod(submittedDateTime.getYear, 1)
  def getExpectedUserAnswersCorrectReturnData(key: String): CorrectReturnUserAnswersData = if (key == "a nilReturn") {
    expectedCorrectReturnDataForNilReturn
  } else {
    expectedCorrectReturnDataForPopulatedReturn
  }

  val submittedInstant: Instant = submittedDateTime.toInstant(ZoneOffset.UTC)
  val orchestrator: CorrectReturnOrchestrator =
    new CorrectReturnOrchestrator(mockReturnsService, mockSdilConnector, mockSessionService) {
      override def instantNow: Instant = submittedInstant
    }

  "getReturnPeriods" - {
    "when the call returns variable returnPeriods" - {
      "should return the variable returns" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc))
          .thenReturn(createSuccessVariationResult(returnPeriodList))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Right(returnPeriodList)
        }
      }
    }

    "when the call returns no variable returnPeriods" - {
      "should return a NoVariableReturns errors" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc))
          .thenReturn(createSuccessVariationResult(List()))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoVariableReturns)
        }
      }
    }

    "when the call fails for variable returnPeriods" - {
      "should return a UnexpectedResponseFromSDIL errors" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc))
          .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(UnexpectedResponseFromSDIL)
        }
      }
    }
  }

  "setupUserAnswersForCorrectReturn" - {
    "when a sdil return exists" - {
      "should populate the user answers with the data, save to the database and return unit" - {
        sdilReturnsExamples.foreach { case (key, sdilReturn) =>
          s"when the sdilReturn is $key and the user answers data is empty" in {
            val returnPeriod = returnPeriodList.head
            val expectedGeneratedUA = emptyUserAnswersForCorrectReturn.copy(
              smallProducerList = if (key == "a nilReturn") { List() }
              else smallProducerList,
              data = Json.obj(("correctReturn", Json.toJson(getExpectedUserAnswersCorrectReturnData(key)))),
              correctReturnPeriod = Some(returnPeriod)
            )
            when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc))
              .thenReturn(createSuccessVariationResult(Some(sdilReturn)))
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.setupUserAnswersForCorrectReturn(
              aSubscription,
              emptyUserAnswersForCorrectReturn,
              returnPeriod
            )(hc, ec)

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }

          s"when the sdilReturn is $key and the user answers data contains some data" in {
            val returnPeriod = returnPeriodList.head
            val uaInitialData = Json.obj(("testing", JsString("I am still here")))
            val initialUserAnswers = emptyUserAnswersForCorrectReturn.copy(data = uaInitialData)
            val expectedGeneratedUA = emptyUserAnswersForCorrectReturn.copy(
              smallProducerList = if (key == "a nilReturn") {
                List()
              } else {
                smallProducerList
              },
              data = uaInitialData ++
                Json.obj(("correctReturn", Json.toJson(getExpectedUserAnswersCorrectReturnData(key)))),
              correctReturnPeriod = Some(returnPeriod)
            )
            when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc))
              .thenReturn(createSuccessVariationResult(Some(sdilReturn)))
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res =
              orchestrator.setupUserAnswersForCorrectReturn(aSubscription, initialUserAnswers, returnPeriod)(hc, ec)

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }
        }
      }

      "should return a FailedToAddDataToUserAnswers" - {
        "adding data to userAnswers fails" in {
          val failingUserAnswers = new UserAnswers(
            sdilNumber,
            SelectChange.CorrectReturn,
            contactAddress = contactAddress
          ) {
            override def setForCorrectReturn(
              correctReturnUserAnswersData: CorrectReturnUserAnswersData,
              smallProducers: List[SmallProducer],
              returnPeriod: ReturnPeriod
            )(implicit writes: Writes[CorrectReturnUserAnswersData]): Try[UserAnswers] =
              Failure[UserAnswers](new Exception(""))
          }
          when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriods.head)(hc))
            .thenReturn(createSuccessVariationResult(Some(emptyReturn)))
          val res =
            orchestrator.setupUserAnswersForCorrectReturn(aSubscription, failingUserAnswers, returnPeriods.head)(hc, ec)

          whenReady(res.value) { result =>
            result mustBe Left(FailedToAddDataToUserAnswers)
          }
        }
      }

      "should return a SessionDatabaseInsertError" - {
        "adding data to userAnswers fails" in {
          val returnPeriod = returnPeriodList.head

          when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc))
            .thenReturn(createSuccessVariationResult(Some(emptyReturn)))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

          val res = orchestrator.setupUserAnswersForCorrectReturn(
            aSubscription,
            emptyUserAnswersForCorrectReturn,
            returnPeriods.head
          )(hc, ec)

          whenReady(res.value) { result =>
            result mustBe Left(SessionDatabaseInsertError)
          }
        }
      }
    }

    "when the sdilReturn does not exist" - {
      "should return a NoSdilReturnForPeriod error" in {
        val returnPeriod = returnPeriodList.head

        when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc))
          .thenReturn(createSuccessVariationResult(None))

        val res =
          orchestrator.setupUserAnswersForCorrectReturn(aSubscription, emptyUserAnswersForCorrectReturn, returnPeriod)(
            hc,
            ec
          )

        whenReady(res.value) { result =>
          result mustBe Left(NoSdilReturnForPeriod)
        }
      }
    }
  }

  "submitReturn" - {

    "should return unit" - {
      "when the calls to submit sdilReturnVary and returns variation are successful" - {
        "and the user answers contain the correctReturn returnPeriod, all correctReturn data and the original return" - {
          List(true, false).foreach { exemptionsForSmallProducers =>
            s"and exemptions for small producers is $exemptionsForSmallProducers" in {
              val litres = LitresInBands(2000, 4000)
              val litreage = Litreage(2000, 4000)
              val returnPeriod = returnPeriodList.head
              val correctReturnUserAnswersData = CorrectReturnUserAnswersData(
                operatePackagingSiteOwnBrands = true,
                Some(litres),
                packagedAsContractPacker = true,
                Some(litres),
                exemptionsForSmallProducers = exemptionsForSmallProducers,
                broughtIntoUK = true,
                Some(litres),
                broughtIntoUkFromSmallProducers = true,
                Some(litres),
                claimCreditsForExports = true,
                Some(litres),
                claimCreditsForLostDamaged = true,
                Some(litres)
              )
              val smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000)))
              val packSmall = if (exemptionsForSmallProducers) smallProducerList else List.empty

              val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
                .copy(packagingSiteList = Map.empty, warehouseList = Map.empty)
                .setForCorrectReturn(correctReturnUserAnswersData, smallProducerList, returnPeriod)
                .success
                .value
                .set(CorrectionReasonPage, "foo")
                .success
                .value
                .set(RepaymentMethodPage, RepaymentMethod.values.head)
                .success
                .value

              val expectedRevisedReturn = SdilReturn(
                litreage,
                litreage,
                packSmall,
                litreage,
                litreage,
                litreage,
                litreage,
                submittedOn = Some(LocalDateTime.ofInstant(submittedInstant, ZoneId.of("Europe/London")))
              )

              when(
                mockReturnsService.submitSdilReturnsVary(
                  aSubscription,
                  userAnswers,
                  emptySdilReturn,
                  returnPeriod,
                  expectedRevisedReturn
                )(hc)
              ).thenReturn(EitherT.rightT[Future, VariationsErrors](()))

              when(
                mockReturnsService.submitReturnVariation(
                  aSubscription,
                  expectedRevisedReturn,
                  userAnswers,
                  correctReturnUserAnswersData,
                  returnPeriod
                )(hc)
              ).thenReturn(EitherT.rightT[Future, VariationsErrors](()))

              when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))
              val res = orchestrator.submitReturn(userAnswers, aSubscription, returnPeriod, emptySdilReturn)(hc, ec)

              whenReady(res.value) { result =>
                result mustBe Right(())
              }
            }
          }
        }
      }
    }
  }

  "separateReturnPeriodsByYear" - {
    "when the is no return period" - {
      "return an empty map" in {
        val res = orchestrator.separateReturnPeriodsByYear(List())
        res mustBe List()
      }
    }

    "when the is one return period in the list" - {
      "return a map with the year and period" in {
        val returnPeriods = List(ReturnPeriod(2023, 0))
        val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
        res mustBe List(returnPeriods)
      }
    }

    "when the is multiple return periods in the list" - {
      "that are all for the year 2022" - {
        "return a map with the year and ordered return periods when ordered" in {
          val returnPeriods = returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriods)
        }

        "return a map with the year and ordered return periods when not ordered" in {
          val returnPeriods = returnPeriodsFor2022.tail ++ List(returnPeriodsFor2022.head)
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriodsFor2022)
        }

        "return a map with the year and repeated return periods removed" in {
          val returnPeriods = returnPeriodsFor2022 ++ returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriodsFor2022)
        }
      }

      "that are all for multiple years" - {
        "return a ordered map with the return periods sorted by years when already ordered" in {
          val returnPeriods = returnPeriodsFor2022 ++ returnPeriodsFor2020
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriodsFor2022, returnPeriodsFor2020)
        }

        "return a ordered map with the return periods sorted by years when not already ordered" in {
          val returnPeriods = returnPeriodsFor2020.tail ++ returnPeriodsFor2022.tail ++ List(
            returnPeriodsFor2022.head,
            returnPeriodsFor2020.head
          )
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriodsFor2022, returnPeriodsFor2020)
        }

        "return a ordered map with the return periods sorted by years and repeated return periods removed" in {
          val returnPeriods = returnPeriodsFor2020 ++ returnPeriodsFor2022 ++ returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe List(returnPeriodsFor2022, returnPeriodsFor2020)
        }
      }
    }
  }

  "calculateAmounts" - {
    "should call the returns service and return the amounts" in {

      when(
        mockReturnsService.calculateAmounts(
          sdilReference,
          userAnswersForCorrectReturnWithEmptySdilReturn,
          requestReturnPeriod,
          emptySdilReturn
        )(hc, ec)
      ).thenReturn(createSuccessVariationResult(amounts))
      val res = orchestrator.calculateAmounts(
        sdilReference,
        userAnswersForCorrectReturnWithEmptySdilReturn,
        requestReturnPeriod,
        emptySdilReturn
      )

      whenReady(res.value) { result =>
        result mustBe Right(amounts)
      }
    }
  }

}
