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

package pages.changeActivity

import controllers.changeActivity.routes
import models.backend.RetrievedSubscription
import models.changeActivity.AmountProduced
import play.api.libs.json.JsPath
import models.{LitresInBands, Mode, UserAnswers}
import pages.{QuestionPage, RequiredPage}

case object HowManyImportsPage extends QuestionPage[LitresInBands] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "changeActivity"
  override def toString: String = "howManyImports"

  override val url: Mode => String = mode => routes.HowManyImportsController.onPageLoad(mode).url

  override val previousPagesRequired: (UserAnswers, RetrievedSubscription) => List[RequiredPage] = (userAnswers, _) =>
    List(
      RequiredPage(AmountProducedPage),
      RequiredPage(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.getChangeActivityData.exists(_.isSmall))),
      RequiredPage(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.getChangeActivityData.exists(_.isLargeOrSmall))),
      RequiredPage(ContractPackingPage),
      RequiredPage(ImportsPage)
    )
}
