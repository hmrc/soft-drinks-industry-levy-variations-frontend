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
import errors.{FailedToAddDataToUserAnswers, NoSdilReturnForPeriod, NoVariableReturns, SessionDatabaseInsertError, UnexpectedResponseFromSDIL}
import models.correctReturn.CorrectReturnUserAnswersData
import models.{LitresInBands, ReturnPeriod, SdilReturn, SelectChange, SmallProducer, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsString, Json, Writes}
import services.SessionService

import java.time.ZoneOffset
import scala.collection.immutable.List
import scala.concurrent.Future
import scala.util.{Failure, Try}

class CorrectReturnOrchestratorSpec extends SpecBase with MockitoSugar {

  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService: SessionService = mock[SessionService]

  val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0), submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))
  val populatedReturn = SdilReturn((100, 200), (200, 100),
    smallProducerList, (300, 400), (400, 300), (50, 60), (60, 50),
    submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))

  val expectedCorrectReturnDataForNilReturn = CorrectReturnUserAnswersData(false, None, false, None, false, false, None, false, None, false, None, false, None)
  val expectedCorrectReturnDataForPopulatedReturn = CorrectReturnUserAnswersData(
    true, Some(LitresInBands(100, 200)),
    true, Some(LitresInBands(200, 100)),
    true,
    true, Some(LitresInBands(300, 400)),
    true, Some(LitresInBands(400, 300)),
    true, Some(LitresInBands(50, 60)),
    true, Some(LitresInBands(60, 50))
  )

  val sdilReturnsExamples = Map("a nilReturn" -> emptyReturn, "not a nilReturn" -> populatedReturn)

  def getExpectedUserAnswersCorrectReturnData(key: String): CorrectReturnUserAnswersData = if (key == "a nilReturn") {
    expectedCorrectReturnDataForNilReturn
  } else {
    expectedCorrectReturnDataForPopulatedReturn
  }

  val orchestrator = new CorrectReturnOrchestrator(mockSdilConnector, mockSessionService)

  "getReturnPeriods" - {
    "when the call returns variable returnPeriods" - {
      "should return the variable returns" in {
        when(mockSdilConnector.returnsVariable(aSubscription.utr)(hc)).thenReturn(createSuccessVariationResult(returnPeriodList))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) {result =>
          result mustBe Right(returnPeriodList)
        }
      }
    }

    "when the call returns no variable returnPeriods" - {
      "should return a NoVariableReturns errors" in {
        when(mockSdilConnector.returnsVariable(aSubscription.utr)(hc)).thenReturn(createSuccessVariationResult(List()))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoVariableReturns)
        }
      }
    }

    "when the call fails for variable returnPeriods" - {
      "should return a UnexpectedResponseFromSDIL errors" in {
        when(mockSdilConnector.returnsVariable(aSubscription.utr)(hc)).thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

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
          s"when the sdilReturn is $key and the useranswers data is empty" in {
            val returnPeriod = returnPeriodList.head
            val expectedGeneratedUA = emptyUserAnswersForCorrectReturn.copy(
              smallProducerList = if(key == "a nilReturn") {List()} else {smallProducerList},
              data = Json.obj(("correctReturn", Json.toJson(getExpectedUserAnswersCorrectReturnData(key)))),
              correctReturnPeriod = Some(returnPeriod)
            )
            when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc)).thenReturn(createSuccessVariationResult(Some(sdilReturn)))
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, emptyUserAnswersForCorrectReturn, returnPeriod)(hc, ec)

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }

          s"when the sdilReturn is $key and the useranswers data contains some data" in {
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
            when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc)).thenReturn(createSuccessVariationResult(Some(sdilReturn)))
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, initialUserAnswers, returnPeriod)(hc, ec)

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
            override def setForCorrectReturn(correctReturnUserAnswersData: CorrectReturnUserAnswersData,
                                             smallProducers: List[SmallProducer],
                                             returnPeriod: ReturnPeriod)
                                            (implicit writes: Writes[CorrectReturnUserAnswersData]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
          }
          when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriods.head)(hc)).thenReturn(createSuccessVariationResult(Some(emptyReturn)))
          val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, failingUserAnswers, returnPeriods.head)(hc, ec)

          whenReady(res.value) { result =>
            result mustBe Left(FailedToAddDataToUserAnswers)
          }
        }
      }

      "should return a SessionDatabaseInsertError" - {
        "adding data to userAnswers fails" in {
          val returnPeriod = returnPeriodList.head

          when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc)).thenReturn(createSuccessVariationResult(Some(emptyReturn)))
          when(mockSessionService.set(any())).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

          val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, emptyUserAnswersForCorrectReturn, returnPeriods.head)(hc, ec)

          whenReady(res.value) { result =>
            result mustBe Left(SessionDatabaseInsertError)
          }
        }
      }
    }

    "when the sdilReturn does not exist" - {
      "should return a NoSdilReturnForPeriod error" in {
        val returnPeriod = returnPeriodList.head

        when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc)).thenReturn(createSuccessVariationResult(None))

        val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, emptyUserAnswersForCorrectReturn, returnPeriod)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoSdilReturnForPeriod)
        }
      }
    }
  }

  "separateReturnPeriodsByYear" - {
    "when the is no return period" - {
      "return an empty map" in {
        val res = orchestrator.separateReturnPeriodsByYear(List())
        res mustBe Map()
      }
    }

    "when the is one return period in the list" - {
      "return a map with the year and period" in {
        val returnPeriods = List(ReturnPeriod(2023, 0))
        val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
        res mustBe Map(2023 -> returnPeriods)
      }
    }

    "when the is multiple return periods in the list" - {
      "that are all for the year 2022" - {
        "return a map with the year and ordered return periods when ordered" in {
          val returnPeriods = returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriods)
        }

        "return a map with the year and ordered return periods when not ordered" in {
          val returnPeriods = returnPeriodsFor2022.tail ++ List(returnPeriodsFor2022.head)
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriodsFor2022)
        }

        "return a map with the year and repeated return periods removed" in {
          val returnPeriods = returnPeriodsFor2022 ++ returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriodsFor2022)
        }
      }

      "that are all for multiple years" - {
        "return a ordered map with the return periods sorted by years when already ordered" in {
          val returnPeriods = returnPeriodsFor2022 ++ returnPeriodsFor2020
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)
        }

        "return a ordered map with the return periods sorted by years when not already ordered" in {
          val returnPeriods = returnPeriodsFor2020.tail ++ returnPeriodsFor2022.tail ++ List(returnPeriodsFor2022.head, returnPeriodsFor2020.head)
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)
        }

        "return a ordered map with the return periods sorted by years and repeated return periods removed" in {
          val returnPeriods = returnPeriodsFor2020 ++ returnPeriodsFor2022 ++ returnPeriodsFor2022
          val res = orchestrator.separateReturnPeriodsByYear(returnPeriods)
          res mustBe Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)
        }
      }
    }
  }

}
