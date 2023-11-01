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
import models.{CheckMode, EditMode, Mode, NormalMode, RetrievedSubscription, UserAnswers}
import pages.Page
import play.api.mvc.Call

trait Navigator {

  def defaultCall = routes.IndexController.onPageLoad

  val normalRoutes: Page => UserAnswers => Call

  val normalRoutesWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = _ => (_, _) => defaultCall

  val checkRouteMap: Page => UserAnswers => Call

  val checkRouteMapWithSubscription: Page => (UserAnswers, RetrievedSubscription) => Call = _ => (_, _) => defaultCall

  val editRouteMap: Page => UserAnswers => Call

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, subscription: Option[RetrievedSubscription] = None): Call = (mode, subscription) match {
    case (NormalMode, None) =>
      normalRoutes(page)(userAnswers)
    case (NormalMode, Some(subscription)) =>
      normalRoutesWithSubscription(page)(userAnswers, subscription)
    case (CheckMode, None) =>
      checkRouteMap(page)(userAnswers)
    case (CheckMode, Some(subscription)) =>
      checkRouteMapWithSubscription(page)(userAnswers, subscription)
    case (EditMode, _) =>
      editRouteMap(page)(userAnswers)
    case _ => sys.error("Mode should be Normal, Check or Edit")
  }

}
