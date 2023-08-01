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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
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
      defaultCall
    }
  }
  private def navigationForClaimCreditsForExports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ClaimCreditsForExportsPage).contains(true)) {
      routes.HowManyClaimCreditsForExportsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CorrectReturnCYAController.onPageLoad
    } else {
        defaultCall
    }
  }

  private def navigationForBroughtIntoUK(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = BroughtIntoUKPage).contains(true)) {
      routes.HowManyBroughtIntoUKController.onPageLoad(mode)
    } else if(mode == CheckMode){
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      defaultCall
    }
  }

  private def navigationForPackagedAsContractPacker(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = PackagedAsContractPackerPage).contains(true)) {
      routes.HowManyPackagedAsContractPackerController.onPageLoad(mode)
    } else if(mode == CheckMode){
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      defaultCall
    }
  }

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      controllers.routes.IndexController.onPageLoad
    }
  }

  private def navigationForCreditsForLostDamaged(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
      routes.HowManyCreditsForLostDamagedController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      controllers.routes.IndexController.onPageLoad
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {

    case RemovePackagingSiteConfirmPage => _ => defaultCall
    case SecondaryWarehouseDetailsPage => _ => defaultCall
    case SmallProducerDetailsPage => _ => defaultCall
    case PackagingSiteDetailsPage => _ => defaultCall
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, NormalMode)
    case HowManyBroughtIntoUkFromSmallProducersPage => _ => defaultCall
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, NormalMode)
    case HowManyClaimCreditsForExportsPage => _ => defaultCall
    case ReturnChangeRegistrationPage => _ => defaultCall
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, NormalMode)
    case HowManyBroughtIntoUKPage => _ => defaultCall
    case ExemptionsForSmallProducersPage => _ => defaultCall
    case RemoveSmallProducerConfirmPage => _ => defaultCall
    case RemoveWarehouseDetailsPage => userAnswers => defaultCall
    case CorrectionReasonPage => _ => defaultCall
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => defaultCall
    case ClaimCreditsForLostDamagedPage => userAnswers => navigationForCreditsForLostDamaged(userAnswers, NormalMode)
    case HowManyCreditsForLostDamagedPage => userAnswers => defaultCall
    case RepaymentMethodPage => userAnswers => defaultCall
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, NormalMode)
    case HowManyPackagedAsContractPackerPage => _ => defaultCall
    case AddASmallProducerPage => userAnswers => defaultCall
    case SelectPage => userAnswers => defaultCall
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case RemovePackagingSiteConfirmPage => _ => defaultCall
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, CheckMode)
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, CheckMode)
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, CheckMode)
    case ExemptionsForSmallProducersPage => _ =>  routes.CorrectReturnCYAController.onPageLoad
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case ClaimCreditsForLostDamagedPage => userAnswers => navigationForCreditsForLostDamaged(userAnswers, CheckMode)
    case _ => _ => routes.CorrectReturnCYAController.onPageLoad
  }
}
