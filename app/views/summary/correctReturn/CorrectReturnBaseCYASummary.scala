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

import config.FrontendAppConfig
import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.correctReturn.ExemptionsForSmallProducersSummary
import views.summary.UKSitesSummary

object CorrectReturnBaseCYASummary {
  def summaryListAndHeadings(userAnswers: UserAnswers)
                 (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {

    val ownBrandsSummary: Option[SummaryList] =
      Some(OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true))
    val ownBrandsSection: Option[(String, SummaryList)] = ownBrandsSummary.map(summary => {
      "correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader" -> summary
    })
    val contractPackedOwnSiteSummary: SummaryList = PackagedAsContractPackerSummary.summaryList(userAnswers, isCheckAnswers = true)
    val contractPackedOwnSiteSection: Option[(String, SummaryList)] = if (contractPackedOwnSiteSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader" ->
          contractPackedOwnSiteSummary
      )
    }
    val contractPackedForRegisteredSmallProducers: SummaryList =
      ExemptionsForSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true)
    val contractPackedForRegisteredSmallProducersSection: Option[(String, SummaryList)] =
      if (contractPackedForRegisteredSmallProducers.rows.isEmpty) None else {
        Option(
          "correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader" ->
            contractPackedForRegisteredSmallProducers
        )
      }
    val broughtIntoUKSummary: SummaryList = BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers = true)
    val broughtIntoUKSection: Option[(String, SummaryList)] = if (broughtIntoUKSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.broughtIntoUK.checkYourAnswersSectionHeader" ->
          broughtIntoUKSummary
      )
    }
    val broughtIntoUkFromSmallProducersSummary: SummaryList =
      BroughtIntoUkFromSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true)
    val broughtIntoUkFromSmallProducersSection: Option[(String, SummaryList)] =
      if (broughtIntoUkFromSmallProducersSummary.rows.isEmpty) None else {
        Option(
          "correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader" ->
            broughtIntoUkFromSmallProducersSummary
        )
      }
    val claimCreditsForExportsSummary: SummaryList = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForExportsSection: Option[(String, SummaryList)] = if (claimCreditsForExportsSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader" ->
          claimCreditsForExportsSummary
      )
    }
    val claimCreditsForLostDamagedSummaryPlaceholder: SummaryList = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForLostDemagedSection: Option[(String, SummaryList)] = if (claimCreditsForLostDamagedSummaryPlaceholder.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader" ->
          claimCreditsForLostDamagedSummaryPlaceholder
      )
    }
    val siteDetailsSection: Option[(String, SummaryList)] = UKSitesSummary.getHeadingAndSummary(userAnswers, isCheckAnswers = true)

    (ownBrandsSection ++ contractPackedOwnSiteSection ++ contractPackedForRegisteredSmallProducersSection ++
      broughtIntoUKSection ++ broughtIntoUkFromSmallProducersSection ++ claimCreditsForExportsSection ++
      claimCreditsForLostDemagedSection ++ siteDetailsSection).toSeq
  }

}
