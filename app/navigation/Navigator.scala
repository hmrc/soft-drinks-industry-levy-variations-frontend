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

import controllers.routes
import models.backend.RetrievedSubscription
import models.changeActivity.AmountProduced
import models.{CheckMode, EditMode, Mode, NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

trait Navigator {

  def defaultCall = routes.SelectChangeController.onPageLoad

  val normalRoutes: Page => UserAnswers => Call

  val normalRoutesWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = _ => (_, _) => defaultCall

  val normalRoutesWithAmountProduced: Page => (UserAnswers, AmountProduced) => Call = _ => (_, _) => defaultCall

  val checkRouteMap: Page => UserAnswers => Call

  val checkRouteMapWithAmountProduced: Page => (UserAnswers, AmountProduced ) => Call = _ => (_, _) => defaultCall

  val checkRouteMapWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = _ => (_, _) => defaultCall

  val editRouteMap: Page => UserAnswers => Call

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers,
               amountProduced: Option[AmountProduced] = None,
               subscription: Option[RetrievedSubscription] = None): Call =
    (mode, subscription, amountProduced) match {
      case (NormalMode, None, None) =>
        normalRoutes(page)(userAnswers)

      case (NormalMode, Some(subscription), None) =>
        normalRoutesWithSubscription(page)(userAnswers, subscription)

      case (NormalMode, None, Some(amountProduced)) =>
        normalRoutesWithAmountProduced(page)(userAnswers, amountProduced)

      case (CheckMode, None, None) =>
        checkRouteMap(page)(userAnswers)

      case (CheckMode, None, Some(amountProduced)) =>
        checkRouteMapWithAmountProduced(page)(userAnswers, amountProduced)

      case (CheckMode, Some(subscription), None) =>
        checkRouteMapWithSubscription(page)(userAnswers, subscription)

      case (EditMode, _, _) =>
        editRouteMap(page)(userAnswers)

      case _ => sys.error("Mode should be Normal, Check or Edit")
  }

}
