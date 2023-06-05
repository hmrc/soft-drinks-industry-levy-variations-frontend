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

package viewmodels.summary.updateRegisteredDetails

import controllers.updateRegisteredDetails.routes
import models.{CheckMode, UserAnswers}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UpdateContactDetailsSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    answers.get(UpdateContactDetailsPage).fold(Seq.empty[SummaryListRow]) {
      answer =>
        Seq(
          createSummaryListItem("fullName", answer.fullName),
          createSummaryListItem("position", answer.position),
          createSummaryListItem("phoneNumber", answer.phoneNumber),
          createSummaryListItem("email", answer.email)
        )
    }

  private def createSummaryListItem(fieldName: String, fieldValue: String)
                                   (implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = s"updateRegisteredDetails.updateContactDetails.$fieldName",
      value = ValueViewModel(Text(fieldValue)),
      actions = Seq(
        ActionItemViewModel("site.change", routes.UpdateContactDetailsController.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText(messages("updateRegisteredDetails.updateContactDetails.change.hidden"))
      )
    )
  }

}
