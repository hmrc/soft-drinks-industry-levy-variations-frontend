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

package views.summary.updateRegisteredDetails

import controllers.updateRegisteredDetails.routes
import models.{ CheckMode, UserAnswers }
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ ActionItem, Actions, SummaryList, SummaryListRow, Value }
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UpdateContactDetailsSummary {

  def rows(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit
    messages: Messages
  ): Option[(String, SummaryList)] =
    answers.get(UpdateContactDetailsPage).fold(Option.empty[(String, SummaryList)]) { answer =>
      Some(
        messages("updateRegisteredDetails.checkYourAnswers.updateContactDetails.title") ->
          SummaryList(
            rows = Seq(
              createSummaryListItem("fullName", answer.fullName, isCheckAnswers),
              createSummaryListItem("position", answer.position, isCheckAnswers),
              createSummaryListItem("phoneNumber", answer.phoneNumber, isCheckAnswers),
              createSummaryListItem("email", answer.email, isCheckAnswers)
            )
          )
      )
    }

  private def createSummaryListItem(fieldName: String, fieldValue: String, isCheckAnswers: Boolean)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRow(
      key = s"updateRegisteredDetails.updateContactDetails.$fieldName",
      value = Value(Text(fieldValue)),
      actions = Some(
        Actions(
          items = if (isCheckAnswers) {
            Seq(
              ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
                .withVisuallyHiddenText(messages("updateRegisteredDetails.updateContactDetails.change.hidden"))
                .withAttribute(("id", "change-contactDetailsAdd"))
            )
          } else {
            Seq.empty
          }
        )
      )
    )

}
