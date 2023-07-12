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
import viewmodels.summary.changeActivity.AmountProducedSummary
import play.api.i18n.Messages
import play.api.mvc.{Call, RequestHeader}
import services.{AddressLookupService, PackingDetails}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

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
    val contractPacker = userAnswers.get(ContractPackingPage).contains(true)
    val noneProduced = userAnswers.get(AmountProducedPage).contains(AmountProduced.None)
    val imports = userAnswers.get(page = ImportsPage).contains(true)

    (noneProduced, contractPacker, imports, mode) match {
      case (_, _, true, NormalMode) => routes.HowManyImportsController.onPageLoad(mode)
      case (true, true, false, NormalMode) => routes.PackagingSiteDetailsController.onPageLoad(mode)
      case (_, _, true, CheckMode) => routes.HowManyImportsController.onPageLoad(mode)
      case (_, _, false, CheckMode) => routes.ChangeActivityCYAController.onPageLoad
      case _ => defaultCall
    }
  }
  private def navigationForHowManyImports(userAnswers: UserAnswers, mode: Mode): Call = {
    val contractPacker = userAnswers.get(ContractPackingPage).contains(true)
    val noneProduced = userAnswers.get(AmountProducedPage).contains(AmountProduced.None)

    (contractPacker, noneProduced, mode) match {
      case (true, true, _) => routes.PackagingSiteDetailsController.onPageLoad(mode)
      case (false, true, NormalMode) => routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
      case _ => routes.ChangeActivityCYAController.onPageLoad
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
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    case SecondaryWarehouseDetailsPage => _ => defaultCall
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => userAnswers => navigationForHowManyContractPacking(userAnswers, NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => navigationForHowManyImports(userAnswers, NormalMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => defaultCall
    case AmountProducedPage => userAnswers => navigationForAmountProduced(userAnswers, NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case ThirdPartyPackagersPage => _ => navigationForOperateThirdPartyPackagers(CheckMode)
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(CheckMode)
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, CheckMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, CheckMode)
    case HowManyImportsPage => userAnswers => navigationForHowManyImports(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => _ => routes.ChangeActivityCYAController.onPageLoad
    case _ => _ => routes.ChangeActivityCYAController.onPageLoad
  }
}
