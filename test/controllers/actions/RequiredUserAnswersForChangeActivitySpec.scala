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
import models.changeActivity.AmountProduced.{Large, None, Small}
import models.changeActivity.AmountProduced
import models.requests.{DataRequest, RequiredDataRequest}
import models.{CheckMode, LitresInBands, UserAnswers}
import pages.{Page, QuestionPage}
import pages.changeActivity._
import play.api.libs.json.Reads
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.{contentAsString, redirectLocation}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import java.time.LocalDate
import scala.concurrent.Future

class RequiredUserAnswersForChangeActivitySpec extends SpecBase with DefaultAwaitTimeout {

  val requiredUserAnswers: RequiredUserAnswersForChangeActivity = application.injector.instanceOf[RequiredUserAnswersForChangeActivity]
  val exampleSuccessActionResult: String = "woohoo"
  val exampleSuccessAction: Future[Result] = Future.successful(Ok(exampleSuccessActionResult))

  def dataRequest(userAnswers: UserAnswers): DataRequest[AnyContentAsEmpty.type] = RequiredDataRequest(FakeRequest(), "", aSubscription, userAnswers)
  "requireData" - {
    "should return result passed in when not a page matched in function" in {
      contentAsString(requiredUserAnswers.requireData(AmountProducedPage)(exampleSuccessAction)(dataRequest(emptyUserAnswersForChangeActivity))) mustBe exampleSuccessActionResult
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
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
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
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when $AmountProducedPage is $Small contract packing is false and pack at business address is not answered" in {
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
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
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
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
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
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }

      s"when all $LitresInBands are not required" in {
        val userAnswers = {
          emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSiteOwnBrandsPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, false).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }
      "when warehouses are not required" in {
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
            .set(PackagingSiteDetailsPage, true).success.value
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
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
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
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
        }
        contentAsString(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
      }
    }

//    s"should redirect to verify controller when missing answers for $ChangeActivityCYAPage" - {
//      "with no answers" in {
//        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(emptyUserAnswersForChangeActivity)))
//        res.get mustBe controllers.changeActivity.routes.VerifyController.onPageLoad(CheckMode).url
//      }
//      "with missing selection of pages and verify is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(AmountProducedPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(ChangeActivityCYAPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.changeActivity.routes.VerifyController.onPageLoad(CheckMode).url
//      }
//    }
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
// TODO: URL STUFF BELOW
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
      List[QuestionPage[_]](
        AmountProducedPage, ContractPackingPage,
        HowManyContractPackingPage,
        OperatePackagingSiteOwnBrandsPage, HowManyOperatePackagingSiteOwnBrandsPage,
        ImportsPage, HowManyImportsPage,
        PackAtBusinessAddressPage, PackagingSiteDetailsPage,
        SecondaryWarehouseDetailsPage).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers set to true, the user is taken to that page that is required for ${AmountProduced.Large}" in {
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
        s"when $eachPage is filtered out from a full list of user answers set to false, the user is taken to that page that is required for ${AmountProduced.Large}" in {
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
        AmountProducedPage, ContractPackingPage,
        HowManyContractPackingPage, ThirdPartyPackagersPage,
        OperatePackagingSiteOwnBrandsPage, HowManyOperatePackagingSiteOwnBrandsPage,
        ImportsPage, HowManyImportsPage,
        PackAtBusinessAddressPage, PackagingSiteDetailsPage,
        SecondaryWarehouseDetailsPage).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required for ${AmountProduced.Small}" in {
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
        AmountProducedPage, ContractPackingPage,
        ThirdPartyPackagersPage,
        OperatePackagingSiteOwnBrandsPage,
        ImportsPage, HowManyImportsPage,
        PackagingSiteDetailsPage 
         ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required for ${AmountProduced.Small}" in {
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
        AmountProducedPage, ContractPackingPage,
        HowManyContractPackingPage,
        ImportsPage, HowManyImportsPage,
        SecondaryWarehouseDetailsPage).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required for ${AmountProduced.None}" in {
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
        AmountProducedPage, ContractPackingPage,
        ImportsPage, HowManyImportsPage
        ).foreach { eachPage =>
        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required for ${AmountProduced.None}" in {
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
  // TODO: URL STUFF ABOVE
//  "returnMissingAnswers" - {
//    "should return all missing answers when user answers is empty" in {
//      implicit val dataRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
//        FakeRequest(),"", hasCTEnrolment = false, None, emptyUserAnswersForChangeActivity, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
//      )
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe
//        List(
//          RequiredPage(AmountProducedPage, List.empty)(implicitly[Reads[AmountProduced]]),
//          RequiredPage(ContractPackingPage, List.empty)(implicitly[Reads[Boolean]]),
//          RequiredPage(ImportsPage, List.empty)(implicitly[Reads[Boolean]]),
//        )
//    }
//
//    "should return all but 1 missing answers when user answers is fully populated apart from 1 answer" in {
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
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(, List(
//        PreviousPage( List(true, false))
//        (implicitly[Reads[Boolean]])))(implicitly[Reads[ContactDetails]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Large, contractPacking is false, OperatePackagingSites " +
//      "is true and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Large).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(SecondaryWarehouseDetailsPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("large").get))(implicitly[Reads[AmountProduced]]),
//        PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitly[Reads[Boolean]]),
//        PreviousPage(ContractPackingPage, List(true, false))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Large, $ is true, $OperatePackagingSiteOwnBrandsPage " +
//      "is true and PackAtBusinessAddress is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Large).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(SecondaryWarehouseDetailsPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("large").get))(implicitly[Reads[AmountProduced]]),
//        PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitly[Reads[Boolean]]),
//        PreviousPage(ContractPackingPage, List(true, false))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Small, $ContractPackingPage is true, $OperatePackagingSiteOwnBrandsPage " +
//      "is true, and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(SecondaryWarehouseDetailsPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("small").get,
//          AmountProduced.enumerable.withName("none").get))(implicitly[Reads[AmountProduced]]),
//        PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $None, $ContractPackingPage is true, and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, AmountProduced.None).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(SecondaryWarehouseDetailsPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("small").get,
//          AmountProduced.enumerable.withName("none").get))(implicitly[Reads[AmountProduced]]),
//        PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when user changes a $StartDatePage required answer from CYA, " +
//      s"such as change $ImportsPage from false to true" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
//          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(
//        RequiredPage(List(
//          PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("small").get,
//            AmountProduced.enumerable.withName("none").get))(implicitly[Reads[AmountProduced]]),
//          PreviousPage(ImportsPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LocalDate]]))
//    }
//
//    s"should return 1 item on the missing answer list when user is $Small with $ThirdPartyPackagersPage true, $OperatePackagingSiteOwnBrandsPage " +
//      s"true, and $ImportsPage false" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(AmountProducedPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSiteOwnBrandsPage, false).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, false).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(
//        RequiredPage(, List(
//          PreviousPage(AmountProducedPage, List(AmountProduced.enumerable.withName("small").get))(implicitly[Reads[AmountProduced]]),
//          PreviousPage(ThirdPartyPackagersPage, List(true))(implicitly[Reads[Boolean]]),
//          PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true, false))(implicitly[Reads[Boolean]]),
//          PreviousPage(ContractPackingPage, List(false))(implicitly[Reads[Boolean]]),
//          PreviousPage(ImportsPage, List(false))(implicitly[Reads[Boolean]])))(implicitly[Reads[ContactDetails]]))
//    }
//
//    "should return nothing when a list is provided for previous pages and previous pages don't exist" in {
//      val requiredPages = {
//        List(RequiredPage(,
//          List(
//            PreviousPage(HowManyImportsPage, List(LitresInBands(1,1)))(implicitly[Reads[LitresInBands]]),
//            PreviousPage(PackAtBusinessAddressPage, List(true))(implicitly[Reads[Boolean]])
//          )
//        )(implicitly[Reads[ContactDetails]]))
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(emptyUserAnswersForChangeActivity)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredPages)
//      res mustBe List.empty
//    }
//    "should return Required Page when both previous pages have correct matching data" in {
//      val requiredPages = {
//        List(RequiredPage(,
//          List(
//            PreviousPage(HowManyImportsPage, List(LitresInBands(1,1)))(implicitly[Reads[LitresInBands]]),
//            PreviousPage(PackAtBusinessAddressPage, List(true))(implicitly[Reads[Boolean]])
//          )
//        )(implicitly[Reads[ContactDetails]]))
//      }
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredPages)
//      res mustBe requiredPages
//    }
//    "should NOT return Required Page when both previous pages have data but one doesnt match" in {
//      val requiredPages = {
//        List(RequiredPage(,
//          List(
//            PreviousPage(HowManyImportsPage, List(LitresInBands(2,3)))(implicitly[Reads[LitresInBands]]),
//            PreviousPage(PackAtBusinessAddressPage, List(true))(implicitly[Reads[Boolean]])
//          )
//        )(implicitly[Reads[ContactDetails]]))
//      }
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//      }
//
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredPages)
//      res mustBe List.empty
//    }
//    "should NOT return Required Page when both previous pages have data both match and current required page is populated" in {
//      val requiredPages = {
//        List(RequiredPage(,
//          List(
//            PreviousPage(HowManyImportsPage, List(LitresInBands(1,1)))(implicitly[Reads[LitresInBands]]),
//            PreviousPage(PackAtBusinessAddressPage, List(true))(implicitly[Reads[Boolean]])
//          )
//        )(implicitly[Reads[ContactDetails]]))
//      }
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredPages)
//      res mustBe List.empty
//    }
//  }
//  "checkYourAnswersRequiredData" - {
//    "should redirect to verify controller when missing answers" in {
//      implicit val dataRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
//        FakeRequest(),"", hasCTEnrolment = false, None, emptyUserAnswersForChangeActivity, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
//      )
//      redirectLocation(requiredUserAnswers.checkYourAnswersRequiredData(exampleSuccessAction)).get mustBe controllers.changeActivity.routes.VerifyController.onPageLoad(CheckMode).url
//    }
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
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      contentAsString(requiredUserAnswers.checkYourAnswersRequiredData(exampleSuccessAction)) mustBe exampleSuccessActionResult
//    }
//  }
}
