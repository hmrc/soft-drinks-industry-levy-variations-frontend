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
import models.backend.UkAddress
import models.correctReturn.AddASmallProducer
import models.requests.CorrectReturnDataRequest
import models.{Contact, LitresInBands, NormalMode, RetrievedActivity, RetrievedSubscription, ReturnPeriod}
import pages.correctReturn._
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import java.time.LocalDate
import scala.concurrent.Future

class RequiredUserAnswersForCorrectReturnSpec extends SpecBase with DefaultAwaitTimeout {

  val requiredUserAnswers: RequiredUserAnswersForCorrectReturn = application.injector.instanceOf[RequiredUserAnswersForCorrectReturn]
  val basicRequestWithEmptyAnswers = CorrectReturnDataRequest(FakeRequest(),
    "",
    RetrievedSubscription(
      "","","", UkAddress(List.empty, "", None),
      RetrievedActivity(smallProducer = true,largeProducer = true,contractPacker = true,importer = true,voluntaryRegistration = true),
      LocalDate.now(),List.empty, List.empty,Contact(None,None,"",""),None),
    emptyUserAnswersForCorrectReturn,
    ReturnPeriod(2022, 3)
  )

  val basicJourney =
    List(
      CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(AddASmallProducerPage,
        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]),
      CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(correctReturn.HowManyBroughtIntoUKPage,
        Some(CorrectReturnPreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(correctReturn.HowManyBroughtIntoUkFromSmallProducersPage,
        Some(CorrectReturnPreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(correctReturn.HowManyClaimCreditsForExportsPage,
        Some(CorrectReturnPreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyCreditsForLostDamagedPage,
        Some(CorrectReturnPreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
    )

  val importerFalseJourney =
    List(CorrectReturnRequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))

  val smallProducerFalseJourney =
    List(CorrectReturnRequiredPage(OperatePackagingSiteOwnBrandsPage, None)(implicitly[Reads[Boolean]]),
    CorrectReturnRequiredPage(HowManyOperatePackagingSiteOwnBrandsPage,
      Some(CorrectReturnPreviousPage(OperatePackagingSiteOwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))

  val coPackerFalseJourney =
    List(CorrectReturnRequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]),
    CorrectReturnRequiredPage(PackagingSiteDetailsPage, None)(implicitly[Reads[Boolean]]))

  "checkYourAnswersRequiredData" - {

    "should return redirect to Packaged as Contract Packer page when user answers is empty and the small producer is true" in {
      val res = requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers)
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url
    }
    "should return Redirect to Own brands page when user answers is empty and the small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false,largeProducer = true,contractPacker = true,importer = true,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(subscription = subscription))
      redirectLocation(res).get mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
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

      val res =
        requiredUserAnswers.checkYourAnswersRequiredData(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers))
      status(res) mustBe OK
    }
  }

  "requireData" - {
    "should take a random page and allow user to carry on their action with empty answers" in {
      val res = requiredUserAnswers.requireData(OperatePackagingSiteOwnBrandsPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers)
      status(res) mustBe OK
    }
    s"should check for $CorrectReturnBaseCYAPage and redirect to first missing page when answers incomplete but not a " +
      s"nil return and small producer is false" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None,None,"",""), None)

      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(
        subscription = subscription, userAnswers = emptyUserAnswersForCorrectReturn.copy(data = Json.obj("foo" -> "bar"))))
      redirectLocation(res).get mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
    }

    s"should check for $CorrectReturnBaseCYAPage and redirect to missing page when answers incomplete but not a nil return and small producer is true" in {
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers.copy(
        userAnswers = emptyUserAnswersForCorrectReturn
          .copy(data = Json.obj("foo" -> "bar"))))
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url
    }
    s"should check for $CorrectReturnBaseCYAPage and redirect to start page when answers data is empty" in {
      val res = requiredUserAnswers.requireData(CorrectReturnBaseCYAPage)(Future.successful(Ok("foo")))(basicRequestWithEmptyAnswers)
      redirectLocation(res).get mustBe routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url
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
        requiredUserAnswers.requireData(CorrectReturnBaseCYAPage)(Future.successful(Ok("")))(basicRequestWithEmptyAnswers
          .copy(userAnswers = completedUserAnswers))
      status(res) mustBe OK
    }
  }

  "returnMissingAnswers" - {
    "should return all missing answers in a list when user answers is empty" in {
      implicit val request = basicRequestWithEmptyAnswers
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.mainRoute)
      res mustBe List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]))
    }

    "should return SOME missing answers when SOME answers are populated" in {
      val someAnswersCompleted = emptyUserAnswersForCorrectReturn
        .set(OperatePackagingSiteOwnBrandsPage, false).success.value
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(ExemptionsForSmallProducersPage, false).success.value
        .set(BroughtIntoUKPage, false).success.value
        .set(BroughtIntoUkFromSmallProducersPage, false).success.value
        .set(ClaimCreditsForLostDamagedPage, false).success.value

      implicit val request = basicRequestWithEmptyAnswers.copy(userAnswers = someAnswersCompleted)
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.mainRoute)
      res mustBe List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]))
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
      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ coPackerFalseJourney ++ importerFalseJourney
    }

    "should return all correct answers if user is newImporter && NOT newCopacker && small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true,largeProducer = true, contractPacker =true,importer = false,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ importerFalseJourney
    }

    "should return all correct answers if user is NOT newImporter && newCopacker && small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = false, importer = true,voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers,subscription = subscription))

      res mustBe basicJourney ++ coPackerFalseJourney
    }

    "should return all correct answers if user is NOT newImporter && NOT newcopacker &&  small producer true" in {
      val completedUserAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(100, 652)).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(259, 923)).success.value
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers, subscription = subscription))

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

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers, subscription = subscription))

      res mustBe smallProducerFalseJourney ++ basicJourney ++ coPackerFalseJourney ++ importerFalseJourney
    }

    "should return all correct answers if user is NOT newImporter && NOT new co packer && NOT small producer" in {
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
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = requiredUserAnswers.mainRoute(basicRequestWithEmptyAnswers.copy(userAnswers = completedUserAnswers, subscription = subscription))

      res mustBe smallProducerFalseJourney ++ basicJourney
    }
  }

  "packingListReturnChange" - {
    "should be correct if user is NOT a new packer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)
      val res = requiredUserAnswers.packingListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription))

      res mustBe List.empty
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
      val res = requiredUserAnswers.packingListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription, userAnswers = completedUserAnswers))
      res mustBe coPackerFalseJourney
    }
  }

  "warehouseListReturnChange" - {
    "should be correct if user is NOT a new importer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(),List.empty,List.empty,Contact(None,None,"",""),None)
      val res = requiredUserAnswers.warehouseListReturnChange(basicRequestWithEmptyAnswers.copy(subscription = subscription))
      res mustBe List.empty
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
      val res = requiredUserAnswers.warehouseListReturnChange(basicRequestWithEmptyAnswers
        .copy(subscription = subscription, userAnswers = completedUserAnswers))

      res mustBe importerFalseJourney
    }
  }

  "smallProducerCheck" - {
    "should be correct if user is NOT a new small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(),List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = requiredUserAnswers.smallProducerCheck(subscription)
      res mustBe List.empty
    }

    "should be correct if user is a new small producer" in {
      val subscription = RetrievedSubscription(
        "","","", UkAddress(List.empty, "", None),
        RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true, importer = true, voluntaryRegistration = true),
        LocalDate.now(), List.empty, List.empty, Contact(None, None, "", ""), None)

      val res = requiredUserAnswers.smallProducerCheck(subscription)

      res mustBe smallProducerFalseJourney
    }
  }
}
