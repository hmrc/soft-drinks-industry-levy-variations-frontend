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

import controllers.correctReturn.routes
import models.backend.RetrievedSubscription
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages._
import pages.correctReturn._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForCorrectReturn @Inject()() extends Navigator {

  private def navigationForBroughtIntoUkFromSmallProducers(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = BroughtIntoUkFromSmallProducersPage).contains(true)) {
      routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ClaimCreditsForExportsController.onPageLoad(mode)
    }
  }

  private def navigationForClaimCreditsForExports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ClaimCreditsForExportsPage).contains(true)) {
      routes.HowManyClaimCreditsForExportsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ClaimCreditsForLostDamagedController.onPageLoad(mode)
    }
  }

  private def navigationForBroughtIntoUK(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = BroughtIntoUKPage).contains(true)) {
      routes.HowManyBroughtIntoUKController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUkFromSmallProducersController.onPageLoad(mode)
    }
  }

  private def navigationForExemptionsForSmallProducers(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ExemptionsForSmallProducersPage).contains(true)) {
      (mode, userAnswers.smallProducerList.nonEmpty) match {
        case (CheckMode, true) => routes.SmallProducerDetailsController.onPageLoad(mode)
        case _ => routes.AddASmallProducerController.onPageLoad(mode)
      }
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUKController.onPageLoad(mode)
    }
  }

  private def navigationForPackagedAsContractPacker(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = PackagedAsContractPackerPage).contains(true)) {
      routes.HowManyPackagedAsContractPackerController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ExemptionsForSmallProducersController.onPageLoad(mode)
    }
  }

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.PackagedAsContractPackerController.onPageLoad(mode)
    }
  }

  private def navigationForRemovePackagingSiteConfirm(userAnswers: UserAnswers, mode: Mode) = {
    if (userAnswers.get(page = RemovePackagingSiteConfirmPage).contains(true) && userAnswers.packagingSiteList.isEmpty) {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    } else {
      routes.PackagingSiteDetailsController.onPageLoad(mode)
    }
  }

  private def navigationForAddASmallProducer(mode: Mode): Call = {
    routes.SmallProducerDetailsController.onPageLoad(mode)
  }

  private def navigationForSmallProducerDetails(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = SmallProducerDetailsPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUKController.onPageLoad(mode)
    }
  }

  private def navigationForRemoveSmallProducerConfirm(userAnswers: UserAnswers, mode: Mode):Call = {
    if (userAnswers.smallProducerList.isEmpty) {
      routes.ExemptionsForSmallProducersController.onPageLoad(mode)
    } else {
      routes.SmallProducerDetailsController.onPageLoad(mode)
    }
  }

  private def navigationForCreditsForLostDamagedInNormalMode(userAnswers: UserAnswers, subscription: RetrievedSubscription) = {
    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
    } else {
      navigationToReturnChangeRegistrationIfRequired(userAnswers, subscription, NormalMode)
    }
  }

  private def navigationForCreditsForLostDamagedInCheckMode(userAnswers: UserAnswers) = {
    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(CheckMode)
    } else {
      routes.CorrectReturnCYAController.onPageLoad
    }
  }

  private def isANewPacker(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    val alreadyAPacker = subscription.activity.contractPacker
    val yesOnCoPacker = userAnswers.get(PackagedAsContractPackerPage).contains(true)
    !alreadyAPacker && yesOnCoPacker
  }

  private def isANewImporter(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    val alreadyAnImporter = subscription.activity.importer
    val yesOnImporter = userAnswers.get(BroughtIntoUKPage).contains(true)
    !alreadyAnImporter && yesOnImporter
  }

  private def navigationForPackagingSiteDetailsInNormalMode(userAnswers: UserAnswers, subscription: RetrievedSubscription) = {
    if (subscription.activity.importer) {
      routes.CorrectReturnCYAController.onPageLoad
    } else if (isANewImporter(userAnswers, subscription)) {
      routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode)
    } else {
      routes.CorrectReturnCYAController.onPageLoad
    }
  }

  private def navigationToReturnChangeRegistrationIfRequired(userAnswers: UserAnswers, subscription: RetrievedSubscription, mode: Mode) = {
    val alreadyAPacker = subscription.activity.contractPacker
    val alreadyAnImporter = subscription.activity.importer
    if (alreadyAPacker && alreadyAnImporter) {
      routes.CorrectReturnCYAController.onPageLoad
    } else if (isANewPacker(userAnswers, subscription) || isANewImporter(userAnswers, subscription)) {
      routes.ReturnChangeRegistrationController.onPageLoad(mode)
    } else {
      routes.CorrectReturnCYAController.onPageLoad
    }
  }

  private def navigationFromReturnChangeRegistration(userAnswers: UserAnswers, subscription: RetrievedSubscription, mode: Mode): Call = {
    val doesNotHavePackagingSites = userAnswers.packagingSiteList.isEmpty
    val doesNotHaveWarehouses = userAnswers.warehouseList.isEmpty
    if (isANewPacker(userAnswers, subscription) && doesNotHavePackagingSites) {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    } else if (isANewImporter(userAnswers, subscription) && doesNotHaveWarehouses) {
      routes.AskSecondaryWarehouseInReturnController.onPageLoad(mode)
    } else {
      routes.CorrectReturnCYAController.onPageLoad
    }
  }

  private def navigationForCorrectionReason(mode: Mode): Call = {
    if (mode == NormalMode) {
      routes.RepaymentMethodController.onPageLoad(mode)
    } else {
      routes.CorrectReturnCheckChangesCYAController.onPageLoad
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case RemovePackagingSiteConfirmPage => userAnswers => navigationForRemovePackagingSiteConfirm(userAnswers, NormalMode)
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, NormalMode)
    case HowManyBroughtIntoUKPage => _ => routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, NormalMode)
    case HowManyBroughtIntoUkFromSmallProducersPage => _ => routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, NormalMode)
    case HowManyClaimCreditsForExportsPage => _ => routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    case ExemptionsForSmallProducersPage => userAnswers => navigationForExemptionsForSmallProducers(userAnswers, NormalMode)
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(NormalMode)
    case SmallProducerDetailsPage => userAnswers => navigationForSmallProducerDetails(userAnswers, NormalMode)
    case AskSecondaryWarehouseInReturnPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case SecondaryWarehouseDetailsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
    case CorrectionReasonPage => _ => navigationForCorrectionReason(NormalMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => routes.PackagedAsContractPackerController.onPageLoad(NormalMode)
    case RepaymentMethodPage => userAnswers => routes.CorrectReturnCheckChangesCYAController.onPageLoad
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, NormalMode)
    case HowManyPackagedAsContractPackerPage => _ => routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    case RemoveSmallProducerConfirmPage => userAnswers => navigationForRemoveSmallProducerConfirm(userAnswers, NormalMode)
    case _ => _ => defaultCall
  }

  override val normalRoutesWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = {
    case ClaimCreditsForLostDamagedPage => (userAnswers, subscription) => navigationForCreditsForLostDamagedInNormalMode(userAnswers, subscription)
    case HowManyCreditsForLostDamagedPage => (userAnswers, subscription) =>
      navigationToReturnChangeRegistrationIfRequired(userAnswers, subscription, NormalMode)
    case PackagingSiteDetailsPage => (userAnswers, subscription) => navigationForPackagingSiteDetailsInNormalMode(userAnswers, subscription)
    case ReturnChangeRegistrationPage => (userAnswers, subscription) => navigationFromReturnChangeRegistration(userAnswers, subscription, NormalMode)
    case _ => (_, _) => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, CheckMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, CheckMode)
    case HowManyBroughtIntoUkFromSmallProducersPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, CheckMode)
    case HowManyClaimCreditsForExportsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case ExemptionsForSmallProducersPage => userAnswers => navigationForExemptionsForSmallProducers(userAnswers, CheckMode)
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(CheckMode)
    case SmallProducerDetailsPage => userAnswers => navigationForSmallProducerDetails(userAnswers, CheckMode)
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case ClaimCreditsForLostDamagedPage => userAnswers => navigationForCreditsForLostDamagedInCheckMode(userAnswers)
    case HowManyCreditsForLostDamagedPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case PackagingSiteDetailsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case RemovePackagingSiteConfirmPage => userAnswers => navigationForRemovePackagingSiteConfirm(userAnswers, CheckMode)
    case RemoveSmallProducerConfirmPage => userAnswers => navigationForRemoveSmallProducerConfirm(userAnswers, CheckMode)
    case AskSecondaryWarehouseInReturnPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case SecondaryWarehouseDetailsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
    case CorrectionReasonPage => _ => navigationForCorrectionReason(CheckMode)
    case RepaymentMethodPage => _ => routes.CorrectReturnCheckChangesCYAController.onPageLoad
    case _ => _ => routes.CorrectReturnCYAController.onPageLoad
  }

  override val checkRouteMapWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = {
    case HowManyBroughtIntoUKPage => (userAnswers, subscription) => navigationToReturnChangeRegistrationIfRequired(userAnswers, subscription, CheckMode)
    case HowManyPackagedAsContractPackerPage => (userAnswers, subscription) =>
      navigationToReturnChangeRegistrationIfRequired(userAnswers, subscription, CheckMode)
    case _ => (_, _) => defaultCall
  }

  override val editRouteMap: Page => UserAnswers => Call = {
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(NormalMode)
    case _ => _ => defaultCall
  }
}
