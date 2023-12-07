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

  private def navigationForAmountProducedCheckMode(userAnswers: UserAnswers, previousAnswer: AmountProduced)= {
    if (userAnswers.get(page = AmountProducedPage).contains(previousAnswer)) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      navigationForAmountProduced(userAnswers)
    }
  }

  private def navigationForAmountProduced(userAnswers: UserAnswers): Call = {

    val pageAnswers = userAnswers.get(page = AmountProducedPage)

    pageAnswers match {
      case pageAnswers if pageAnswers.contains(AmountProduced.Large) =>
        routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
      case pageAnswers if pageAnswers.contains(AmountProduced.Small) =>
        routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
      case _ =>
        routes.ContractPackingController.onPageLoad(NormalMode)
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
    } else if(mode == CheckMode) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ImportsPage).contains(true)) {
      routes.HowManyImportsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      navigationFollowingImports(userAnswers, mode)
    }
  }

  private def navigationFollowingImports(userAnswers: UserAnswers, mode: Mode): Call = {
    (userAnswers.get(AmountProducedPage), mode) match {
      case (Some(AmountProduced.Large), mode) => navigateFollowingImportsForAmountProducedLarge(userAnswers, mode)
      case (Some(AmountProduced.Small), mode) => navigateFollowingImportsForAmountProducedSmall(userAnswers, mode)
      case (Some(AmountProduced.None), mode) => navigateFollowingImportsForAmountProducedNone(userAnswers, mode)
      case _ => routes.AmountProducedController.onPageLoad(NormalMode)
    }
  }

  private def navigateFollowingImportsForAmountProducedLarge(userAnswers: UserAnswers, mode: Mode): Call = {
    val operateOwnBrands = userAnswers.get(OperatePackagingSiteOwnBrandsPage)
    val coPacker = userAnswers.get(ContractPackingPage)
    (operateOwnBrands, coPacker, mode) match {
      case (None, _, NormalMode) => routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
      case (_, None, NormalMode) => routes.ContractPackingController.onPageLoad(NormalMode)
      case (Some(false), Some(false), NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case (Some(_), Some(_), NormalMode) if userAnswers.packagingSiteList.isEmpty => routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
      case (Some(_), Some(_), NormalMode) => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
      case (_, _, CheckMode) => routes.ChangeActivityCYAController.onPageLoad
    }
  }

  private def navigateFollowingImportsForAmountProducedSmall(userAnswers: UserAnswers, mode: Mode): Call = {
    (userAnswers.get(ContractPackingPage), userAnswers.get(SecondaryWarehouseDetailsPage), mode) match {
      case (Some(true), _, NormalMode) if userAnswers.packagingSiteList.isEmpty => routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
      case (Some(true), _, NormalMode) => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
      case (Some(false), _, NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case (None, _, NormalMode) => routes.ContractPackingController.onPageLoad(NormalMode)
      case (_, Some(_), CheckMode) => routes.ChangeActivityCYAController.onPageLoad
      case (_, None, CheckMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
    }
  }

  private def navigateFollowingImportsForAmountProducedNone(userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(ContractPackingPage), userAnswers.get(SecondaryWarehouseDetailsPage), mode) match {
      case (Some(true), _, NormalMode) if userAnswers.packagingSiteList.isEmpty => routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
      case (Some(true), _, NormalMode) => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
      case (Some(false), _, NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case (None, _, NormalMode) => routes.ContractPackingController.onPageLoad(NormalMode)
      case (_, Some(_), CheckMode) => routes.ChangeActivityCYAController.onPageLoad
      case (_, _, mode) => routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
    }

  override val normalRoutes: Page => UserAnswers => Call = {
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers)
    case ThirdPartyPackagersPage => _ => routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => _ => routes.ImportsController.onPageLoad(NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => navigationFollowingImports(userAnswers, NormalMode)
    case SuggestDeregistrationPage => _ => controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)
    case PackAtBusinessAddressPage => _ => defaultCall
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case HowManyContractPackingPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case HowManyImportsPage => userAnswers => navigationFollowingImports(userAnswers, CheckMode)
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(CheckMode)
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }

  override val normalRoutesWithAmountProduced: Page => (UserAnswers, AmountProduced) => Call = {
    case AmountProducedPage => (userAnswers, previousAnswer) =>  navigationForAmountProduced(userAnswers)
  }

  override val checkRouteMapWithAmountProduced: Page => (UserAnswers, AmountProduced) => Call = {
    case AmountProducedPage => (userAnswers, previousAnswer) =>  navigationForAmountProducedCheckMode(userAnswers, previousAnswer)
  }

  override val editRouteMap: Page => UserAnswers => Call = {
    case _ => _ => defaultCall
  }
}