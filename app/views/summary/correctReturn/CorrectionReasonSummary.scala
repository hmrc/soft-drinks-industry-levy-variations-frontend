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
import models.{CheckMode, UserAnswers}
import pages.correctReturn.CorrectionReasonPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CorrectionReasonSummary  {

  def row(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CorrectionReasonPage).map {
      answer =>
        SummaryListRowViewModel(
          key     = "correctReturn.correctionReason.checkYourAnswersLabel",
          value   = ValueViewModel(answer),
          actions = if (!isCheckAnswers) Seq.empty else {
            Seq(
              ActionItemViewModel("site.change", routes.CorrectionReasonController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("correctReturn.correctionReason.change.hidden"))
                .withAttribute(("id", "change-correctionReason"))
            )
          }
        )
    }
}
