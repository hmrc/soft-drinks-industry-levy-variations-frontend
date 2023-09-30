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
import models.correctReturn.ChangedPage
import models.{RetrievedSubscription, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.correctReturn.ExemptionsForSmallProducersSummary
import views.summary.UKSitesSummary

object CorrectReturnBaseCYASummary {

  def summaryListAndHeadings(userAnswers: UserAnswers, subscription: RetrievedSubscription)
                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    (ownBrandsSummarySection(userAnswers) ++ contractPackerSummarySection(userAnswers) ++
      contractPackedForRegisteredSmallProducersSection(userAnswers) ++ broughtIntoUKSection(userAnswers) ++
      broughtIntoUkFromSmallProducersSection(userAnswers) ++ claimCreditsForExportsSection(userAnswers) ++
      claimCreditsForLostDamagedSection(userAnswers) ++ siteDetailsSection(userAnswers, subscription)).toSeq
  }

  def changedSummaryListAndHeadings(userAnswers: UserAnswers, subscription: RetrievedSubscription, changedPages: List[ChangedPage])
                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    (ownBrandsSummarySection(userAnswers, changedPages.head.answerChanged) ++
      contractPackerSummarySection(userAnswers, changedPages.apply(2).answerChanged) ++
      contractPackedForRegisteredSmallProducersSection(userAnswers, changedPages.apply(12).answerChanged) ++
      broughtIntoUKSection(userAnswers, changedPages.apply(4).answerChanged) ++
      broughtIntoUkFromSmallProducersSection(userAnswers, changedPages.apply(6).answerChanged) ++
      claimCreditsForExportsSection(userAnswers, changedPages.apply(8).answerChanged) ++
      claimCreditsForLostDamagedSection(userAnswers, changedPages.apply(10).answerChanged) ++
      siteDetailsSection(userAnswers, subscription)
      ).toSeq
  }

  private def ownBrandsSummarySection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                     (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val ownBrandsSummary: SummaryList = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true)
    val ownBrandsSummaryList: Option[(String, SummaryList)] = if (ownBrandsSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader" ->
          ownBrandsSummary
      )
    }
    val showOwnBrandsSummaryList: Option[(String, SummaryList)] = if (changedPage) {
      ownBrandsSummaryList
    } else {
      None
    }
    showOwnBrandsSummaryList
  }
  private def contractPackerSummarySection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                          (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val contractPackedOwnSiteSummary: SummaryList = PackagedAsContractPackerSummary.summaryList(userAnswers, isCheckAnswers = true)
    val contractPackedOwnSiteList: Option[(String, SummaryList)] = if (contractPackedOwnSiteSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.packagedAsContractPacker.checkYourAnswersSectionHeader" ->
          contractPackedOwnSiteSummary
      )
    }
    val showContractPackedOwnSiteList: Option[(String, SummaryList)] = if (changedPage) {
      contractPackedOwnSiteList
    } else {
      None
    }
    showContractPackedOwnSiteList
  }

  private def contractPackedForRegisteredSmallProducersSection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                                              (implicit messages: Messages, frontendAppConfig: FrontendAppConfig)
  : Option[(String, SummaryList)] = {
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
    val showContractPackedForRegisteredSmallProducersList: Option[(String, SummaryList)] = if (changedPage) {
      contractPackedForRegisteredSmallProducersList
    } else {
      None
    }
    showContractPackedForRegisteredSmallProducersList
  }

  private def broughtIntoUKSection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                  (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val broughtIntoUKSummary: SummaryList = BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers = true)
    val broughtIntoUKList: Option[(String, SummaryList)] = if (broughtIntoUKSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.broughtIntoUK.checkYourAnswersSectionHeader" ->
          broughtIntoUKSummary
      )
    }
    val showBroughtIntoUKList: Option[(String, SummaryList)] = if (changedPage) {
      broughtIntoUKList
    } else {
      None
    }
    showBroughtIntoUKList
  }

  private def broughtIntoUkFromSmallProducersSection(userAnswers: UserAnswers, changedPage: Boolean = true)
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
    val showBroughtIntoUkFromSmallProducersList: Option[(String, SummaryList)] = if (changedPage) {
      broughtIntoUkFromSmallProducersList
    } else {
      None
    }
    showBroughtIntoUkFromSmallProducersList
  }

  private def claimCreditsForExportsSection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                           (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val claimCreditsForExportsSummary: SummaryList = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForExportsList: Option[(String, SummaryList)] = if (claimCreditsForExportsSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForExports.checkYourAnswersSectionHeader" ->
          claimCreditsForExportsSummary
      )
    }
    val showClaimCreditsForExportsList: Option[(String, SummaryList)] = if (changedPage) {
      claimCreditsForExportsList
    } else {
      None
    }
    showClaimCreditsForExportsList
  }

  private def claimCreditsForLostDamagedSection(userAnswers: UserAnswers, changedPage: Boolean = true)
                                               (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[(String, SummaryList)] = {
    val claimCreditsForLostDamagedSummary: SummaryList = ClaimCreditsForLostDestroyedSummary.summaryList(userAnswers, isCheckAnswers = true)
    val claimCreditsForLostDamagedList: Option[(String, SummaryList)] = if (claimCreditsForLostDamagedSummary.rows.isEmpty) None else {
      Option(
        "correctReturn.claimCreditsForLostDamaged.checkYourAnswersSectionHeader" ->
          claimCreditsForLostDamagedSummary
      )
    }
    val showClaimCreditsForLostDamagedList: Option[(String, SummaryList)] = if (changedPage) {
      claimCreditsForLostDamagedList
    } else {
      None
    }
    showClaimCreditsForLostDamagedList
  }

  private def siteDetailsSection(userAnswers: UserAnswers, subscription: RetrievedSubscription)(implicit messages: Messages): Option[(String, SummaryList)] =
    UKSitesSummary.getHeadingAndSummaryForCorrectReturn(userAnswers, isCheckAnswers = true, subscription)

}