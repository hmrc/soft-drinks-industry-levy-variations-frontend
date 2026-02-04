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

package views.summary.correctReturn

import models.backend.Site
import models.Mode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ Actions, HtmlContent, Key }
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ SummaryList, SummaryListRow }
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PackagingSiteDetailsSummary {

  def summaryList(packagingSiteList: Map[String, Site], mode: Mode)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = row2(packagingSiteList, mode)
    )

  def row2(packagingSiteList: Map[String, Site], mode: Mode)(implicit messages: Messages): List[SummaryListRow] =
    packagingSiteList.map { packagingSite =>
      SummaryListRow(
        key = Key(
          content = HtmlContent(
            AddressFormattingHelper.addressFormatting(packagingSite._2.address, packagingSite._2.tradingName)
          ),
          classes = "govuk-!-font-weight-regular govuk-!-width-full"
        ),
        actions = if (packagingSiteList.size > 1) {
          Some(
            Actions(
              "",
              Seq(
                ActionItemViewModel(
                  "site.remove",
                  controllers.correctReturn.routes.RemovePackagingSiteConfirmController
                    .onPageLoad(mode, packagingSite._1)
                    .url
                )
                  .withVisuallyHiddenText(
                    messages(
                      "correctReturn.packagingSiteDetails.remove.hidden",
                      packagingSite._2.tradingName.getOrElse(""),
                      packagingSite._2.address.lines.head
                    )
                  )
              )
            )
          )
        } else {
          None
        }
      )
    }.toList
}
