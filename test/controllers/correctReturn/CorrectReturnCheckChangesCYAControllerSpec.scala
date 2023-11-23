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

package controllers.correctReturn

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import controllers._
import models.correctReturn.{AddASmallProducer, ChangedPage, RepaymentMethod}
import models.{LitresInBands, SmallProducer}
import navigation.{FakeNavigatorForCorrectReturn, NavigatorForCorrectReturn}
import orchestrators.CorrectReturnOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.correctReturn._
import play.api.inject
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReturnService
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import scala.concurrent.Future

class CorrectReturnCheckChangesCYAControllerSpec extends SpecBase with SummaryListFluency {

  val mockReturnService: ReturnService = mock[ReturnService]
  val mockCorrectReturnOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  def onwardRoute: Call = Call("GET", "/foo")

  "Check Changes Controller" - {

    "must return OK and the correct view for a GET" in {
      val litres = LitresInBands(2000, 4000)
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
          smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, litres).success.value
        .set(ExemptionsForSmallProducersPage, true).success.value
        .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, litres).success.value
        .set(BroughtIntoUkFromSmallProducersPage, true).success.value
        .set(HowManyBroughtIntoUkFromSmallProducersPage, litres).success.value
        .set(ClaimCreditsForExportsPage, true).success.value
        .set(HowManyClaimCreditsForExportsPage, litres).success.value
        .set(ClaimCreditsForLostDamagedPage, true).success.value
        .set(HowManyCreditsForLostDamagedPage, litres).success.value
        .set(CorrectionReasonPage, "foo").success.value
        .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

      val changedPages = List(
        ChangedPage(OperatePackagingSiteOwnBrandsPage, answerChanged = true),
        ChangedPage(HowManyOperatePackagingSiteOwnBrandsPage, answerChanged = true),
        ChangedPage(PackagedAsContractPackerPage, answerChanged = true),
        ChangedPage(HowManyPackagedAsContractPackerPage, answerChanged = true),
        ChangedPage(BroughtIntoUKPage, answerChanged = true),
        ChangedPage(HowManyBroughtIntoUKPage, answerChanged = true),
        ChangedPage(BroughtIntoUkFromSmallProducersPage, answerChanged = true),
        ChangedPage(HowManyBroughtIntoUkFromSmallProducersPage, answerChanged = true),
        ChangedPage(ClaimCreditsForExportsPage, answerChanged = true),
        ChangedPage(HowManyClaimCreditsForExportsPage, answerChanged = true),
        ChangedPage(ClaimCreditsForLostDamagedPage, answerChanged = true),
        ChangedPage(HowManyCreditsForLostDamagedPage, answerChanged = true),
        ChangedPage(ExemptionsForSmallProducersPage, answerChanged = true))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[ReturnService].toInstance(mockReturnService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
        when (mockReturnService.getBalanceBroughtForward(any())(any(),any())) thenReturn Future.successful(-502.75)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorrectReturnCheckChangesCYAView]
        val orgName = " Super Lemonade Plc"
        val section = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(userAnswers, aSubscription, changedPages, amounts)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgName, section,
          controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onSubmit)(request, messages(application)).toString
      }
    }

    "must submit successfully " in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            inject.bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute))
          )
          .build()
      running(application) {
        //when (mockCorrectReturnOrchestrator.submitVariation(userAnswersForCorrectReturnWithEmptySdilReturn, aSubscription)(any(),any())) thenReturn Future.successful(Right())
        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad.url
      }
    }

    "return to select change if user answers fails" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute))
          )
          .build()
      running(application) {
       // when (mockCorrectReturnOrchestrator.submitVariation(userAnswersForCorrectReturnWithEmptySdilReturn, aSubscription)(any(),any())) thenReturn Future.successful(Right())
        val request = FakeRequest(POST, controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url).withFormUrlEncodedBody()
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SelectChangeController.onPageLoad.url
      }
    }
  }
}
