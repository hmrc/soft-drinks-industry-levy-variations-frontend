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
import controllers.correctReturn.routes
import models.backend.{RetrievedActivity, RetrievedSubscription, UkAddress}
import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.requests.CorrectReturnDataRequest
import models.{CheckMode, Contact, LitresInBands, ReturnPeriod}
import pages.RequiredPage
import pages.correctReturn._
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import java.time.LocalDate
import scala.concurrent.Future

class RequiredUserAnswersForCorrectReturnSpec extends SpecBase with DefaultAwaitTimeout {

  val requiredUserAnswers: RequiredUserAnswersForCorrectReturn = application.injector.instanceOf[RequiredUserAnswersForCorrectReturn]
  val basicUserAnswers= emptyUserAnswersForCorrectReturn
  val basicSubscription = RetrievedSubscription(
    "", "", "", UkAddress(List.empty, "", None),
    RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
    LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

//  val basicJourney =
//    List(
//      CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
//        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
//      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(AddASmallProducerPage,
//        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]),
//      CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(HowManyBroughtIntoUKPage,
//        Some(CorrectReturnPreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
//      CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(HowManyBroughtIntoUkFromSmallProducersPage,
//        Some(CorrectReturnPreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
//      CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(HowManyClaimCreditsForExportsPage,
//        Some(CorrectReturnPreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
//      CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
//      CorrectReturnRequiredPage(HowManyCreditsForLostDamagedPage,
//        Some(CorrectReturnPreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
//    )

  val basicJourney = List(
    RequiredPage(PackagedAsContractPackerPage),
    RequiredPage(HowManyPackagedAsContractPackerPage, additionalPreconditions = List(false)),
    RequiredPage(ExemptionsForSmallProducersPage),
    RequiredPage(AddASmallProducerPage, additionalPreconditions = List(false, true)),
    RequiredPage(BroughtIntoUKPage),
    RequiredPage(HowManyBroughtIntoUKPage, additionalPreconditions = List(false)),
    RequiredPage(BroughtIntoUkFromSmallProducersPage),
    RequiredPage(HowManyBroughtIntoUkFromSmallProducersPage, additionalPreconditions = List(false)),
    RequiredPage(ClaimCreditsForExportsPage),
    RequiredPage(HowManyClaimCreditsForExportsPage, additionalPreconditions = List(false)),
    RequiredPage(ClaimCreditsForLostDamagedPage),
    RequiredPage(HowManyCreditsForLostDamagedPage, additionalPreconditions = List(false)),
    RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(false, true)),
    RequiredPage(AskSecondaryWarehouseInReturnPage, additionalPreconditions = List(false))
  )

//  val importerFalseJourney =
//    List(CorrectReturnRequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))

  val importerFalseJourney = List(AskSecondaryWarehouseInReturnPage)

//  val smallProducerFalseJourney =
//    List(CorrectReturnRequiredPage(OperatePackagingSiteOwnBrandsPage, None)(implicitly[Reads[Boolean]]),
//    CorrectReturnRequiredPage(HowManyOperatePackagingSiteOwnBrandsPage,
//      Some(CorrectReturnPreviousPage(OperatePackagingSiteOwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))

  val smallProducerFalseJourney = List(
    RequiredPage(OperatePackagingSiteOwnBrandsPage),
    RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(false))
  )

