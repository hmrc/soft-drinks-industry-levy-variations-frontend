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

import controllers.changeActivity.routes._
import models.backend.Site
import models.{Mode, NormalMode, UserAnswers}
import pages.updateRegisteredDetails.WarehouseDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

//TODO: What is the difference between this and SecondaryWarehouseDetailsSummary
object WarehouseDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WarehouseDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "updateRegisteredDetails.warehouseDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
              .withVisuallyHiddenText(messages("updateRegisteredDetails.warehouseDetails.change.hidden"))
          )
        )
    }

  def row2(warehouseList: Map[String, Site], mode: Mode)(implicit messages: Messages): List[SummaryListRow] = {
    warehouseList.map {
          warehouse =>
            SummaryListRow(
              key     = Key(
                content = HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName)),
                classes = "govuk-!-font-weight-regular govuk-!-width-full"
              ),
              actions = Some(Actions("",Seq(
                ActionItemViewModel("site.remove",
                  controllers.updateRegisteredDetails.routes.RemoveWarehouseDetailsController.onPageLoad(mode, warehouse._1).url )
                  .withVisuallyHiddenText(messages("updateRegisteredDetails.warehouseDetails.remove.hidden",
                    warehouse._2.tradingName.getOrElse(""), warehouse._2.address.lines.head))
              )))
            )
          }.toList
    }

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                 (implicit messages: Messages): SummaryList = {

          SummaryListViewModel(
            rows = Seq(SummaryListRowViewModel(
              key = if(userAnswers.warehouseList.size != 1){
                messages("checkYourAnswers.warehouse.checkYourAnswersLabel.multiple",  {userAnswers.warehouseList.size.toString})}else{
                messages("checkYourAnswers.warehouse.checkYourAnswersLabel.one", {userAnswers.warehouseList.size.toString})
              },
              value = Value(),
              actions = if (isCheckAnswers) {
                Seq(
                  ActionItemViewModel("site.change", SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                    .withAttribute(("id", "change-packaging-sites"))
                    .withVisuallyHiddenText(messages("checkYourAnswers.sites.warehouse.change.hidden.one"))
                )
              } else {
                Seq.empty
              }
            )
            )
          )
  }

}
