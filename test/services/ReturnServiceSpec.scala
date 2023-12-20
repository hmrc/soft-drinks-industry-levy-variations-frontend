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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.backend.CentralAssessment
import models.correctReturn.{CorrectReturnUserAnswersData, RepaymentMethod, ReturnsVariation}
import models.submission.{Litreage, ReturnVariationData}
import models.{LitresInBands, SdilReturn}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.{BalanceRepaymentRequired, CorrectionReasonPage, RepaymentMethodPage}
import play.api.libs.json.Json

import java.time.LocalDate

class ReturnServiceSpec extends SpecBase with MockitoSugar {

  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val returnService = new ReturnService(mockSdilConnector)(mockAppConfig)

  "getBalanceBroughtForward" - {

    "when balance all is enabled" - {
      "should get the balance history, extract the total and return it" in {
        val fli = CentralAssessment(LocalDate.now(), 200)
        when(mockAppConfig.balanceAllEnabled).thenReturn(true)
        when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(createSuccessVariationResult(List(fli)))

        val res = returnService.getBalanceBroughtForward("sdilRef")

        whenReady(res.value) { result =>
          result mustBe Right(200)
        }
      }

      "and balance history is empty should return 0" in {
        when(mockAppConfig.balanceAllEnabled).thenReturn(true)
        when(mockSdilConnector.balanceHistory(any(), any())(any())).thenReturn(createSuccessVariationResult(List.empty))

        val res = returnService.getBalanceBroughtForward("sdilRef")

        whenReady(res.value) { result =>
          result mustBe Right(0)
        }
      }
    }

    "when balanceAll is disabled" - {
      "should return the current balance" in {
        when(mockAppConfig.balanceAllEnabled).thenReturn(false)
        when(mockSdilConnector.balance(any(), any())(any())).thenReturn(createSuccessVariationResult(200))

        val res = returnService.getBalanceBroughtForward("sdilRef")

        whenReady(res.value) { result =>
          result mustBe Right(200)
        }
      }
    }
  }

