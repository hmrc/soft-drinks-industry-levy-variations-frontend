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

import config.FrontendAppConfig
import models.UserAnswers
import models.changeActivity.AmountProduced.Small
import pages.changeActivity.{AmountProducedPage, PackagingSiteDetailsPage, SecondaryWarehouseDetailsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.summary.changeActivity.PackagingSiteDetailsSummary

object ChangeActivitySummary  {

  def summaryListsAndHeadings(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    val amountProducedSummary: Option[SummaryListRow] = AmountProducedSummary.row(userAnswers, isCheckAnswers)
    val thirdPartyPackagersSummary: Option[SummaryListRow] = ThirdPartyPackagersSummary.row(userAnswers, isCheckAnswers)
    val ownBrandsSummary: SummaryList = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
    val contractSummary: SummaryList = ContractPackingSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
    val importsSummary: SummaryList = ImportsSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
    val packingSummary: SummaryList = PackagingSiteDetailsSummary.summaryList(userAnswers, isCheckAnswers)
    val warehouseSummary: SummaryList = SecondaryWarehouseDetailsSummary.summaryList(userAnswers, isCheckAnswers)
    val amountProducedSection: Option[(String, SummaryList)] = amountProducedSummary.map(summary => {
      "changeActivity.checkYourAnswers.amountProducedSection" -> SummaryList(Seq(summary))
    })
    val thirdPartyPackagersSection: Option[(String, SummaryList)] = if (userAnswers.get(AmountProducedPage).contains(Small)) {
      thirdPartyPackagersSummary.map(summary => {
        "changeActivity.checkYourAnswers.thirdPartyPackagersSection" -> SummaryList(Seq(summary))
      })
    } else None
    val ownBrandsSection: Option[(String, SummaryList)] = if (ownBrandsSummary.rows.isEmpty) None else {
      Option(
        "changeActivity.checkYourAnswers.operatePackingSiteOwnBrandsSection" ->
          OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
      )
    }
    val contractSection: Option[(String, SummaryList)] = if (contractSummary.rows.isEmpty) None else {
      Option(
        "changeActivity.checkYourAnswers.contractPackingSection" ->
          ContractPackingSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
      )
    }
    val importsSection: Option[(String, SummaryList)] = if (importsSummary.rows.isEmpty) None else {
      Option(
        "changeActivity.checkYourAnswers.importsSection" ->
          ImportsSummary.summaryList(userAnswers, isCheckAnswers, includeLevyRows = false)
      )
    }
    val sitesSection: Option[(String, SummaryList)] = {
      Option("checkYourAnswers.sites" -> SummaryList(packingSummary.rows ++ warehouseSummary.rows))
    }

    (amountProducedSection ++ thirdPartyPackagersSection ++ ownBrandsSection ++ contractSection ++ importsSection ++ sitesSection).toSeq
  }
}
