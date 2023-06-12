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
import models.{CheckMode, NormalMode, UserAnswers, Warehouse}
import pages.updateRegisteredDetails.WarehouseDetailsPage
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WarehouseDetailsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WarehouseDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "updateRegisteredDetails.warehouseDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("updateRegisteredDetails.warehouseDetails.change.hidden"))
          )
        )
    }

  def row2(warehouseList: Map[String, Warehouse])(implicit messages: Messages):Object = {
    warehouseList match {
      case warehouseList if warehouseList.isEmpty => Html(messages("updateRegisteredDetails.warehouseDetails.subtext"))
      case warehouseList if !warehouseList.isEmpty => warehouseList.map {
          warehouse =>
            SummaryListRow(
              key     = Key(HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName))),
              classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds",
              actions = Some(Actions("",Seq(
                ActionItemViewModel("site.remove", routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
                  .withVisuallyHiddenText(messages("secondaryWarehouseDetails.remove.hidden"))
              )))
            )
        }.toList
      }
    }
}
