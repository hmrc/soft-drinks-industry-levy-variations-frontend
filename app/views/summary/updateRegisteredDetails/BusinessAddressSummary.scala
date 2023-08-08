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

import models.backend.UkAddress
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessAddressSummary  {

  def summaryList(businessAddressList: List[UkAddress])(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(
      rows = row(businessAddressList)
    )
  }

  def row(businessAddressList: List[UkAddress])(implicit messages: Messages): List[SummaryListRow] = {
    businessAddressList.map {
      businessAddress =>
        SummaryListRow(
          key = Key(
            content = HtmlContent(AddressFormattingHelper.addressFormatting(businessAddress, None)),
            classes = "govuk-!-font-weight-regular govuk-!-width-full"
          ),
          actions = Some(Actions("", Seq(
            ActionItemViewModel("site.change", controllers.routes.IndexController.onPageLoad.url)
              .withVisuallyHiddenText(messages("updateRegisteredDetails.businessAddress.change.hidden"))
          )))
        )
    }
  }

}