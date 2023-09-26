///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.actions
//
//import base.SpecBase
//import models.HowManyLitresGlobally.{Large, Small}
//import models.OrganisationType.LimitedCompany
//import models.Verify.YesRegister
//import models.backend.UkAddress
//import models.requests.DataRequest
//import models.{CheckMode, ContactDetails, HowManyLitresGlobally, LitresInBands, OrganisationType, RosmRegistration, RosmWithUtr, UserAnswers, Verify}
//import pages.{Page, QuestionPage}
//import pages.changeActivity._
//import play.api.libs.json.Reads
//import play.api.mvc.Results.Ok
//import play.api.mvc.{AnyContentAsEmpty, Result}
//import play.api.test.Helpers.{contentAsString, redirectLocation}
//import play.api.test.{DefaultAwaitTimeout, FakeRequest}
//
//import java.time.LocalDate
//import scala.concurrent.Future
//
//class RequiredUserAnswersForChangeActivitySpec extends SpecBase with DefaultAwaitTimeout {
//
//  val requiredUserAnswers: RequiredUserAnswersForChangeActivity = application.injector.instanceOf[RequiredUserAnswersForChangeActivity]
//  val exampleSuccessActionResult: String = "woohoo"
//  val exampleSuccessAction: Future[Result] = Future.successful(Ok(exampleSuccessActionResult))
//
//  def dataRequest(userAnswers: UserAnswers): DataRequest[AnyContentAsEmpty.type] = DataRequest(
//    FakeRequest(), "", hasCTEnrolment = false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty, "", None)))
//  )
//  "requireData" - {
//    "should return result passed in when not a page matched in function" in {
//      contentAsString(requiredUserAnswers.requireData(VerifyPage)(exampleSuccessAction)(dataRequest(emptyUserAnswersForChangeActivity))) mustBe exampleSuccessActionResult
//    }
//    s"should return result passed in when page is $CheckYourAnswersPage" - {
//      s"when HowManyLitresGlobally is $Large and all answers are answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Large).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(PackAtBusinessAddressPage, true).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//    }
//
//    s"should return result passed in when page is $CheckYourAnswersPage" - {
//      s"when HowManyLitresGlobally is $Large and contractPacking and OperatePackagingSites are both false and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Large).success.value
//            .set(OperatePackagingSitesPage, false).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when $HowManyLitresGlobally is $Small contract packing is false and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when $HowManyLitresGlobally is $None and all answers are answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(PackAtBusinessAddressPage, true).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when $HowManyLitresGlobally is $None, contract packing is false and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when all $LitresInBands are not required" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, false).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, false).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(PackAtBusinessAddressPage, true).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//      "when warehouses are not required" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(PackAtBusinessAddressPage, true).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//            .set(AskSecondaryWarehousesPage, false).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when HowManyLitresGlobally is $Small, $ThirdPartyPackagersPage is true, $ContractPackingPage is false, $OperatePackagingSitesPage " +
//        s"is false, $ImportsPage is false, and $ContactDetailsPage has been answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, false).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, false).success.value
//            .set(ContactDetailsPage, ContactDetails("aaa", "aaa", "123", "a@a.com")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//
//      s"when HowManyLitresGlobally is $Small, $ThirdPartyPackagersPage is true, $OperatePackagingSitesPage is true, $ContractPackingPage " +
//        s"is false, $ImportsPage is false, and $ContactDetailsPage has been answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, false).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers))) mustBe exampleSuccessActionResult
//      }
//    }
//
//    s"should redirect to verify controller when missing answers for $CheckYourAnswersPage" - {
//      "with no answers" in {
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(emptyUserAnswersForChangeActivity)))
//        res.get mustBe controllers.routes.VerifyController.onPageLoad(CheckMode).url
//      }
//      "with missing selection of pages and verify is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(PackagingSiteDetailsPage, true).success.value
//            .set(AskSecondaryWarehousesPage, false).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.routes.VerifyController.onPageLoad(CheckMode).url
//      }
//    }
//    s"should redirect to the appropriate missing page when missing answers for $CheckYourAnswersPage" - {
//
//      s"when HowManyLitresGlobally is $Large and contractPacking is false, OperatePackagingSites is true and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Large).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, false).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
//      }
//
//      s"when HowManyLitresGlobally is $Large and contractPacking is true, OperatePackagingSites is false and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Large).success.value
//            .set(OperatePackagingSitesPage, false).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
//      }
//
//      s"when HowManyLitresGlobally is $Small, $OperatePackagingSitesPage is true, $ContractPackingPage is true, and " +
//        s"pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, Small).success.value
//            .set(ThirdPartyPackagersPage, true).success.value
//            .set(OperatePackagingSitesPage, true).success.value
//            .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
//      }
//
//      s"when HowManyLitresGlobally is $None and contractPacking is true and pack at business address is not answered" in {
//        val userAnswers = {
//          emptyUserAnswersForChangeActivity
//            .set(VerifyPage, YesRegister).success.value
//            .set(OrganisationTypePage, LimitedCompany).success.value
//            .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//            .set(ContractPackingPage, true).success.value
//            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//            .set(ImportsPage, true).success.value
//            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//            .set(StartDatePage, LocalDate.now()).success.value
//            .set(AskSecondaryWarehousesPage, true).success.value
//            .set(WarehouseDetailsPage, true).success.value
//            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//        }
//        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//        res.get mustBe controllers.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage, ContractPackingPage,
//        HowManyContractPackingPage,
//        OperatePackagingSitesPage, HowManyOperatePackagingSitesPage,
//        ImportsPage, HowManyImportsPage,
//        PackAtBusinessAddressPage, PackagingSiteDetailsPage,
//        StartDatePage, AskSecondaryWarehousesPage,
//        WarehouseDetailsPage, ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers set to true, the user is taken to that page that is required for ${HowManyLitresGlobally.Large}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.Large).success.value
//              .set(ContractPackingPage, true).success.value
//              .set(ThirdPartyPackagersPage, true).success.value
//              .set(OperatePackagingSitesPage, true).success.value
//              .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//              .set(ImportsPage, true).success.value
//              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//              .set(PackAtBusinessAddressPage, true).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, true).success.value
//              .set(WarehouseDetailsPage, true).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage,
//        OperatePackagingSitesPage,
//        ImportsPage,
//        StartDatePage, AskSecondaryWarehousesPage,
//        ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers set to false, the user is taken to that page that is required for ${HowManyLitresGlobally.Large}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.Large).success.value
//              .set(ContractPackingPage, false).success.value
//              .set(ThirdPartyPackagersPage, false).success.value
//              .set(OperatePackagingSitesPage, false).success.value
//              .set(ImportsPage, false).success.value
//              .set(PackAtBusinessAddressPage, false).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, false).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage, ContractPackingPage,
//        HowManyContractPackingPage, ThirdPartyPackagersPage,
//        OperatePackagingSitesPage, HowManyOperatePackagingSitesPage,
//        ImportsPage, HowManyImportsPage,
//        PackAtBusinessAddressPage, PackagingSiteDetailsPage,
//        StartDatePage, AskSecondaryWarehousesPage,
//        WarehouseDetailsPage, ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required for ${HowManyLitresGlobally.Small}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.Small).success.value
//              .set(ContractPackingPage, true).success.value
//              .set(ThirdPartyPackagersPage, true).success.value
//              .set(OperatePackagingSitesPage, true).success.value
//              .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//              .set(ImportsPage, true).success.value
//              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//              .set(PackAtBusinessAddressPage, true).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, true).success.value
//              .set(WarehouseDetailsPage, true).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage, ContractPackingPage,
//        ThirdPartyPackagersPage,
//        OperatePackagingSitesPage,
//        ImportsPage, HowManyImportsPage,
//        PackagingSiteDetailsPage, StartDatePage,
//        AskSecondaryWarehousesPage, ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required for ${HowManyLitresGlobally.Small}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.Small).success.value
//              .set(ContractPackingPage, false).success.value
//              .set(ThirdPartyPackagersPage, false).success.value
//              .set(OperatePackagingSitesPage, false).success.value
//              .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//              .set(ImportsPage, true).success.value
//              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//              .set(PackAtBusinessAddressPage, false).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, false).success.value
//              .set(WarehouseDetailsPage, false).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage, ContractPackingPage,
//        HowManyContractPackingPage,
//        ImportsPage, HowManyImportsPage,
//        StartDatePage, AskSecondaryWarehousesPage,
//        WarehouseDetailsPage, ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers of true, the user is taken to that page that is required for ${HowManyLitresGlobally.None}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//              .set(ContractPackingPage, true).success.value
//              .set(ThirdPartyPackagersPage, true).success.value
//              .set(OperatePackagingSitesPage, true).success.value
//              .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//              .set(ImportsPage, true).success.value
//              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//              .set(PackAtBusinessAddressPage, true).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, true).success.value
//              .set(WarehouseDetailsPage, true).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//      List[QuestionPage[_]](VerifyPage, OrganisationTypePage,
//        HowManyLitresGloballyPage, ContractPackingPage,
//        ImportsPage, HowManyImportsPage,
//        StartDatePage, AskSecondaryWarehousesPage,
//        ContactDetailsPage).foreach { eachPage =>
//        s"when $eachPage is filtered out from a full list of user answers of false, the user is taken to that page that is required for ${HowManyLitresGlobally.None}" in {
//          val userAnswers = {
//            emptyUserAnswersForChangeActivity
//              .set(VerifyPage, YesRegister).success.value
//              .set(OrganisationTypePage, LimitedCompany).success.value
//              .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//              .set(ContractPackingPage, false).success.value
//              .set(ThirdPartyPackagersPage, false).success.value
//              .set(OperatePackagingSitesPage, false).success.value
//              .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//              .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//              .set(ImportsPage, true).success.value
//              .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//              .set(PackAtBusinessAddressPage, false).success.value
//              .set(PackagingSiteDetailsPage, false).success.value
//              .set(StartDatePage, LocalDate.now()).success.value
//              .set(AskSecondaryWarehousesPage, false).success.value
//              .set(WarehouseDetailsPage, false).success.value
//              .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//          }.remove(eachPage).success.value
//          val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(exampleSuccessAction)(dataRequest(userAnswers)))
//          res.get mustBe eachPage.url(CheckMode)
//        }
//      }
//    }
//  }
//  "returnMissingAnswers" - {
//    "should return all missing answers when user answers is empty" in {
//      implicit val dataRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
//        FakeRequest(),"", hasCTEnrolment = false, None, emptyUserAnswersForChangeActivity, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
//      )
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe
//        List(
//          RequiredPage(VerifyPage, List.empty)(implicitly[Reads[Verify]]),
//          RequiredPage(OrganisationTypePage, List.empty)(implicitly[Reads[OrganisationType]]),
//          RequiredPage(HowManyLitresGloballyPage, List.empty)(implicitly[Reads[HowManyLitresGlobally]]),
//          RequiredPage(ContractPackingPage, List.empty)(implicitly[Reads[Boolean]]),
//          RequiredPage(ImportsPage, List.empty)(implicitly[Reads[Boolean]]),
//        )
//    }
//
//    "should return all but 1 missing answers when user answers is fully populated apart from 1 answer" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//          .set(PackagingSiteDetailsPage, true).success.value
//          .set(AskSecondaryWarehousesPage, false).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(ContactDetailsPage, List(
//        PreviousPage(AskSecondaryWarehousesPage, List(true, false))
//        (implicitly[Reads[Boolean]])))(implicitly[Reads[ContactDetails]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Large, contractPacking is false, OperatePackagingSites " +
//      "is true and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Large).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(AskSecondaryWarehousesPage, true).success.value
//          .set(WarehouseDetailsPage, true).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("large").get))(implicitly[Reads[HowManyLitresGlobally]]),
//        PreviousPage(OperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]]),
//        PreviousPage(ContractPackingPage, List(true, false))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Large, $ContactDetailsPage is true, $OperatePackagingSitesPage " +
//      "is true and PackAtBusinessAddress is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Large).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(AskSecondaryWarehousesPage, true).success.value
//          .set(WarehouseDetailsPage, true).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("large").get))(implicitly[Reads[HowManyLitresGlobally]]),
//        PreviousPage(OperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]]),
//        PreviousPage(ContractPackingPage, List(true, false))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $Small, $ContractPackingPage is true, $OperatePackagingSitesPage " +
//      "is true, and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(AskSecondaryWarehousesPage, true).success.value
//          .set(WarehouseDetailsPage, true).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get,
//          HowManyLitresGlobally.enumerable.withName("xnot").get))(implicitly[Reads[HowManyLitresGlobally]]),
//        PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when producer is $None, $ContractPackingPage is true, and pack at business address is not answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(AskSecondaryWarehousesPage, true).success.value
//          .set(WarehouseDetailsPage, true).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(RequiredPage(PackAtBusinessAddressPage, List(
//        PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get,
//          HowManyLitresGlobally.enumerable.withName("xnot").get))(implicitly[Reads[HowManyLitresGlobally]]),
//        PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]))
//    }
//
//    s"should return 1 item on the missing answer list when user changes a $StartDatePage required answer from CYA, " +
//      s"such as change $ImportsPage from false to true" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 1)).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(
//        RequiredPage(StartDatePage, List(
//          PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get,
//            HowManyLitresGlobally.enumerable.withName("xnot").get))(implicitly[Reads[HowManyLitresGlobally]]),
//          PreviousPage(ImportsPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LocalDate]]))
//    }
//
//    s"should return 1 item on the missing answer list when user is $Small with $ThirdPartyPackagersPage true, $OperatePackagingSitesPage " +
//      s"true, and $ImportsPage false" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSitesPage, false).success.value
//          .set(ContractPackingPage, false).success.value
//          .set(ImportsPage, false).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
//      res mustBe List(
//        RequiredPage(ContactDetailsPage, List(
//          PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get))(implicitly[Reads[HowManyLitresGlobally]]),
//          PreviousPage(ThirdPartyPackagersPage, List(true))(implicitly[Reads[Boolean]]),
//          PreviousPage(OperatePackagingSitesPage, List(true, false))(implicitly[Reads[Boolean]]),
//          PreviousPage(ContractPackingPage, List(false))(implicitly[Reads[Boolean]]),
//          PreviousPage(ImportsPage, List(false))(implicitly[Reads[Boolean]])))(implicitly[Reads[ContactDetails]]))
//    }
//
//    "should return nothing when a list is provided for previous pages and previous pages don't exist" in {
//      val requiredPages = {
//        List(RequiredPage(ContactDetailsPage,
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
//        List(RequiredPage(ContactDetailsPage,
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
//        List(RequiredPage(ContactDetailsPage,
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
//        List(RequiredPage(ContactDetailsPage,
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
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
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
//      redirectLocation(requiredUserAnswers.checkYourAnswersRequiredData(exampleSuccessAction)).get mustBe controllers.routes.VerifyController.onPageLoad(CheckMode).url
//    }
//    "should redirect to action when all answers answered" in {
//      val userAnswers = {
//        emptyUserAnswersForChangeActivity
//          .set(VerifyPage, YesRegister).success.value
//          .set(OrganisationTypePage, LimitedCompany).success.value
//          .set(HowManyLitresGloballyPage, Small).success.value
//          .set(ThirdPartyPackagersPage, true).success.value
//          .set(OperatePackagingSitesPage, true).success.value
//          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
//          .set(ContractPackingPage, true).success.value
//          .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
//          .set(ImportsPage, true).success.value
//          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
//          .set(StartDatePage, LocalDate.now()).success.value
//          .set(PackAtBusinessAddressPage, true).success.value
//          .set(PackagingSiteDetailsPage, true).success.value
//          .set(AskSecondaryWarehousesPage, false).success.value
//          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
//      }
//      implicit val request: DataRequest[AnyContentAsEmpty.type] = dataRequest(userAnswers)
//      contentAsString(requiredUserAnswers.checkYourAnswersRequiredData(exampleSuccessAction)) mustBe exampleSuccessActionResult
//    }
//  }
//}
