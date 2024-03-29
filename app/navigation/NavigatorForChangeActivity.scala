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

import controllers.changeActivity.routes
import models.changeActivity.AmountProduced
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.changeActivity._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForChangeActivity @Inject() extends Navigator {

  private def suggestDeregistration(userAnswers: UserAnswers) = {
    val notThirdPartyPackagers = userAnswers.get(ThirdPartyPackagersPage).contains(false)
    val notContractPacking = userAnswers.get(ContractPackingPage).contains(false)
    val notImporter = userAnswers.get(ImportsPage).contains(false)
    userAnswers.get(AmountProducedPage) match {
      case Some(AmountProduced.Small) => notThirdPartyPackagers && notContractPacking && notImporter
      case Some(AmountProduced.None) => notContractPacking && notImporter
      case _ => false
    }
  }

  private def navigationForAmountProducedCheckMode(userAnswers: UserAnswers, previousAnswer: AmountProduced) = {
    if (userAnswers.get(page = AmountProducedPage).contains(previousAnswer)) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      navigationForAmountProduced(userAnswers)
    }
  }

  private def navigationForAmountProduced(userAnswers: UserAnswers): Call = {
    userAnswers.get(page = AmountProducedPage) match {
      case Some(AmountProduced.Large) =>
        routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
      case Some(AmountProduced.Small) =>
        routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
      case Some(AmountProduced.None) =>
        routes.ContractPackingController.onPageLoad(NormalMode)
      case _ =>
        routes.AmountProducedController.onPageLoad(NormalMode)
    }
  }

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      routes.ContractPackingController.onPageLoad(mode)
    }
  }

  private def navigationForContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ContractPackingPage).contains(true)) {
      routes.HowManyContractPackingController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      if (suggestDeregistration(userAnswers)) {
        routes.SuggestDeregistrationController.onPageLoad()
      } else {
        routes.ChangeActivityCYAController.onPageLoad
      }
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

  private def navigationForHowManyContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (mode == CheckMode && userAnswers.get(PackagingSiteDetailsPage).isEmpty) {
      routes.PackagingSiteDetailsController.onPageLoad(mode)
    } else {
      routes.ChangeActivityCYAController.onPageLoad
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ImportsPage).contains(true)) {
      routes.HowManyImportsController.onPageLoad(mode)
    } else {
      navigationFollowingImports(userAnswers, mode)
    }
  }

  private def navigationFollowingImports(userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(AmountProducedPage) match {
      case Some(AmountProduced.Large) => navigateFollowingImportsForAmountProducedLarge(userAnswers, mode)
      case Some(AmountProduced.Small) => navigateFollowingImportsForAmountProducedSmall(userAnswers, mode)
      case Some(AmountProduced.None) => navigateFollowingImportsForAmountProducedNone(userAnswers, mode)
      case _ => routes.AmountProducedController.onPageLoad(NormalMode)
    }
  }

  private def routeToPackagingSiteJourney(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.packagingSiteList.isEmpty) {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    } else {
      routes.PackagingSiteDetailsController.onPageLoad(mode)
    }
  }

  private def navigateFollowingImportsForAmountProducedLarge(userAnswers: UserAnswers, mode: Mode): Call = {
    val operateOwnBrands = userAnswers.get(OperatePackagingSiteOwnBrandsPage)
    val coPacker = userAnswers.get(ContractPackingPage)
    (operateOwnBrands, coPacker, mode) match {
      case (Some(false), Some(false), NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case (Some(_), Some(_), NormalMode) => routeToPackagingSiteJourney(userAnswers, NormalMode)
      case _ => routes.ChangeActivityCYAController.onPageLoad
    }
  }

  private def navigateFollowingImportsForAmountProducedSmall(userAnswers: UserAnswers, mode: Mode): Call = {
    if (suggestDeregistration(userAnswers)) {
      routes.SuggestDeregistrationController.onPageLoad()
    } else if (userAnswers.getChangeActivityData.exists(_.isVoluntary)) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      (userAnswers.get(ContractPackingPage), userAnswers.get(ImportsPage), mode) match {
        case (Some(true), Some(_), NormalMode) => routeToPackagingSiteJourney(userAnswers, NormalMode)
        case (Some(false), Some(true), NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
        case (Some(_), Some(true), CheckMode) if userAnswers.get(SecondaryWarehouseDetailsPage).isEmpty =>
          routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
        case _ => routes.ChangeActivityCYAController.onPageLoad
      }
    }
  }

  private def navigateFollowingImportsForAmountProducedNone(userAnswers: UserAnswers, mode: Mode): Call = {
    if (suggestDeregistration(userAnswers)) {
      routes.SuggestDeregistrationController.onPageLoad()
    } else {
      (userAnswers.get(ContractPackingPage), userAnswers.get(ImportsPage), mode) match {
        case (Some(true), Some(_), NormalMode) => routeToPackagingSiteJourney(userAnswers, NormalMode)
        case (Some(false), Some(true), NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
        case (Some(_), Some(true), CheckMode) if userAnswers.get(SecondaryWarehouseDetailsPage).isEmpty =>
          routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
        case _ => routes.ChangeActivityCYAController.onPageLoad
      }
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case AmountProducedPage => userAnswers =>  navigationForAmountProduced(userAnswers)
    case ThirdPartyPackagersPage => _ => routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => _ => routes.ImportsController.onPageLoad(NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => navigationFollowingImports(userAnswers, NormalMode)
    case RemovePackagingSiteDetailsPage => userAnswers => routeToPackagingSiteJourney(userAnswers, NormalMode)
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case HowManyContractPackingPage => userAnswers => navigationForHowManyContractPacking(userAnswers, CheckMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case HowManyImportsPage => userAnswers => navigationFollowingImports(userAnswers, CheckMode)
    case RemovePackagingSiteDetailsPage => userAnswers => routeToPackagingSiteJourney(userAnswers, CheckMode)
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }

  override val normalRoutesWithAmountProduced: Page => (UserAnswers, AmountProduced) => Call = {
    case AmountProducedPage => (userAnswers, _) =>  navigationForAmountProduced(userAnswers)
    case _ => (_, _) => defaultCall
  }

  override val checkRouteMapWithAmountProduced: Page => (UserAnswers, AmountProduced) => Call = {
    case AmountProducedPage => (userAnswers, previousAnswer) =>  navigationForAmountProducedCheckMode(userAnswers, previousAnswer)
    case _ => (_, _) => defaultCall
  }

  override val editRouteMap: Page => UserAnswers => Call = _ => _ => defaultCall
}