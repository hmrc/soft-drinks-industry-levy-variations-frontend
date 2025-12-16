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
import models.NormalMode
import models.updateRegisteredDetails.ContactDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ContactDetailsSummary {

  def rows(contact: ContactDetails)(implicit messages: Messages): SummaryList =
    SummaryList(rows =
      Seq(
        createSummaryListItem(
          "fullName",
          contact.fullName,
          messages("updateRegisteredDetails.contactDetails.name.change.hidden")
        ),
        createSummaryListItem(
          "position",
          contact.position,
          messages("updateRegisteredDetails.contactDetails.position.change.hidden")
        ),
        createSummaryListItem(
          "phoneNumber",
          contact.phoneNumber,
          messages("updateRegisteredDetails.contactDetails.phoneNumber.change.hidden")
        ),
        createSummaryListItem(
          "email",
          contact.email,
          messages("updateRegisteredDetails.contactDetails.email.change.hidden")
        )
      )
    )

  private def createSummaryListItem(fieldName: String, fieldValue: String, visuallyHiddenMessage: String)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRow(
      key = s"updateRegisteredDetails.updateContactDetails.$fieldName",
      value = Value(Text(fieldValue)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(routes.UpdateContactDetailsController.onPageLoad(NormalMode).url, "site.change")
              .withVisuallyHiddenText(visuallyHiddenMessage)
          )
        )
      )
    )

}
