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

import models.UserAnswers
import models.backend.UkAddress
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessAddressSummary  {

  def summaryList(businessAddressList: List[UkAddress])(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(
      rows = row(businessAddressList)
    )
  }

  def row(businessAddressList: List[UkAddress], isCheckAnswers: Boolean = true)(implicit messages: Messages): List[SummaryListRow] = {
    businessAddressList.map {
      businessAddress =>
        if (isCheckAnswers) {
          SummaryListRow(
            key = Key(
              content = HtmlContent(AddressFormattingHelper.addressFormatting(businessAddress, None)),
              classes = "govuk-!-font-weight-regular govuk-!-width-full"
            ),
            actions = Some(Actions("", Seq(
              ActionItemViewModel("site.change", controllers.updateRegisteredDetails.routes.BusinessAddressController.changeAddress().url)
                .withVisuallyHiddenText(messages("updateRegisteredDetails.businessAddress.change.hidden"))
                .withAttribute(("id", "change-businessAddress"))
            )))
          )
        } else {
          SummaryListRow(
            key = "updateRegisteredDetails.businessAddress.key",
            value = Value(HtmlContent(AddressFormattingHelper.addressFormatting(businessAddress, None)))
          )
        }
    }
  }

  def rows(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): Option[(String, SummaryList)] = {
    Some(
      messages("updateRegisteredDetails.checkYourAnswers.businessAddress.title") ->
        SummaryList(
          rows = row(List(answers.contactAddress), isCheckAnswers)
        )
    )
  }

}