//  val coPackerFalseJourney =
//    List(CorrectReturnRequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]))

  val coPackerFalseJourney = List(PackAtBusinessAddressPage)

  "checkYourAnswersRequiredData" - {

    "should return redirect to Packaged as Contract Packer page when user answers is empty and the small producer is true" in {
//      val res = requiredUserAnswers.checkYourAnswersRequiredData(basicUserAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, basicUserAnswers, basicSubscription)(Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url
    }
    "should return Redirect to Own brands page when user answers is empty and the small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false,largeProducer = true,contractPacker = true,importer = true,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

//      val res = requiredUserAnswers.checkYourAnswersRequiredData(basicUserAnswers, subscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, basicUserAnswers, subscription)(Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    }
    "should allow user to continue if all user answers are filled in and user is NOT newImporter && NOT co packer && NOT small producer" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
          .set(OperatePackagingSiteOwnBrandsPage, false).success.value
          .set(PackagedAsContractPackerPage, true).success.value
          .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
          .set(ExemptionsForSmallProducersPage, false).success.value
          .set(BroughtIntoUKPage, false).success.value
          .set(BroughtIntoUkFromSmallProducersPage, false).success.value
          .set(ClaimCreditsForExportsPage, false).success.value
          .set(ClaimCreditsForLostDamagedPage, false).success.value

//      val res = requiredUserAnswers.checkYourAnswersRequiredData(completedUserAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, completedUserAnswers, basicSubscription)(Future.successful(Ok("")))
      status(res) mustBe OK
    }
  }

  "requireData" - {
    "should take a random page and allow user to carry on their action with empty answers" in {
      val res = requiredUserAnswers.requireData(OperatePackagingSiteOwnBrandsPage, basicUserAnswers, basicSubscription)(Future.successful(Ok("")))
      status(res) mustBe OK
    }
    s"should check for $CorrectReturnBaseCYAPage and redirect to first missing page when answers incomplete but not a " +
      s"nil return and small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None,None,"",""), None)

      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, emptyUserAnswersForCorrectReturn.copy(data = Json.obj("foo" -> "bar")), subscription)(Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
    }

    s"should check for $CorrectReturnBaseCYAPage and redirect to missing page when answers incomplete but not a nil return and small producer is true" in {
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, emptyUserAnswersForCorrectReturn.copy(data = Json.obj("foo" -> "bar")), basicSubscription)(Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url
    }
    s"should check for $CorrectReturnBaseCYAPage and redirect to start page when answers data is empty" in {
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, basicUserAnswers, basicSubscription)(Future.successful(Ok("foo")))
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(CheckMode).url
    }

    s"should check for $CorrectReturnBaseCYAPage and allow the user to continue when all answers are complete" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val res =
        requiredUserAnswers.requireData(CorrectReturnBaseCYAPage, completedUserAnswers, basicSubscription)(Future.successful(Ok("")))
      status(res) mustBe OK
    }
  }

  "checkChangesRequiredData" - {
    "should return Redirect to CYA page when user answers past check your answers not submitted" in {
      val userAnswers = basicUserAnswers
      val res = requiredUserAnswers.checkChangesRequiredData(userAnswers, basicSubscription, Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.CorrectReturnCYAController.onPageLoad.url
    }

    "should return Redirect to Correction Reason page when user answers past check your answers is empty and balance repayment required" in {
      val userAnswers = basicUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(BalanceRepaymentRequired, true).success.value
      val res = requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, userAnswers, basicSubscription)(Future.successful(Ok("foo")))
//      val res = requiredUserAnswers.checkChangesRequiredData(userAnswers, basicSubscription, Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.CorrectionReasonController.onPageLoad(CheckMode).url
    }

    "should return Redirect to Repayment reason page if it is not filled in and balance repayment required" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(BalanceRepaymentRequired, true).success.value
        .set(CorrectionReasonPage, "some info").success.value

//      val res = requiredUserAnswers.checkChangesRequiredData(completedUserAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, completedUserAnswers, basicSubscription)(Future.successful(Ok("foo")))
      redirectLocation(res).get mustBe routes.RepaymentMethodController.onPageLoad(CheckMode).url
    }

    "should allow user to continue if Correction Reason and Repayment Method filled in and balance repayment required" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(BalanceRepaymentRequired, true).success.value
        .set(CorrectionReasonPage, "some info").success.value
        .set(RepaymentMethodPage, RepaymentMethod.BankAccount).success.value

//      val res = requiredUserAnswers.checkChangesRequiredData(completedUserAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, completedUserAnswers, basicSubscription)(Future.successful(Ok("foo")))
      status(res) mustBe OK
    }

    "should return Redirect to Correction Reason page when user answers past check your answers is empty and balance repayment not required" in {
      val userAnswers = basicUserAnswers
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(BalanceRepaymentRequired, false).success.value
//      val res = requiredUserAnswers.checkChangesRequiredData(userAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, userAnswers, basicSubscription)(Future.successful(Ok("foo")))
      redirectLocation(res).get mustBe routes.CorrectionReasonController.onPageLoad(CheckMode).url
    }

    "should allow user to continue if Correction Reason filled in and balance repayment not required" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(CorrectReturnBaseCYAPage, true).success.value
        .set(BalanceRepaymentRequired, false).success.value
        .set(CorrectionReasonPage, "some info").success.value

