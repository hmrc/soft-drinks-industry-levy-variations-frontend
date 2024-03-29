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

package navigation

import base.SpecBase
import controllers.correctReturn.routes
import models._
import models.backend.RetrievedSubscription
import pages._
import pages.correctReturn._
import play.api.libs.json.Json

class NavigatorForCorrectReturnSpec extends SpecBase with DataHelper {

  val navigator = new NavigatorForCorrectReturn

  val newImporterUserAnswers: UserAnswers = emptyUserAnswersForCorrectReturn
    .set(BroughtIntoUKPage, true).success.value

  val newCoPackerUserAnswers: UserAnswers = emptyUserAnswersForCorrectReturn
    .set(PackagedAsContractPackerPage, true).success.value

  val currentImporterSubscription: Option[RetrievedSubscription] = Option(aSubscription.copy(activity = testRetrievedActivity(importer = true)))
  val currentCoPackerSubscription: Option[RetrievedSubscription] = Option(aSubscription.copy(activity = testRetrievedActivity(contractPacker = true)))
  val currentImporterAndCoPackerSubscription: Option[RetrievedSubscription] =
    Option(aSubscription.copy(activity = testRetrievedActivity(contractPacker = true, importer = true)))

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe defaultCall
      }

      "must go from repayment method page to check changes page" in {
        navigator.nextPage(RepaymentMethodPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe
          controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe
          controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad
      }
    }
  }

  "Brought into UK" - {
    def navigateFromBroughtIntoUkPage(value: Boolean, mode: Mode) =
      navigator.nextPage(BroughtIntoUKPage, mode, emptyUserAnswersForCorrectReturn.set(BroughtIntoUKPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How many brought into UK in $mode" in {
        val result = navigateFromBroughtIntoUkPage(value = true, mode)
        result mustBe routes.HowManyBroughtIntoUKController.onPageLoad(mode)
      }
    })

    "select No to navigate to brought into uk from small producers page in NormalMode" in {
      val result = navigateFromBroughtIntoUkPage(value = false, NormalMode)
      result mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    }

    "Should No to navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromBroughtIntoUkPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "How many brought into UK" - {
    def navigateFromHowManyBroughtIntoUkPage(mode: Mode) =
      navigator.nextPage(HowManyBroughtIntoUKPage, mode, emptyUserAnswersForCorrectReturn.set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value)

    "navigate to brought into uk from small producers page in NormalMode" in {
      val result = navigateFromHowManyBroughtIntoUkPage(NormalMode)
      result mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode (when not new importer or new copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when already importer and already copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, subscription = currentImporterAndCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when currently importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, None, currentImporterSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when currently copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, None, currentCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Return Change Registration page in CheckMode (when new importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(CheckMode)
    }

    "navigate to Return Change Registration page in CheckMode (when new copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(HowManyBroughtIntoUKPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyBroughtIntoUKPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(CheckMode)
    }
  }

  "Brought into UK from small producers" - {
    def navigateFromBroughtIntoUkFromSmallProducersPage(value: Boolean, mode: Mode) =
      navigator.nextPage(BroughtIntoUkFromSmallProducersPage, mode, emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUkFromSmallProducersPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How many brought into UK in $mode" in {
        val result = navigateFromBroughtIntoUkFromSmallProducersPage(value = true, mode)
        result mustBe routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(mode)
      }
    })

    "select No to navigate to brought into uk from small producers page in NormalMode" in {
      val result = navigateFromBroughtIntoUkFromSmallProducersPage(value = false, NormalMode)
      result mustBe routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    }

    "Should No to navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromBroughtIntoUkFromSmallProducersPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "How many brought into UK from small producers" - {
    def navigateFromHowManyBroughtIntoUkFromSmallProducersPage(mode: Mode) =
      navigator.nextPage(HowManyBroughtIntoUkFromSmallProducersPage, mode, emptyUserAnswersForCorrectReturn
        .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(1, 1)).success.value)

    "navigate to brought into uk from small producers page in NormalMode" in {
      val result = navigateFromHowManyBroughtIntoUkFromSmallProducersPage(NormalMode)
      result mustBe routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromHowManyBroughtIntoUkFromSmallProducersPage(CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Packaged as a contract packer" - {
    def navigateFromPackagedAsContractPackerPage(value: Boolean, mode: Mode) =
      navigator.nextPage(PackagedAsContractPackerPage, mode, emptyUserAnswersForCorrectReturn.set(PackagedAsContractPackerPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How Many packaged as contract packer in $mode" in {
        val result = navigateFromPackagedAsContractPackerPage(value = true, mode)
        result mustBe routes.HowManyPackagedAsContractPackerController.onPageLoad(mode)
      }
    })

    "select No to navigate to exemptions for small producers page in NormalMode" in {
      val result = navigateFromPackagedAsContractPackerPage(value = false, NormalMode)
      result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    }

    "Should navigate to Check Your Answers page when no is selected in CheckMode" in {
      val result = navigateFromPackagedAsContractPackerPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "How many packaged as contract packer" - {
    def navigateFromHowManyPackagedAsContractPackerPage(mode: Mode) =
      navigator.nextPage(HowManyPackagedAsContractPackerPage, mode, emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value)

    "navigate to exemptions for small producers page in NormalMode" in {
      val result = navigateFromHowManyPackagedAsContractPackerPage(NormalMode)
      result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode (when not new importer or new copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when already importer and already copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, subscription = currentImporterAndCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when currently importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, subscription = currentImporterSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when currently copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, None, currentCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Return Change Registration page in CheckMode (when new importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(CheckMode)
    }

    "navigate to Return Change Registration page in CheckMode (when new copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(HowManyPackagedAsContractPackerPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyPackagedAsContractPackerPage, CheckMode, userAnswers, subscription = Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(CheckMode)
    }
  }

  "Exemptions for small producers" - {

    def navigateFromExemptionsForSmallProducers(value: Boolean, mode: Mode, populateSmallProducerList: Boolean = false) = {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .copy(smallProducerList = if (populateSmallProducerList) smallProducerList else List.empty)
        .set(ExemptionsForSmallProducersPage, value).success.value
      navigator.nextPage(ExemptionsForSmallProducersPage, mode, userAnswers)
    }

    "select Yes to navigate to Add small producer page in NormalMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = true, NormalMode)
      result mustBe routes.AddASmallProducerController.onPageLoad(NormalMode)
    }

    "select No to navigate to brought into uk page in NormalMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = false, NormalMode)
      result mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode)
    }

    "select Yes to navigate to Small Producer Details page when small producer list is not  populated in NormalMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = true, NormalMode)
      result mustBe routes.AddASmallProducerController.onPageLoad(NormalMode)
    }

    "select Yes to navigate to Small Producer Details page when small producer list is populated in NormalMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = true, NormalMode, populateSmallProducerList = true)
      result mustBe routes.SmallProducerDetailsController.onPageLoad(NormalMode)
    }

    "select Yes to navigate to Small Producer Details page when small producer list populated in CheckMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = true, CheckMode, populateSmallProducerList = true)
      result mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    }

    "select Yes to navigate to Add small producer page when small producer list not populated in CheckMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = true, CheckMode)
      result mustBe routes.AddASmallProducerController.onPageLoad(CheckMode)
    }

    "select No to navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromExemptionsForSmallProducers(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

  }

  "Ask Secondary Warehouses In Return Page" - {
    List(NormalMode, CheckMode).foreach(mode => {
      s"must go from $AskSecondaryWarehouseInReturnPage to $CorrectReturnBaseCYAPage in $mode when user selects no" in {
        navigator.nextPage(AskSecondaryWarehouseInReturnPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)
          .set(AskSecondaryWarehouseInReturnPage, false).success.value) mustBe routes.CorrectReturnCYAController.onPageLoad
      }
    })
  }

  "Secondary Warehouse Details Page" - {
    List(NormalMode, CheckMode).foreach(mode => {
      s"must go from $SecondaryWarehouseDetailsPage to $CorrectReturnBaseCYAPage in $mode when user selects no" in {
        navigator.nextPage(SecondaryWarehouseDetailsPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)
          .set(SecondaryWarehouseDetailsPage, false).success.value) mustBe routes.CorrectReturnCYAController.onPageLoad
      }
    })
  }

  "Add A Small Producer " - {

    "Should navigate to small producer details controller when data is entered" in {
      navigator.nextPage(AddASmallProducerPage,
        CheckMode,
        emptyUserAnswersForCorrectReturn.copy(data = Json.obj("addASmallProducer" -> Json.obj("lowBand" -> "10000", "highBand" -> "20000")))
      ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    }
  }

  s"must go from $AddASmallProducerPage to $SmallProducerDetailsPage in EditMode" in {
    navigator.nextPage(AddASmallProducerPage, EditMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
      contactAddress)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(NormalMode)
  }

  List(NormalMode, CheckMode).foreach(mode => {
    s"must go from $AddASmallProducerPage to $SmallProducerDetailsPage in $mode" in {
      navigator.nextPage(AddASmallProducerPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
        contactAddress)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(mode)
    }


    s"must go from $RemoveSmallProducerConfirmPage to $ExemptionsForSmallProducersPage in $mode when zero small producers left" in {
      navigator.nextPage(RemoveSmallProducerConfirmPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)
        .set(RemoveSmallProducerConfirmPage, true).success.value) mustBe controllers.correctReturn.routes.ExemptionsForSmallProducersController.onPageLoad(mode)
    }

    s"must go from $RemoveSmallProducerConfirmPage to $SmallProducerDetailsPage in $mode when one or more small producers left" in {
      navigator.nextPage(RemoveSmallProducerConfirmPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
        contactAddress, smallProducerList = smallProducerList)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(mode)
    }
  })

  "Claim Credits for Exports " - {
    def navigateFromClaimCreditsForExportsPage(value: Boolean, mode: Mode) =
      navigator.nextPage(ClaimCreditsForExportsPage, mode, emptyUserAnswersForCorrectReturn.set(ClaimCreditsForExportsPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How many claim credits for exports in $mode" in {
        val result = navigateFromClaimCreditsForExportsPage(value = true, mode)
        result mustBe routes.HowManyClaimCreditsForExportsController.onPageLoad(mode)
      }
    })

    "select No to navigate to Claim credits for lost/damaged page in NormalMode" in {
      val result = navigateFromClaimCreditsForExportsPage(value = false, NormalMode)
      result mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    }

    "Should navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromClaimCreditsForExportsPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "How many claim credits for exports " - {
    def navigateFromHowManyClaimCreditsForExportsPage(mode: Mode) =
      navigator.nextPage(HowManyClaimCreditsForExportsPage, mode, emptyUserAnswersForCorrectReturn
        .set(HowManyClaimCreditsForExportsPage, LitresInBands(1, 1)).success.value)

    "navigate to claim credits for lost/damaged in NormalMode" in {
      val result = navigateFromHowManyClaimCreditsForExportsPage(NormalMode)
      result mustBe routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromHowManyClaimCreditsForExportsPage(CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Claim Credits for Lost Damaged " - {
    def navigateFromClaimCreditsForLostDamagedPage(value: Boolean, mode: Mode, subscription: Option[RetrievedSubscription] = None) =
      navigator.nextPage(ClaimCreditsForLostDamagedPage, mode,
        emptyUserAnswersForCorrectReturn.set(ClaimCreditsForLostDamagedPage, value).success.value, None, subscription)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How many claim credits for lost damaged in $mode" in {
        val subscription = if (mode == NormalMode) Some(aSubscription) else None
        val result = navigateFromClaimCreditsForLostDamagedPage(value = true, mode, subscription)
        result mustBe routes.HowManyCreditsForLostDamagedController.onPageLoad(mode)
      }
    })

    "select No to navigate to Check Your Answers page in NormalMode (when not new importer or new copacker)" in {
      val result = navigateFromClaimCreditsForLostDamagedPage(value = false, NormalMode, Some(aSubscription))
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when already importer and already copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(ClaimCreditsForLostDamagedPage, false).success.value
      val result = navigator.nextPage(ClaimCreditsForLostDamagedPage, NormalMode, userAnswers, subscription = currentImporterAndCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "select no to navigate to Check Your Answers page in NormalMode (when currently importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(ClaimCreditsForLostDamagedPage, false).success.value
      val result = navigator.nextPage(ClaimCreditsForLostDamagedPage, NormalMode, userAnswers, None, currentImporterSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "select no to navigate to Check Your Answers page in NormalMode (when currently copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(ClaimCreditsForLostDamagedPage, false).success.value
      val result = navigator.nextPage(ClaimCreditsForLostDamagedPage, NormalMode, userAnswers, None, currentCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "select no to navigate to Return Change Registration page in NormalMode (when new importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(ClaimCreditsForLostDamagedPage, false).success.value
      val result = navigator.nextPage(ClaimCreditsForLostDamagedPage, NormalMode, userAnswers, None, Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(NormalMode)
    }

    "select no to navigate to Return Change Registration page in NormalMode (when new copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(ClaimCreditsForLostDamagedPage, false).success.value
      val result = navigator.nextPage(ClaimCreditsForLostDamagedPage, NormalMode, userAnswers, None, Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(NormalMode)
    }

    "Should navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromClaimCreditsForLostDamagedPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "How many claim credits for lost damaged " - {
    def navigateFromHowManyClaimCreditsForLostDamagedPage(mode: Mode, subscription: Option[RetrievedSubscription] = None) =
      navigator.nextPage(HowManyCreditsForLostDamagedPage, mode,
        emptyUserAnswersForCorrectReturn.set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value, None, subscription)

    "navigate to Check Your Answers page in NormalMode (when not new importer or new copacker)" in {
      val result = navigateFromHowManyClaimCreditsForLostDamagedPage(NormalMode, Some(aSubscription))
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in CheckMode (when already importer and already copacker)" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, subscription = currentImporterAndCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in NormalMode (when currently importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, None, currentImporterSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in NormalMode (when currently copacker)" in {
      val userAnswers = newCoPackerUserAnswers
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, None, currentCoPackerSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Return Change Registration page in NormalMode (when new importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, None, Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(NormalMode)
    }

    "navigate to Return Change Registration page in NormalMode (when new copacker)" in {
      val userAnswers = newImporterUserAnswers
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(1, 1)).success.value
      val result = navigator.nextPage(HowManyCreditsForLostDamagedPage, NormalMode, userAnswers, None, Some(aSubscription))
      result mustBe routes.ReturnChangeRegistrationController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromHowManyClaimCreditsForLostDamagedPage(CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Return change registration page " - {
    def navigateFromReturnChangeRegistrationPageWhenNewImporter(mode: Mode, subscription: Option[RetrievedSubscription] = Some(aSubscription)) =
      navigator.nextPage(ReturnChangeRegistrationPage, mode, completedUserAnswersForCorrectReturnNewPackerOrImporter
        .set(PackagedAsContractPackerPage, false).success.value, None, subscription)

    def navigateFromReturnChangeRegistrationPageWhenNewCoPacker(mode: Mode, subscription: Option[RetrievedSubscription] = Some(aSubscription)) =
      navigator.nextPage(ReturnChangeRegistrationPage, mode, completedUserAnswersForCorrectReturnNewPackerOrImporter, None, subscription)

    "should navigate to Ask Secondary Warehouse in return when the user is a new importer with no warehouses in NormalMode" in {
      val result = navigateFromReturnChangeRegistrationPageWhenNewImporter(NormalMode)
      result mustBe routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode)
    }

    "should navigate to Pack At Business Address when the user is a new packer with no packaging sites in NormalMode" in {
      val result = navigateFromReturnChangeRegistrationPageWhenNewCoPacker(NormalMode)
      result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
    }

    def navigateFromReturnChangeRegistrationPageWhenNewImporterButUserHasSites(mode: Mode, subscription: Option[RetrievedSubscription] = Some(aSubscription)) =
      navigator.nextPage(ReturnChangeRegistrationPage, mode, completedUserAnswersForCorrectReturnNewPackerOrImporter
        .copy(warehouseList = twoWarehouses)
        .set(PackagedAsContractPackerPage, false).success.value, None, subscription)

    def navigateFromReturnChangeRegistrationPageWhenNewCoPackerButUserHasSites(mode: Mode, subscription: Option[RetrievedSubscription] = Some(aSubscription)) =
      navigator.nextPage(ReturnChangeRegistrationPage, mode, completedUserAnswersForCorrectReturnNewPackerOrImporter
        .copy(packagingSiteList = packingSiteMap)
        .set(BroughtIntoUKPage, false).success.value, None, subscription)

    "should navigate to Check your answers when the user is a new importer with existing warehouses in NormalMode" in {
      val result = navigateFromReturnChangeRegistrationPageWhenNewImporterButUserHasSites(NormalMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "should navigate to Check your answers when the user is a new packer with existing packaging sites in NormalMode" in {
      val result = navigateFromReturnChangeRegistrationPageWhenNewCoPackerButUserHasSites(NormalMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Packaging Site Details " - {
    def navigateFromPackagingSiteDetailsPage(mode: Mode, subscription: Option[RetrievedSubscription] = None) =
      navigator.nextPage(PackagingSiteDetailsPage, mode,
        emptyUserAnswersForCorrectReturn.set(PackagingSiteDetailsPage, false).success.value, None, subscription)

    "navigate to Check Your Answers page in NormalMode (when not new importer)" in {
      val result = navigateFromPackagingSiteDetailsPage(NormalMode, Some(aSubscription))
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Check Your Answers page in NormalMode (when currently importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(PackagingSiteDetailsPage, false).success.value
      val result = navigator.nextPage(PackagingSiteDetailsPage, NormalMode, userAnswers, None, currentImporterSubscription)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }

    "navigate to Ask Secondary Warehouse In Return page in NormalMode (when new importer)" in {
      val userAnswers = newImporterUserAnswers
        .set(PackagingSiteDetailsPage, false).success.value
      val result = navigator.nextPage(PackagingSiteDetailsPage, NormalMode, userAnswers, None, Some(aSubscription))
      result mustBe routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode)
    }

    "navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromPackagingSiteDetailsPage(CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Remove Packaging Site confirm " - {
    def navigateFromRemovePackagingSiteConfirm(value: Boolean, mode: Mode, lastPackagingSite: Boolean) = {
      navigator.nextPage(RemovePackagingSiteConfirmPage, mode,
        emptyUserAnswersForCorrectReturn
          .copy(packagingSiteList = if (lastPackagingSite) Map.empty else Map("000001" -> packingSite))
          .set(RemovePackagingSiteConfirmPage, value).success.value
      )
    }

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to Packaging Site Details in $mode when not last packaging site" in {
        val result = navigateFromRemovePackagingSiteConfirm(value = true, mode, lastPackagingSite = false)
        result mustBe routes.PackagingSiteDetailsController.onPageLoad(mode)
      }

      s"select Yes to navigate to Pack At Business Address in $mode when last packaging site" in {
        val result = navigateFromRemovePackagingSiteConfirm(value = true, mode, lastPackagingSite = true)
        result mustBe routes.PackAtBusinessAddressController.onPageLoad(mode)
      }

      s"select No to navigate to Packaging Site Details in $mode when not last packaging site" in {
        val result = navigateFromRemovePackagingSiteConfirm(value = false, mode, lastPackagingSite = false)
        result mustBe routes.PackagingSiteDetailsController.onPageLoad(mode)
      }

      s"select No to navigate to Packaging Site Details in $mode when last packaging site" in {
        val result = navigateFromRemovePackagingSiteConfirm(value = false, mode, lastPackagingSite = false)
        result mustBe routes.PackagingSiteDetailsController.onPageLoad(mode)
      }
    })
  }

  "Remove Warehouse confirm " - {
    def navigateFromRemoveWarehouseConfirm(value: Boolean, mode: Mode, lastWarehouse: Boolean) = {
      navigator.nextPage(RemoveWarehouseDetailsPage, mode,
        emptyUserAnswersForCorrectReturn
          .copy(warehouseList = if (lastWarehouse) Map.empty else twoWarehouses)
          .set(RemoveWarehouseDetailsPage, value).success.value
      )
    }

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to Secondary Warehouse Details in $mode when not last warehouse" in {
        val result = navigateFromRemoveWarehouseConfirm(value = true, mode, lastWarehouse= false)
        result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
      }

      s"select Yes to navigate to Ask Secondary Warehouse in $mode when last warehouse" in {
        val result = navigateFromRemoveWarehouseConfirm(value = true, mode, lastWarehouse = true)
        result mustBe routes.AskSecondaryWarehouseInReturnController.onPageLoad(mode)
      }

      s"select No to navigate to Secondary Warehouse Details in $mode when not last warehouse" in {
        val result = navigateFromRemoveWarehouseConfirm(value = false, mode, lastWarehouse = false)
        result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
      }

      s"select No to navigate to Secondary Warehouse Details in $mode when last warehouse" in {
        val result = navigateFromRemoveWarehouseConfirm(value = false, mode, lastWarehouse = false)
        result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
      }
    })
  }

}
