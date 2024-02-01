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

package views.summary.cancelRegistration

import controllers.cancelRegistration.routes
import models.{CheckMode, UserAnswers}
import pages.cancelRegistration.ReasonPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReasonSummary  {

  def row(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): SummaryListRow =
    answers.get(ReasonPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "cancelRegistration.reason.checkYourAnswersLabel",
          value   = ValueViewModel(answer),
          actions = if (isCheckAnswers) {
            Seq(
              ActionItemViewModel("site.change", routes.ReasonController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("cancelRegistration.reason.change.hidden"))
            )
          } else Seq.empty
        )
    }.getOrElse(throw new IllegalArgumentException(s"No reason given"))
}