//      val res = requiredUserAnswers.checkChangesRequiredData(completedUserAnswers, basicSubscription, Future.successful(Ok("")))
      val res = requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, completedUserAnswers, basicSubscription)(Future.successful(Ok("foo")))
      status(res) mustBe OK
    }
  }

  "correctionReasonRequiredData" - {
    "should return Redirect to CYA page when user answers past check your answers not submitted" in {
      val userAnswers = basicUserAnswers
      val res = requiredUserAnswers.correctionReasonRequiredData(userAnswers, basicSubscription, Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.CorrectReturnCYAController.onPageLoad.url
    }
  }

  "repaymentMethodRequiredData" - {
    "should return Redirect to CYA page when user answers past check your answers not submitted" in {
      val userAnswers = basicUserAnswers
      val res = requiredUserAnswers.repaymentMethodRequiredData(userAnswers, basicSubscription, Future.successful(Ok("")))
      redirectLocation(res).get mustBe routes.CorrectReturnCYAController.onPageLoad.url
    }
  }

  "returnMissingAnswers" - {
    "should return all missing answers in a list when user answers is empty" in {
      val res = requiredUserAnswers.returnMissingAnswers(basicUserAnswers, basicSubscription, CorrectReturnBaseCYAPage.previousPagesRequired)
//      res mustBe List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
//        CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
//        CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
//        CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
//        CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
//        CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]))
      res mustBe List(
        PackagedAsContractPackerPage,
        ExemptionsForSmallProducersPage,
        BroughtIntoUKPage,
        BroughtIntoUkFromSmallProducersPage,
        ClaimCreditsForExportsPage,
        ClaimCreditsForLostDamagedPage
      )
    }

    "should return SOME missing answers when SOME answers are populated" in {
      val someAnswersCompleted = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

//      val journey = CorrectReturnBaseCYAPage.previousPagesRequired(someAnswersCompleted, basicSubscription)
//      val res = requiredUserAnswers.returnMissingAnswers(someAnswersCompleted, journey)
      val res = requiredUserAnswers.returnMissingAnswers(someAnswersCompleted, basicSubscription, CorrectReturnBaseCYAPage.previousPagesRequired)
      res mustBe List(PackagedAsContractPackerPage, ClaimCreditsForExportsPage)
    }
  }

  "mainRoute" - {

    "should return all correct answers if user is newImporter && newCopacker && small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value

      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = false, contractPacker =false, importer = false,voluntaryRegistration = false),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe basicJourney
    }

    "should return all correct answers if user is newImporter && NOT newCopacker && small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true,largeProducer = true, contractPacker =true,importer = false,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe basicJourney
    }

    "should return all correct answers if user is NOT newImporter && newCopacker && small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = false, importer = true,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe basicJourney
    }

    "should return all correct answers if user is NOT newImporter && NOT newcopacker &&  small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe basicJourney
    }

    "should return all correct answers if user is newImporter && newcopacker && small producer false" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe smallProducerFalseJourney ++ basicJourney
    }

    "should return all correct answers if user is NOT newImporter && NOT new co packer && NOT small producer" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(PackagedAsContractPackerPage, false).success.value
//        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res mustBe smallProducerFalseJourney ++ basicJourney
    }
  }

  "packingListReturnChange" - {
    "should be correct if user is NOT a new packer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)
//      val res = requiredUserAnswers.packingListReturnChange(basicUserAnswers, subscription)
//      res mustBe List.empty
      val res = CorrectReturnBaseCYAPage.previousPagesRequired(basicUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(PackAtBusinessAddressPage) mustBe false
    }

    "should be correct if user is a new packer" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = false, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)
//      val res = requiredUserAnswers.packingListReturnChange(completedUserAnswers, subscription)
//      res mustBe coPackerFalseJourney
      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(PackAtBusinessAddressPage) mustBe true
    }
  }

  "warehouseListReturnChange" - {
    "should be correct if user is NOT a new importer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = CorrectReturnBaseCYAPage.previousPagesRequired(basicUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(AskSecondaryWarehouseInReturnPage) mustBe false
    }

    "should be correct if user is a new importer" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(PackagedAsContractPackerPage, true).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForExportsPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = false, voluntaryRegistration = true),
        LocalDate.now(),List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(completedUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(AskSecondaryWarehouseInReturnPage) mustBe true
    }
  }

  "smallProducerCheck" - {
    "should be correct if user is NOT a new small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(),List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(basicUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(OperatePackagingSiteOwnBrandsPage) mustBe false
//      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(HowManyOperatePackagingSiteOwnBrandsPage) mustBe false
    }

    "should be correct if user is a new small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = CorrectReturnBaseCYAPage.previousPagesRequired(basicUserAnswers, subscription)
      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(OperatePackagingSiteOwnBrandsPage) mustBe true
//      res.filter(!_.additionalPreconditions.contains(false)).map(_.page).contains(HowManyOperatePackagingSiteOwnBrandsPage) mustBe true
    }
  }
}
