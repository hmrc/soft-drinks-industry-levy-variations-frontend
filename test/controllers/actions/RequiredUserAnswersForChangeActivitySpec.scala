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

package controllers.actions

import base.SpecBase
import models.backend.Site
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, None, Small}
import models.requests.{DataRequest, RequiredDataRequest}
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import pages.{QuestionPage, RequiredPage}
import pages.changeActivity._
import play.api.libs.json.Reads
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.{contentAsString, redirectLocation}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import scala.concurrent.Future

class RequiredUserAnswersForChangeActivitySpec extends SpecBase with DefaultAwaitTimeout {

  val requiredUserAnswers: RequiredUserAnswersForChangeActivity = application.injector.instanceOf[RequiredUserAnswersForChangeActivity]
  val exampleSuccessActionResult: String = "woohoo"
  val exampleSuccessAction: Future[Result] = Future.successful(Ok(exampleSuccessActionResult))

  def dataRequest(userAnswers: UserAnswers): DataRequest[AnyContentAsEmpty.type] = RequiredDataRequest(FakeRequest(), "", aSubscription, userAnswers)
  "requireData" - {
    "should return result passed in when not a page matched in function" in {
      contentAsString(requiredUserAnswers.requireData(AmountProducedPage)(exampleSuccessAction)
      (dataRequest(emptyUserAnswersForChangeActivity))) mustBe exampleSuccessActionResult
    }

    s"should return result passed in when page is $ChangeActivityCYAPage" - {
      s"when AmountProduced is $Large and all answers are answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }
    }

    s"should return result passed in when page is $ChangeActivityCYAPage" - {
      s"when AmountProduced is $Large and contractPacking and OperatePackagingSites are both false and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when $AmountProducedPage is $Small, $ThirdPartyPackagersPage is true, contract packing is false and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when $AmountProducedPage is $None and all answers are answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when $AmountProducedPage is $None, contract packing is false and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when all $LitresInBands are not required" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, false).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is true, $ContractPackingPage is false, $OperatePackagingSiteOwnBrandsPage " +
        s"is false, $ImportsPage is false" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, false).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is true, $OperatePackagingSiteOwnBrandsPage is true, $ContractPackingPage " +
        s"is false, $ImportsPage is false" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, false).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is false, $OperatePackagingSiteOwnBrandsPage is true, " +
        s"$ContractPackingPage is true, $ImportsPage is false" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, false).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, false).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, false).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is false, $OperatePackagingSiteOwnBrandsPage is false, " +
        s"$ContractPackingPage is true, $ImportsPage is false" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, false).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, false).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, false).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is false, $OperatePackagingSiteOwnBrandsPage is true, " +
        s"$ContractPackingPage is false, $ImportsPage is true" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, false).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when AmountProduced is $Small, $ThirdPartyPackagersPage is false, $OperatePackagingSiteOwnBrandsPage is false, " +
        s"$ContractPackingPage is false, $ImportsPage is true" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, false).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)
        (dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }
    }

    s"should redirect to the appropriate missing page when missing answers for $ChangeActivityCYAPage" - {

      s"when AmountProduced is $Large and contractPacking is false, OperatePackagingSites is true and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $Large and contractPacking is true, OperatePackagingSites is false and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $Small, $OperatePackagingSiteOwnBrandsPage is true, $ContractPackingPage is true, and " +
        s"pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $None and contractPacking is true and pack at business address is not answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $Large and contractPacking is false, OperatePackagingSites is true and pack at business address is answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $Large and contractPacking is true, OperatePackagingSites is false and pack at business address is answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $Small, $OperatePackagingSiteOwnBrandsPage is true, $ContractPackingPage is true, and " +
        s"pack at business address is answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
      }

      s"when AmountProduced is $None and contractPacking is true and pack at business address is answered" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
      }

      List[QuestionPage[_]](
        AmountProducedPage,
        ContractPackingPage,
        HowManyContractPackingPage,
        OperatePackagingSiteOwnBrandsPage,
        HowManyOperatePackagingSiteOwnBrandsPage,
        ImportsPage,
        HowManyImportsPage,
        PackagingSiteDetailsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers set to true, the user is taken to that page that is required for " +
          s"${AmountProduced.Large}" in {
          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(ContractPackingPage, true).success.value
              .set(ThirdPartyPackagersPage, true).success.value
              .set(OperatePackagingSiteOwnBrandsPage, true).success.value
              .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
              .set(ImportsPage, true).success.value
              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
              .set(PackAtBusinessAddressPage, true).success.value
              .set(PackagingSiteDetailsPage, false).success.value
              .set(SecondaryWarehouseDetailsPage, true).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }

      List[QuestionPage[_]](
        AmountProducedPage,
        OperatePackagingSiteOwnBrandsPage,
        ImportsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers set to false, the user is taken to that page that is required " +
        s"for ${AmountProduced.Large}" in {

          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, false).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(ImportsPage, false).success.value
              .set(PackAtBusinessAddressPage, false).success.value
              .set(PackagingSiteDetailsPage, false).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }

      List[QuestionPage[_]](
        AmountProducedPage,
        ContractPackingPage,
        HowManyContractPackingPage,
        ThirdPartyPackagersPage,
        OperatePackagingSiteOwnBrandsPage,
        HowManyOperatePackagingSiteOwnBrandsPage,
        ImportsPage,
        HowManyImportsPage,
        PackagingSiteDetailsPage,
        SecondaryWarehouseDetailsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required " +
        s"for ${AmountProduced.Small}" in {
          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value
              .set(ContractPackingPage, true).success.value
              .set(ThirdPartyPackagersPage, true).success.value
              .set(OperatePackagingSiteOwnBrandsPage, true).success.value
              .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
              .set(ImportsPage, true).success.value
              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
              .set(PackAtBusinessAddressPage, true).success.value
              .set(PackagingSiteDetailsPage, false).success.value
              .set(SecondaryWarehouseDetailsPage, true).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }

      List[QuestionPage[_]](
        AmountProducedPage,
        ContractPackingPage,
        ThirdPartyPackagersPage,
        OperatePackagingSiteOwnBrandsPage,
        ImportsPage,
        HowManyImportsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required " +
        s"for ${AmountProduced.Small}" in {
          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, false).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
              .set(ImportsPage, true).success.value
              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
              .set(PackAtBusinessAddressPage, false).success.value
              .set(PackagingSiteDetailsPage, false).success.value
              .set(SecondaryWarehouseDetailsPage, false).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }
      List[QuestionPage[_]](
        AmountProducedPage,
        ContractPackingPage,
        HowManyContractPackingPage,
        ImportsPage,
        HowManyImportsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required " +
        s"for ${AmountProduced.None}" in {
          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.None).success.value
              .set(ContractPackingPage, true).success.value
              .set(ThirdPartyPackagersPage, true).success.value
              .set(OperatePackagingSiteOwnBrandsPage, true).success.value
              .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
              .set(ImportsPage, true).success.value
              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
              .set(PackAtBusinessAddressPage, true).success.value
              .set(PackagingSiteDetailsPage, false).success.value
              .set(SecondaryWarehouseDetailsPage, true).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }
      List[QuestionPage[_]](
        AmountProducedPage,
        ContractPackingPage,
        ImportsPage,
        HowManyImportsPage
      ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required " +
        s"for ${AmountProduced.None}" in {
          val userAnswers = {
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.None).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, false).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
              .set(ImportsPage, true).success.value
              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
              .set(PackAtBusinessAddressPage, false).success.value
              .set(PackagingSiteDetailsPage, false).success.value
              .set(SecondaryWarehouseDetailsPage, false).success.value
          }.remove(eachPage).success.value
          val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
          res.get mustBe eachPage.url(CheckMode)
        }
      }
    }
  }

  "returnMissingAnswers" - {
    List(true, false).foreach(packagingSitesEmpty => {
      val packagingSiteList: Map[String, Site] = if (packagingSitesEmpty) Map.empty else packingSiteMap

      s"should return all missing answers when user answers is empty and packaging site list is ${if (packagingSitesEmpty) "" else "not "}empty" in {
        val userAnswers = emptyUserAnswersForChangeActivity.copy(packagingSiteList = packagingSiteList)
        implicit val dataRequest: DataRequest[AnyContentAsEmpty.type] = RequiredDataRequest(FakeRequest(), "", aSubscription, userAnswers)
        val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ChangeActivityCYAPage.previousPagesRequired)
        res mustBe List(AmountProducedPage, ContractPackingPage, ImportsPage)
      }

      s"should return correct missing answer list when producer is $Large, contractPacking is false, OperatePackagingSites " +
        s"is true and pack at business address is not answered and packaging site list is ${if (packagingSitesEmpty) "" else "not "}empty" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packagingSiteList)
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)

        val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ChangeActivityCYAPage.previousPagesRequired)
        val requiredPages = if (packagingSitesEmpty) {
          List(PackAtBusinessAddressPage, PackagingSiteDetailsPage)
        } else {
          List(PackagingSiteDetailsPage)
        }
        res mustBe requiredPages
      }

      s"should return correct missing answer list when producer is $Large, $OperatePackagingSiteOwnBrandsPage " +
        s"is true and PackAtBusinessAddress is not answered and packaging site list is ${if (packagingSitesEmpty) "" else "not "}empty" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packagingSiteList)
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)

        val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ChangeActivityCYAPage.previousPagesRequired)
        val requiredPages = if (packagingSitesEmpty) {
          List(PackAtBusinessAddressPage, PackagingSiteDetailsPage)
        } else {
          List(PackagingSiteDetailsPage)
        }
        res mustBe requiredPages
      }

      val requiredPagesSmallorNonProducer = if (packagingSitesEmpty) {
        List(PackAtBusinessAddressPage, PackagingSiteDetailsPage)
      } else {
        List(PackagingSiteDetailsPage)
      }

      s"should return correct missing answer list when producer is $Small, $ContractPackingPage is true, $OperatePackagingSiteOwnBrandsPage " +
        s"is true, and pack at business address is not answered and packaging site list ${if (packagingSitesEmpty) "" else "not "}empty" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packagingSiteList)
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)

        val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ChangeActivityCYAPage.previousPagesRequired)
        res mustBe requiredPagesSmallorNonProducer
      }

      s"should return correct missing answer list when producer is $None, $ContractPackingPage is true," +
        s"and pack at business address is not answered and packaging site list ${if (packagingSitesEmpty) "" else "not "}empty" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packagingSiteList)
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)

        val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ChangeActivityCYAPage.previousPagesRequired)
        res mustBe requiredPagesSmallorNonProducer
      }
    })
  }
