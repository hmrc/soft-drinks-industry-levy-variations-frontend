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

package views.summary.correctReturn

import controllers.correctReturn.routes
import models.{ CheckMode, LitresInBands }
import pages.QuestionPage
import pages.correctReturn.{ BroughtIntoUKPage, HowManyBroughtIntoUKPage }
import views.summary.{ ReturnDetailsSummaryListWithLitres, SummaryListRowLitresHelper }

object BroughtIntoUKSummary extends ReturnDetailsSummaryListWithLitres {

  override val page: QuestionPage[Boolean] = BroughtIntoUKPage
  override val optLitresPage: Option[QuestionPage[LitresInBands]] = Some(HowManyBroughtIntoUKPage)
  override val summaryLitres: SummaryListRowLitresHelper = HowManyBroughtIntoUKSummary
  override val key: String = "correctReturn.broughtIntoUK.checkYourAnswersLabel"
  override val action: String = routes.BroughtIntoUKController.onPageLoad(CheckMode).url
  override val actionId: String = "change-broughtIntoUK"
  override val hiddenText: String = "correctReturn.broughtIntoUK"

}
