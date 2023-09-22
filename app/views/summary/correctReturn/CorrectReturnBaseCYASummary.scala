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
import models.{RetrievedSubscription, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.correctReturn.ExemptionsForSmallProducersSummary
import views.summary.UKSitesSummary

object CorrectReturnBaseCYASummary {

  def summaryListAndHeadings(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    val allSummarySectionsExceptOwnBrands = (contractPackerSummarySection(userAnswers) ++
      contractPackedForRegisteredSmallProducersSection(userAnswers) ++ broughtIntoUKSection(userAnswers) ++
      broughtIntoUkFromSmallProducersSection(userAnswers) ++ claimCreditsForExportsSection(userAnswers) ++
      claimCreditsForLostDamagedSection(userAnswers) ++ siteDetailsSection(userAnswers, subscription)
      )
    if (subscription.activity.smallProducer) {
      allSummarySectionsExceptOwnBrands.toSeq
    } else {
      (allSummarySectionsExceptOwnBrands ++ ownBrandsSummarySection(userAnswers)).toSeq
    }
  }

  def ownBrandsSummarySection(userAnswers: UserAnswers)
                             (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val ownBrandsSummary: Option[SummaryList] =
      Some(OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true))
    val ownBrandsList: Option[(String, SummaryList)] = ownBrandsSummary.map(summary => {
      "correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader" -> summary
    })
    ownBrandsList
  }

  def contractPackerSummarySection(userAnswers: UserAnswers)
                                  (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val contractPackedOwnSiteSummary: SummaryList = PackagedAsContractPackerSummary.summaryList(userAnswers, isCheckAnswers = true)
    val contractPackedOwnSiteList: Option[(String, SummaryList)] = if (contractPackedOwnSiteSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader" ->
          contractPackedOwnSiteSummary
      )
    }
    contractPackedOwnSiteList
  }

  def contractPackedForRegisteredSmallProducersSection(userAnswers: UserAnswers)
                                                      (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val contractPackedForRegisteredSmallProducers: SummaryList =
      ExemptionsForSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true)
    val contractPackedForRegisteredSmallProducersList: Option[(String, SummaryList)] = {
      if (contractPackedForRegisteredSmallProducers.rows.isEmpty) None else {
        Option(
          "correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader" ->
            contractPackedForRegisteredSmallProducers
        )
      }
    }
    contractPackedForRegisteredSmallProducersList
  }

  def broughtIntoUKSection(userAnswers: UserAnswers)
                          (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val broughtIntoUKSummary: SummaryList = BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers = true)
    val broughtIntoUKList: Option[(String, SummaryList)] = if (broughtIntoUKSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.broughtIntoUK.checkYourAnswersSectionHeader" ->
          broughtIntoUKSummary
      )
    }
    broughtIntoUKList
  }

  def broughtIntoUkFromSmallProducersSection(userAnswers: UserAnswers)
                                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
  val broughtIntoUkFromSmallProducersSummary: SummaryList =
    BroughtIntoUkFromSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true)
  val broughtIntoUkFromSmallProducersList: Option[(String, SummaryList)] = {
    if (broughtIntoUkFromSmallProducersSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader" ->
          broughtIntoUkFromSmallProducersSummary
      )
    }
  }
    broughtIntoUkFromSmallProducersList
  }

  def claimCreditsForExportsSection(userAnswers: UserAnswers)
                                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val claimCreditsForExportsSummary: SummaryList = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForExportsList: Option[(String, SummaryList)] = if (claimCreditsForExportsSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader" ->
          claimCreditsForExportsSummary
      )
    }
    claimCreditsForExportsList
  }

  def claimCreditsForLostDamagedSection(userAnswers: UserAnswers)
                                   (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val claimCreditsForLostDamagedSummary: SummaryList = ClaimCreditsForLostDestroyedSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForLostDamagedList: Option[(String, SummaryList)] = if (claimCreditsForLostDamagedSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader" ->
          claimCreditsForLostDamagedSummary
      )
    }
    claimCreditsForLostDamagedList
  }

  def siteDetailsSection(userAnswers: UserAnswers, subscription: RetrievedSubscription)(implicit messages: Messages): Option[(String, SummaryList)] =
    UKSitesSummary.getHeadingAndSummary(userAnswers, isCheckAnswers = true, subscription)

}
