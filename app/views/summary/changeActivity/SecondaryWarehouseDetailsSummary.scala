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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import models.backend.Site
import pages.changeActivity.SecondaryWarehouseDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

//TODO: What is the difference between this and WarehouseDetailsSummary
object SecondaryWarehouseDetailsSummary  {

  def cyaRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SecondaryWarehouseDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "changeActivity.secondaryWarehouseDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("changeActivity.secondaryWarehouseDetails.change.hidden"))
          )
        )
    }

  def summaryRows(warehouseList: Map[String, Site], mode: Mode)(implicit messages: Messages): List[SummaryListRow] = {
    warehouseList.map {
      warehouse =>
        SummaryListRow(
          key = Key(HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName))),
          classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds",
          actions = Some(Actions("", Seq(
            ActionItemViewModel("site.remove", routes.RemoveWarehouseDetailsController.onPageLoad(warehouse._1, mode).url)
              .withVisuallyHiddenText(messages("changeActivity.secondaryWarehouseDetails.remove.hidden",
                warehouse._2.tradingName.getOrElse(""), warehouse._2.address.lines.head))
          )))
        )
    }
  }.toList

}