//
//  "checkYourAnswersRequiredData" - {
//    "should redirect to action when all answers answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1,1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//          .set(PackagingSiteDetailsPage, true).success.value
//          .set(SecondaryWarehouseDetailsPage, false).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      contentAsString(requiredUserAnswers.checkYourAnswersRequiredData(exampleSuccessAction)) mustBe exampleSuccessActionResult
//    }
//  }

  s"ThirdPartyPackagerRequiredData" - {
    s"should return empty missing answer list when producer is $Small" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Small).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ThirdPartyPackagersPage.previousPagesRequired)

      res mustBe List.empty
    }
  }

  s"OperatePackagingSiteRequiredData" - {
    s"should redirect to the appropriate missing page when missing answers required for $OperatePackagingSiteOwnBrandsPage" - {
      s"when AmountProduced is $Small and $ThirdPartyPackagersPage has not been completed, on page load of $OperatePackagingSiteOwnBrandsPage should" +
        s" redirect to $ThirdPartyPackagersPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(OperatePackagingSiteOwnBrandsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
      }
    }

    s"should return correct missing answer list when producer is $Small" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Small).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, OperatePackagingSiteOwnBrandsPage.previousPagesRequired)

      res mustBe List(ThirdPartyPackagersPage)
    }

    s"should return empty missing answer list when producer is $Large" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Large).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, OperatePackagingSiteOwnBrandsPage.previousPagesRequired)

      res mustBe List.empty
    }
  }

  "ContractPackingPageRequiredData" - {
    s"should redirect to the appropriate missing page when missing answers required for $ContractPackingPage" - {
      s"when AmountProduced is $Small and $ThirdPartyPackagersPage has not been completed, on page load of $ContractPackingPage should" +
        s" redirect to $ThirdPartyPackagersPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ContractPackingPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
      }
    }

    s"should return correct missing answer list when producer is $Small and no other answers have been set" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Small).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ContractPackingPage.previousPagesRequired)

      res mustBe List(ThirdPartyPackagersPage, OperatePackagingSiteOwnBrandsPage)
    }

    s"should return correct missing answer list when producer is $Large" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Large).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ContractPackingPage.previousPagesRequired)

      res mustBe List(OperatePackagingSiteOwnBrandsPage)
    }

    s"should return empty missing answer list when producer is $None" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, None).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ContractPackingPage.previousPagesRequired)

      res mustBe List.empty
    }
  }

  s"ImportsPageRequiredData" - {
    s"should redirect to the appropriate missing page when missing answers required for $ImportsPage" - {
      s"when AmountProduced is $Small and all other pages have not been completed, on page load of $ImportsPage should" +
        s" redirect to $ThirdPartyPackagersPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Small and $ThirdPartyPackagersPage has been completed, on page load of $ImportsPage should" +
        s" redirect to $OperatePackagingSiteOwnBrandsPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Small and $ThirdPartyPackagersPage and $OperatePackagingSiteOwnBrandsPage have been completed, " +
        s"on page load of $ImportsPage should redirect to $ContractPackingPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ContractPackingController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Small and $ContractPackingPage has been completed, on page load of $ImportsPage should" +
        s" redirect to $ThirdPartyPackagersPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ContractPackingPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Small and $ThirdPartyPackagersPage and $ContractPackingPage have been completed, " +
        s"on page load of $ImportsPage should redirect to $OperatePackagingSiteOwnBrandsPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(ContractPackingPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Large and no other pages have been completed, on page load of $ImportsPage should" +
        s" redirect to $OperatePackagingSiteOwnBrandsPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Large and $OperatePackagingSiteOwnBrandsPage has been completed, on page load of $ImportsPage should" +
        s" redirect to $ContractPackingPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ContractPackingController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $Large and $ContractPackingPage has been completed, on page load of $ImportsPage should" +
        s" redirect to $OperatePackagingSiteOwnBrandsPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Large).success.value
            .set(ContractPackingPage, false).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
      }

      s"when AmountProduced is $None and all other pages have not been completed, on page load of $ImportsPage should" +
        s" redirect to $ContractPackingPage" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, None).success.value
        }
        val res = redirectLocation(requiredUserAnswers.requireData(ImportsPage)(exampleSuccessAction)(dataRequest(userAnswers)))
        res.get mustBe controllers.changeActivity.routes.ContractPackingController.onPageLoad(NormalMode).url
      }
    }

    s"should return correct missing answer list when producer is $Small and no other answers have been set" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Small).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ImportsPage.previousPagesRequired)

      res mustBe List(ThirdPartyPackagersPage, OperatePackagingSiteOwnBrandsPage, ContractPackingPage)
    }

    s"should return correct missing answer list when producer is $Large" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, Large).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ImportsPage.previousPagesRequired)

      res mustBe List(OperatePackagingSiteOwnBrandsPage, ContractPackingPage)
    }

    s"should return correct missing answer list when producer is $None" in {
      val userAnswers = {
        emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, None).success.value
      }
      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
      val res = requiredUserAnswers.returnMissingAnswers(userAnswers, ImportsPage.previousPagesRequired)

      res mustBe List(ContractPackingPage)
    }
  }
}
