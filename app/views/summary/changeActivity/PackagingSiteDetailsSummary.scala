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
import models.backend.Site
import models.{CheckMode, Mode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PackagingSiteDetailsSummary  {

  def row2(packingSiteList: Map[String, Site], mode: Mode)(implicit messages: Messages): List[SummaryListRow] = {
    packingSiteList.map {
        packingSite =>
          SummaryListRow(
            key     = Key(
              content = HtmlContent(AddressFormattingHelper.addressFormatting(packingSite._2.address, packingSite._2.tradingName)),
              classes = "govuk-!-font-weight-regular govuk-!-width-full"
            ),
            actions = if (packingSiteList.size > 1) {
              Some(Actions("", Seq(
                ActionItemViewModel("site.remove", controllers.changeActivity.routes.RemovePackagingSiteDetailsController.onPageLoad(mode, packingSite._1).url)
                  .withVisuallyHiddenText(messages("changeActivity.packagingSiteDetails.remove.hidden", packingSite._2.tradingName.getOrElse(""),
                    packingSite._2.address.lines.head))
              )))
            } else {
              None
            }
          )
      }.toList
    }

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                 (implicit messages: Messages): SummaryList = {
    val key = if (userAnswers.warehouseList.size != 1) {
      messages("checkYourAnswers.packing.checkYourAnswersLabel.multiple", userAnswers.packagingSiteList.size.toString)
    } else {
      messages("checkYourAnswers.packing.checkYourAnswersLabel.one", userAnswers.packagingSiteList.size.toString)
    }
    val visuallyHiddenChangeText = if (userAnswers.packagingSiteList.size != 1) {
      messages("checkYourAnswers.sites.packing.change.hidden.multiple")
    } else {
      messages("checkYourAnswers.sites.packing.change.hidden.one")
    }

    SummaryListViewModel(
      rows = Seq(SummaryListRowViewModel(
        key = Key(
          content = key,
          classes = "govuk-!-width-full"
        ),
        value = Value(),
        actions = if (isCheckAnswers) {
          Seq(
            ActionItemViewModel("site.change", routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
              .withAttribute(("id", "change-packaging-sites"))
              .withVisuallyHiddenText(visuallyHiddenChangeText)
          )
        } else {
          Seq.empty
        }
      ))
    )
  }
}