  "submitSdilReturnsVary" - {
    val returnPeriod = returnPeriodsFor2022.head
    val originalReturn = emptySdilReturn
    val revisedReturn = emptySdilReturn.copy(ownBrand = Litreage(200, 200))
    "should submit the sdilReturnsVary and return unit" - {
      "when the userAnswers contains correction reason and payment method and balance repayment required" in {
        val userAnswers = emptyUserAnswersForCorrectReturn
          .set(BalanceRepaymentRequired, true).success.value
          .set(CorrectionReasonPage, "testing").success.value
          .set(RepaymentMethodPage, RepaymentMethod.BankAccount).success.value
        val expectedReturnVariation = ReturnVariationData(
          originalReturn, revisedReturn, returnPeriod, aSubscription.orgName,
          aSubscription.address, "testing", Some(RepaymentMethod.BankAccount.toString)
        )
        when(mockSdilConnector.submitSdilReturnsVary(aSubscription.sdilRef, expectedReturnVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitSdilReturnsVary(aSubscription, userAnswers, originalReturn, returnPeriod, revisedReturn)

        whenReady(res.value) {result =>
          result mustBe Right((): Unit)
        }
      }

      "when the userAnswers contains correction reason and balance repayment not required" in {
        val userAnswers = emptyUserAnswersForCorrectReturn
          .set(BalanceRepaymentRequired, false).success.value
          .set(CorrectionReasonPage, "testing").success.value
        val expectedReturnVariation = ReturnVariationData(
          originalReturn, revisedReturn, returnPeriod, aSubscription.orgName,
          aSubscription.address, "testing", None
        )
        when(mockSdilConnector.submitSdilReturnsVary(aSubscription.sdilRef, expectedReturnVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitSdilReturnsVary(aSubscription, userAnswers, originalReturn, returnPeriod, revisedReturn)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }

      "when the userAnswers does not contain correction reason and payment method" in {
        val userAnswers = emptyUserAnswersForCorrectReturn
        val expectedReturnVariation = ReturnVariationData(
          originalReturn, revisedReturn, returnPeriod, aSubscription.orgName,
          aSubscription.address, "", None
        )
        when(mockSdilConnector.submitSdilReturnsVary(aSubscription.sdilRef, expectedReturnVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitSdilReturnsVary(aSubscription, userAnswers, originalReturn, returnPeriod, revisedReturn)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }
    }
  }

  "submitReturnVariation" - {
    "should submit the submitReturnVariation and return unit" - {
      "when the userAnswers is a new importer and new packer with warehouses" in {
        val correctReturnData = CorrectReturnUserAnswersData(
          true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100)),
          false, true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100)),
          true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100))
        )

        val userAnswers = emptyUserAnswersForCorrectReturn
          .copy(packagingSiteList = packingSiteMap,
            warehouseList = twoWarehouses,
            data = Json.obj(("correctReturn", Json.toJson(correctReturnData)))
          )

        val sdilReturn = SdilReturn.generateFromUserAnswers(userAnswers, None)

        val expectedReturnsVariation = ReturnsVariation(
          aSubscription.orgName, aSubscription.address,
          (true, Litreage(800, 800)),
          (true, Litreage(400, 400)),
          twoWarehouses.values.toList,
          packingSiteMap.values.toList,
          aSubscription.contact.phoneNumber,
          aSubscription.contact.email,
          504.00
        )
        when(mockAppConfig.lowerBandCostPerLitre).thenReturn(0.18)
        when(mockAppConfig.higherBandCostPerLitre).thenReturn(0.24)

        when(mockSdilConnector.submitReturnVariation(aSubscription.sdilRef, expectedReturnsVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitReturnVariation(aSubscription, sdilReturn, userAnswers, correctReturnData)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }

      "when the userAnswers is a new importer and not a new packer with warehouses" in {
        val correctReturnData = CorrectReturnUserAnswersData(
          true, Some(LitresInBands(100, 100)), false, None,
          false, true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100)),
          true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100))
        )

        val userAnswers = emptyUserAnswersForCorrectReturn
          .copy(
            warehouseList = twoWarehouses,
            data = Json.obj(("correctReturn", Json.toJson(correctReturnData)))
          )

        val sdilReturn = SdilReturn.generateFromUserAnswers(userAnswers, None)

        val expectedReturnsVariation = ReturnsVariation(
          aSubscription.orgName, aSubscription.address,
          (true, Litreage(800, 800)),
          (false, Litreage(0, 0)),
          twoWarehouses.values.toList,
          List.empty,
          aSubscription.contact.phoneNumber,
          aSubscription.contact.email,
          336.00
        )
        when(mockAppConfig.lowerBandCostPerLitre).thenReturn(0.18)
        when(mockAppConfig.higherBandCostPerLitre).thenReturn(0.24)

        when(mockSdilConnector.submitReturnVariation(aSubscription.sdilRef, expectedReturnsVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitReturnVariation(aSubscription, sdilReturn, userAnswers, correctReturnData)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }

      "when the userAnswers is a new packer with no warehouses" in {
        val correctReturnData = CorrectReturnUserAnswersData(
          true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100)),
          false, false, None, false, None,
          true, Some(LitresInBands(100, 100)), true, Some(LitresInBands(100, 100))
        )

        val userAnswers = emptyUserAnswersForCorrectReturn
          .copy(packagingSiteList = packingSiteMap,
            data = Json.obj(("correctReturn", Json.toJson(correctReturnData)))
          )

        val sdilReturn = SdilReturn.generateFromUserAnswers(userAnswers, None)

        val expectedReturnsVariation = ReturnsVariation(
          aSubscription.orgName, aSubscription.address,
          (false, Litreage(0, 0)),
          (true, Litreage(400, 400)),
          List.empty,
          packingSiteMap.values.toList,
          aSubscription.contact.phoneNumber,
          aSubscription.contact.email,
          336.00
        )
        when(mockAppConfig.lowerBandCostPerLitre).thenReturn(0.18)
        when(mockAppConfig.higherBandCostPerLitre).thenReturn(0.24)

        when(mockSdilConnector.submitReturnVariation(aSubscription.sdilRef, expectedReturnsVariation)(hc))
          .thenReturn(createSuccessVariationResult((): Unit))

        val res = returnService.submitReturnVariation(aSubscription, sdilReturn, userAnswers, correctReturnData)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }
    }
  }
}
