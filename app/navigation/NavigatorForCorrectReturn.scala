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
import pages.correctReturn.{HowManyOperatePackagingSiteOwnBrandsPage, CorrectionReasonPage, OperatePackagingSiteOwnBrandsPage, RepaymentMethodPage, SelectPage}

import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForCorrectReturn @Inject()() extends Navigator {

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      controllers.routes.IndexController.onPageLoad
    }
  }

  override val normalRoutes: Page => UserAnswers => Call = {

    case CorrectionReasonPage => _ => defaultCall
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => defaultCall
    case RepaymentMethodPage => userAnswers => defaultCall
    case SelectPage => userAnswers => defaultCall
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => defaultCall
  }
}
