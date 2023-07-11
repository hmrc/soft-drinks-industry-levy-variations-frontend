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
import models.changeActivity.AmountProduced._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.changeActivity._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForChangeActivity @Inject()() extends Navigator {

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
        defaultCall
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
      case pageAnswers if pageAnswers.contains(None)  =>
        routes.ContractPackingController.onPageLoad(mode)
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => navigationForOperateThirdPartyPackagers(NormalMode)
    case PackAtBusinessAddressPage => _ => defaultCall
    case PackagingSiteDetailsPage => _ => defaultCall
    case RemovePackagingSiteDetailsPage => _ => defaultCall
    case SecondaryWarehouseDetailsPage => _ => defaultCall
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => userAnswers => navigationForHowManyContractPacking(userAnswers, NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => _ => defaultCall
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => defaultCall
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers, NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => navigationForOperateThirdPartyPackagers(CheckMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }
}
