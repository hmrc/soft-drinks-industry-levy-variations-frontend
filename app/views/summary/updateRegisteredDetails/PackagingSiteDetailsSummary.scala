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
import models.backend.Site
import models.{CheckMode, NormalMode, UserAnswers}
import pages.updateRegisteredDetails.PackagingSiteDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PackagingSiteDetailsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PackagingSiteDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "updateRegisteredDetails.packagingSiteDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("updateRegisteredDetails.packagingSiteDetails.change.hidden"))
          )
        )
    }

  def row2(packingSiteList: Map[String, Site])(implicit messages: Messages): List[SummaryListRow] = {
    packingSiteList.map {
        packingSite =>
          SummaryListRow(
            key     = Key(
              content = HtmlContent(AddressFormattingHelper.addressFormatting(packingSite._2.address, packingSite._2.tradingName)),
              classes = "govuk-!-font-weight-regular govuk-!-width-full"
            ),
            actions = if(packingSiteList.size > 1){ Some(Actions("",Seq(
              ActionItemViewModel("site.remove", routes.PackingSiteDetailsRemoveController.onPageLoad(NormalMode, packingSite._1).url)
                .withVisuallyHiddenText(messages("updateRegisteredDetails.packagingSiteDetails.remove.hidden",
                  packingSite._2.tradingName.getOrElse(""), packingSite._2.address.lines.head))
            )))} else {
              None
            }
          )
      }.toList
    }
}
