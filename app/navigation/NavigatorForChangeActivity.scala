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
import models.changeActivity.AmountProduced._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.changeActivity._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForChangeActivity @Inject() extends Navigator {

  private def navigationForContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ContractPackingPage).contains(true)) {
      routes.HowManyContractPackingController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.ChangeActivityCYAController.onPageLoad
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

    private def navigationForHowManyContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if(mode == CheckMode){
      routes.ChangeActivityCYAController.onPageLoad
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ImportsPage).contains(true)) {
      routes.HowManyImportsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.ChangeActivityCYAController.onPageLoad
    } else {
      navigationFollowingImports(userAnswers)
    }
  }

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.ChangeActivityCYAController.onPageLoad
    } else {
        routes.ContractPackingController.onPageLoad(mode)
    }
  }

  private def navigationForOperateThirdPartyPackagers(mode: Mode): Call = {
   if(mode == CheckMode){
      routes.ChangeActivityCYAController.onPageLoad
    } else {
     routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
    }
  }

  private def navigationForAmountProduced (userAnswers: UserAnswers, mode: Mode): Call = {
    val pageAnswers = userAnswers.get(page = AmountProducedPage)
    pageAnswers match {
      case pageAnswers if pageAnswers.contains(Large)  =>
        routes.OperatePackagingSiteOwnBrandsController.onPageLoad(mode)
      case pageAnswers if pageAnswers.contains(Small)  =>
        routes.ThirdPartyPackagersController.onPageLoad(mode)
      case pageAnswers  =>
        routes.ContractPackingController.onPageLoad(mode)
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => navigationForOperateThirdPartyPackagers(NormalMode)
    case PackAtBusinessAddressPage => _ => defaultCall
    case PackagingSiteDetailsPage => _ => defaultCall
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => userAnswers => navigationForHowManyContractPacking(userAnswers, NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => navigationFollowingImports(userAnswers)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers, NormalMode)
    case SuggestDeregistrationPage => _ => controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => navigationForOperateThirdPartyPackagers(CheckMode)
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(CheckMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case RemoveWarehouseDetailsPage => _ => routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }

  private def navigationFollowingImports(userAnswers: UserAnswers): Call = {
    userAnswers.get(AmountProducedPage) match {
      case Some(AmountProduced.Large) => navigateForLargeAmountProducedFollowingImports(userAnswers)
      case Some(AmountProduced.None) => navigateForAmountProducedNoneFollowingImports(userAnswers)
      case Some(AmountProduced.Small) => defaultCall
      case _ => routes.AmountProducedController.onPageLoad(NormalMode)
    }
  }

  private def navigateForAmountProducedNoneFollowingImports(userAnswers: UserAnswers): Call =
    userAnswers.get(ContractPackingPage) match {
      case Some(true) if userAnswers.packagingSiteList.isEmpty =>
        routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
      case Some(true) =>
        routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
      case Some(false) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case _ => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
    }

  private def navigateForLargeAmountProducedFollowingImports(userAnswers: UserAnswers): Call =
    (userAnswers.get(OperatePackagingSiteOwnBrandsPage), userAnswers.get(ContractPackingPage)) match {
      case (Some(opsob), Some(cp)) if (opsob || cp) && userAnswers.packagingSiteList.isEmpty =>
        routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
      case (Some(opsob), Some(cp)) if opsob || cp =>
        routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
      case (Some(_), Some(_)) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case (Some(_), _) => routes.ContractPackingController.onPageLoad(NormalMode)
      case _ => routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
    }
}
