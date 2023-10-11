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

package views.summary.changeActivity

import controllers.changeActivity.routes
import models.{CheckMode, UserAnswers}
import pages.changeActivity.ThirdPartyPackagersPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ThirdPartyPackagersSummary {

  def row(answers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ThirdPartyPackagersPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "changeActivity.thirdPartyPackagers.checkYourAnswersLabel",
          value   = ValueViewModel(value).withCssClass("govuk-!-text-align-right"),
          actions = if(isCheckAnswers) {
            Seq(
              ActionItemViewModel("site.change", routes.ThirdPartyPackagersController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("changeActivity.thirdPartyPackagers.change.hidden"))
            )
          }else{Seq.empty}
        )
    }
}
