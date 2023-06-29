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
        defaultCall
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
        defaultCall
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case PackAtBusinessAddressPage => userAnswers => defaultCall
    case PackagingSiteDetailsPage => userAnswers => defaultCall
    case RemovePackagingSiteDetailsPage => userAnswers => defaultCall
    case SecondaryWarehouseDetailsPage => userAnswers => defaultCall
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => userAnswers => defaultCall
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => defaultCall
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => defaultCall
    case AmountProducedPage => userAnswers => defaultCall
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }
}
