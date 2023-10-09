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

package controllers.changeActivity

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import controllers.changeActivity.routes._
import generators.ChangeActivityCYAGenerators._
import models.{DataHelper, Litreage, LitresInBands, VariationsSubmission}
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced.Large
import navigation.{FakeNavigatorForChangeActivity, NavigatorForChangeActivity}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyContractPackingPage, HowManyImportsPage, HowManyOperatePackagingSiteOwnBrandsPage, ImportsPage}
import play.api.mvc.Call
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ChangeActivityService
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.ChangeActivityCYAView
import views.summary.changeActivity.ChangeActivitySummary

import java.time.LocalDate
import scala.concurrent.Future

class ChangeActivityCYAControllerSpec extends SpecBase with SummaryListFluency with DataHelper{

  def onwardRoute = Call("GET", "/foo")

  "Check Your Answers Controller" - {

    amountProducedValues.foreach { case (amountProducedKey, amountProducedValue) =>
      thirdPartyPackagingValues.foreach { case (thirdPartyPackagingKey, thirdPartyPackagingValue) =>
        ownBrandsValues.foreach { case (ownBrandsKey, ownBrandsValue) =>
          contractValues.foreach { case (contractKey, contractValue) =>
            importValues.foreach { case (importKey, importValue) =>
              val key = List(amountProducedKey, thirdPartyPackagingKey, ownBrandsKey, contractKey, importKey).filterNot(_.isEmpty).mkString(", ")
              val userAnswers = getUserAnswers(amountProducedValue, thirdPartyPackagingValue, ownBrandsValue, contractValue, importValue)
              s"must return OK and the correct view for a GET for user answers $key" in {

                val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

                running(application) {
                  val request = FakeRequest(GET, ChangeActivityCYAController.onPageLoad.url)

                  val result = route(application, request).value

                  val view = application.injector.instanceOf[ChangeActivityCYAView]

                  status(result) mustEqual OK
                  contentAsString(result) mustEqual view(
                    aSubscription.orgName,
                    ChangeActivitySummary.summaryListsAndHeadings(userAnswers, isCheckAnswers = true),
                    routes.ChangeActivityCYAController.onSubmit
                  )(request, messages(application)).toString
                }
              }
            }
          }
        }
      }
    }

    "must redirect to return sent page on submit" in {
      val userAnswers = emptyUserAnswersForChangeActivity.set(AmountProducedPage, Large).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100L, 100L)).success.value
        .set(ContractPackingPage, true).success.value
        .set(HowManyContractPackingPage, LitresInBands(100 , 100)).success.value
        .set(ImportsPage, true).success.value
        .set(HowManyImportsPage, LitresInBands(100, 100)).success.value
        .copy(packagingSiteList = Map.empty,
              warehouseList = Map.empty)

      val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
      val mockChangeActivityService = mock[ChangeActivityService]

      val retrievedActivityData = testRetrievedActivity()
      val retrievedSubData = testRetrievedSubscription(
        address = testAddress(),
        activity = retrievedActivityData,
        liabilityDate = LocalDate.now(),
        productionSites = List.empty,
        warehouseSites = List.empty,
        contact = testContact(phoneNumber = "testnumber", email = "test@email.test")
      )

      val data: VariationsSubmission = testConvert(testRegistrationVariationData(
        original = retrievedSubData,
        updatedBusinessAddress = testAddress(),
        producer = testProducer(isProducer = false),
        updatedContactDetails = testContactDetails(),
        packageOwn = Some(true),
        packageOwnVol= Some(Litreage(100, 100)),
        copackForOthers = true,
        copackForOthersVol = Some(Litreage(100, 100)),
        imports = true,
        importsVol = Some(Litreage(100, 100)),
      ))


      when(mockChangeActivityService.submitVariation(retrievedSubData, userAnswers)) thenReturn Future.successful(Some(OK))
      when(mockConnector.submitVariation(data, "testref")(hc)).thenReturn(Future.successful(Some(200)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ChangeActivityService].toInstance(mockChangeActivityService),
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockConnector)
        )
        .build()

      running(application) {
        val request =
        FakeRequest(POST, ChangeActivityCYAController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-variations-frontend/change-activity/variation-done"
      }
    }
    testInvalidJourneyType(ChangeActivity, ChangeActivityCYAController.onPageLoad.url, false)
    testNoUserAnswersError(ChangeActivityCYAController.onPageLoad.url, false)
  }
}
