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
import errors.{UnexpectedResponseFromSDIL, _}
import models.backend.{RetrievedActivity, RetrievedSubscription, UkAddress}
import models.correctReturn.RepaymentMethod.BankAccount
import models.correctReturn.{AddASmallProducer, CorrectReturnUserAnswersData, RepaymentMethod}
import models.requests.{CorrectReturnDataRequest, DataRequest}
import models.submission.ReturnVariationData
import models.{Contact, LitresInBands, ReturnPeriod, SdilReturn, SelectChange, SmallProducer, UserAnswers, VariationsSubmissionDataHelper}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn._
import play.api.inject.bind
import play.api.libs.json.{JsString, Json, Writes}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import service.VariationResult
import services.{ReturnService, SessionService}
import utilities.GenericLogger

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future
import scala.util.{Failure, Try}

class CorrectReturnOrchestratorSpec extends SpecBase with MockitoSugar with VariationsSubmissionDataHelper {

  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService: SessionService = mock[SessionService]
  val mockOrchastrator:CorrectReturnOrchestrator = new CorrectReturnOrchestrator(mockSdilConnector, mockSessionService)

  val emptyReturn: SdilReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0), submittedOn =
    Some(submittedDateTime.toInstant(ZoneOffset.UTC)))
  val populatedReturn: SdilReturn = SdilReturn((100, 200), (200, 100),
    smallProducerList, (300, 400), (400, 300), (50, 60), (60, 50),
    submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))

  val sdilReturnsExamples: Map[String, SdilReturn] = Map("a nilReturn" -> emptyReturn, "not a nilReturn" -> populatedReturn)

  def getExpectedUserAnswersCorrectReturnData(key: String): CorrectReturnUserAnswersData = if (key == "a nilReturn") {
    expectedCorrectReturnDataForNilReturn
  } else {
    expectedCorrectReturnDataForPopulatedReturn
  }

  val orchestrator = new CorrectReturnOrchestrator(mockSdilConnector, mockSessionService)

  "getReturnPeriods" - {
    "when the call returns variable returnPeriods" - {
      "should return the variable returns" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc)).thenReturn(createSuccessVariationResult(returnPeriodList))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) {result =>
          result mustBe Right(returnPeriodList)
        }
      }
    }

    "when the call returns no variable returnPeriods" - {
      "should return a NoVariableReturns errors" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc)).thenReturn(createSuccessVariationResult(List()))

        val res = orchestrator.getReturnPeriods(aSubscription)(hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoVariableReturns)
        }
      }
    }

    "when the call fails for variable returnPeriods" - {
      "should return a UnexpectedResponseFromSDIL errors" in {
        when(mockSdilConnector.getVariableReturnsFromCache(aSubscription.utr)(hc)).thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

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
            val uAsSDILReturn = if(key == "a nilReturn") {emptyReturn} else populatedReturn
            val expectedGeneratedUA = emptyUserAnswersForCorrectReturn.copy(
              smallProducerList = if(key == "a nilReturn") {List()} else {smallProducerList},
              data = Json.obj("originalSDILReturn" -> Json.toJson(uAsSDILReturn),
                ("correctReturn", Json.toJson(getExpectedUserAnswersCorrectReturnData(key)))),
              correctReturnPeriod = Some(returnPeriod)
            )
            when(mockSdilConnector.getReturn(aSubscription.utr, returnPeriod)(hc)).thenReturn(createSuccessVariationResult(Some(sdilReturn)))
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.setupUserAnswersForCorrectReturn(aSubscription, emptyUserAnswersForCorrectReturn, returnPeriod)(hc, ec)

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }

          s"when the sdilReturn is $key and the user answers data contains some data" in {
            val returnPeriod = returnPeriodList.head
            val uaInitialData = Json.obj(("testing", JsString("I am still here")))
            val initialUserAnswers = emptyUserAnswersForCorrectReturn.copy(data = uaInitialData)
            val uAsSDILReturn = if (key == "a nilReturn") {emptyReturn} else populatedReturn
            val expectedGeneratedUA = emptyUserAnswersForCorrectReturn.copy(
              smallProducerList = if (key == "a nilReturn") {
                List()
              } else {
                smallProducerList
              },
              data = uaInitialData ++
                Json.obj("originalSDILReturn" -> Json.toJson(uAsSDILReturn),
                  ("correctReturn", Json.toJson(getExpectedUserAnswersCorrectReturnData(key)))),
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
            override def setOriginalSDILReturn(originalSDILReturn: SdilReturn)
                                              (implicit writes: Writes[SdilReturn]):
              Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
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




  "submitVariation" - {
    "Create returnVariationData structure and submit unsuccessfully using the connector" in {
      val currentReturnPeriod = ReturnPeriod(2023, 1)
      val litres = LitresInBands(2000, 4000)
      val userAnswers: UserAnswers = completedUserAnswersForCorrectReturnNewPackerOrImporter
        .copy(
          data = Json.obj(
            fields =
              "originalSDILReturn" -> Json.toJson(populatedReturn),
            "correctReturn" ->
              Json.obj("operatePackagingSiteOwnBrands" -> false, "packagedAsContractPacker" -> false,
                "exemptionsForSmallProducers" -> false, "broughtIntoUK" -> false, "broughtIntoUkFromSmallProducers" -> false,
                "claimCreditsForExports" -> false, "claimCreditsForLostDamaged" -> false
              )
          ),
          packagingSiteList = Map.empty, warehouseList = Map.empty,
          correctReturnPeriod = Some(currentReturnPeriod),
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
        .set(RepaymentMethodPage, BankAccount).success.value
        .set(CorrectionReasonPage, "N/A").success.value


      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      running(application) {
        val constructReturnVariationData = orchestrator.constructReturnVariationData(userAnswers,aSubscription)
        val constructActivityVariation = orchestrator.constructActivityVariation(userAnswers,aSubscription)
        val returnPeriod = userAnswers.correctReturnPeriod.get
        val revisedReturn = userAnswers.getCorrectReturnData.get
        val returnVariationData = ReturnVariationData(
          original = userAnswers.getCorrectReturnOriginalSDILReturnData.get,
          revised = SdilReturn(
            ownBrand = revisedReturn.howManyOperatePackagingSiteOwnBrands.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            packLarge = revisedReturn.howManyPackagedAsContractPacker.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            packSmall = userAnswers.smallProducerList,
            importLarge = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            importSmall = revisedReturn.howManyBroughtIntoUkFromSmallProducers.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            export = revisedReturn.howManyClaimCreditsForExports.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            wastage = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            submittedOn = None
          ),
          period = returnPeriod,
          orgName = aSubscription.orgName,
          address = aSubscription.address,
          reason = userAnswers.get(CorrectionReasonPage).get,
          repaymentMethod = Some(userAnswers.get(RepaymentMethodPage).toString),
        )


        when( mockSdilConnector.submitReturnsVariation(aSubscription.sdilRef, constructReturnVariationData.get)(hc))
          .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

        val returnVariationResult:Option[VariationResult[Unit]] = orchestrator.submitReturnVariation(userAnswers, aSubscription)(hc, ec)

        whenReady(returnVariationResult.get.value){result =>
          result mustEqual Left(UnexpectedResponseFromSDIL)
        }

        when(mockSdilConnector.submitVariation(constructActivityVariation, aSubscription.sdilRef)(hc))
          .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

        val submitActivityVariation: VariationResult[Unit] = orchestrator.submitActivityVariation(userAnswers, aSubscription)(hc, ec)

        whenReady(submitActivityVariation.value){ result =>
          result mustEqual Left(UnexpectedResponseFromSDIL)
        }
        }
      }
    }

    "Create returnVariationData structure and submit successfully using the connector" in {
      val currentReturnPeriod = ReturnPeriod(2023, 1)
      val litres = LitresInBands(2000, 4000)
      val userAnswers: UserAnswers = completedUserAnswersForCorrectReturnNewPackerOrImporter
        .copy(
          data = Json.obj(
            fields =
              "originalSDILReturn" -> Json.toJson(populatedReturn),
            "correctReturn" ->
              Json.obj("operatePackagingSiteOwnBrands" -> false, "packagedAsContractPacker" -> false,
                "exemptionsForSmallProducers" -> false, "broughtIntoUK" -> false, "broughtIntoUkFromSmallProducers" -> false,
                "claimCreditsForExports" -> false, "claimCreditsForLostDamaged" -> false
              )
          ),
          packagingSiteList = Map.empty, warehouseList = Map.empty,
          correctReturnPeriod = Some(currentReturnPeriod),
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
        .set(RepaymentMethodPage, BankAccount).success.value
        .set(CorrectionReasonPage, "N/A").success.value


      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      running(application) {
        val constructReturnVariationData = orchestrator.constructReturnVariationData(userAnswers,aSubscription)
        val constructActivityVariation = orchestrator.constructActivityVariation(userAnswers,aSubscription)
        val returnPeriod = userAnswers.correctReturnPeriod.get
        val revisedReturn = userAnswers.getCorrectReturnData.get
        val returnVariationData = ReturnVariationData(
          original = userAnswers.getCorrectReturnOriginalSDILReturnData.get,
          revised = SdilReturn(
            ownBrand = revisedReturn.howManyOperatePackagingSiteOwnBrands.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            packLarge = revisedReturn.howManyPackagedAsContractPacker.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            packSmall = userAnswers.smallProducerList,
            importLarge = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            importSmall = revisedReturn.howManyBroughtIntoUkFromSmallProducers.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            export = revisedReturn.howManyClaimCreditsForExports.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            wastage = revisedReturn.howManyCreditsForLostDamaged.map(litres => (litres.lowBand, litres.highBand)).getOrElse(0, 0),
            submittedOn = None
          ),
          period = returnPeriod,
          orgName = aSubscription.orgName,
          address = aSubscription.address,
          reason = userAnswers.get(CorrectionReasonPage).get,
          repaymentMethod = Some(userAnswers.get(RepaymentMethodPage).toString),
        )


        when( mockSdilConnector.submitReturnsVariation(aSubscription.sdilRef, constructReturnVariationData.get)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val returnVariationResult:Option[VariationResult[Unit]] = orchestrator.submitReturnVariation(userAnswers, aSubscription)(hc, ec)

        whenReady(returnVariationResult.get.value){result =>
          result mustEqual Right((): Unit)
        }

        when(mockSdilConnector.submitVariation(constructActivityVariation, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

        val submitActivityVariation: VariationResult[Unit] = orchestrator.submitActivityVariation(userAnswers, aSubscription)(hc, ec)

          whenReady(submitActivityVariation.value){ result =>
            result mustEqual Right((): Unit)
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
