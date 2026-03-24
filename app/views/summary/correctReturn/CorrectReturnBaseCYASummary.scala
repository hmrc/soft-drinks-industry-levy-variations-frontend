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

import models.LevyCalculation
import models.backend.RetrievedSubscription
import models.correctReturn.ChangedPage
import models.{ Amounts, UserAnswers }
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

object CorrectReturnBaseCYASummary {

  def summaryListAndHeadings(
    userAnswers: UserAnswers,
    subscription: RetrievedSubscription,
    amounts: Amounts,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Seq[(String, SummaryList)] =
    (ownBrandsSummarySection(userAnswers, subscription = subscription, levyCalculations = levyCalculations) ++
      contractPackerSummarySection(userAnswers, levyCalculations = levyCalculations) ++
      contractPackedForRegisteredSmallProducersSection(userAnswers, levyCalculations = levyCalculations) ++
      broughtIntoUKSection(userAnswers, levyCalculations = levyCalculations) ++
      broughtIntoUkFromSmallProducersSection(userAnswers, levyCalculations = levyCalculations) ++
      claimCreditsForExportsSection(userAnswers, levyCalculations = levyCalculations) ++
      claimCreditsForLostDamagedSection(userAnswers, levyCalculations = levyCalculations) ++
      siteDetailsSection(userAnswers, subscription) ++
      amountToPaySummarySection(amounts)).toSeq

  def changedSummaryListAndHeadings(
    userAnswers: UserAnswers,
    subscription: RetrievedSubscription,
    changedPages: List[ChangedPage],
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Seq[(String, SummaryList)] =
    (ownBrandsSummarySection(
      userAnswers,
      changedPages.head.answerChanged,
      isCheckAnswers,
      subscription = subscription,
      levyCalculations = levyCalculations
    ) ++
      contractPackerSummarySection(
        userAnswers,
        changedPages.apply(2).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      contractPackedForRegisteredSmallProducersSection(
        userAnswers,
        changedPages.apply(12).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      broughtIntoUKSection(
        userAnswers,
        changedPages.apply(4).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      broughtIntoUkFromSmallProducersSection(
        userAnswers,
        changedPages.apply(6).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      claimCreditsForExportsSection(
        userAnswers,
        changedPages.apply(8).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      claimCreditsForLostDamagedSection(
        userAnswers,
        changedPages.apply(10).answerChanged,
        isCheckAnswers,
        levyCalculations = levyCalculations
      ) ++
      siteDetailsSection(userAnswers, subscription, isCheckAnswers)).toSeq

  private def ownBrandsSummarySection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    subscription: RetrievedSubscription,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] =
    if (changedPage && !subscription.activity.smallProducer) {
      val ownBrandsSummary: SummaryList =
        OperatePackagingSiteOwnBrandsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
      if (ownBrandsSummary.rows.isEmpty) None
      else {
        Option(
          "correctReturn.operatePackagingSiteOwnBrands.checkYourAnswersSectionHeader" ->
            ownBrandsSummary
        )
      }
    } else {
      None
    }

  private def contractPackerSummarySection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val contractPackedOwnSiteSummary: SummaryList =
      PackagedAsContractPackerSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
    val contractPackedOwnSiteList: Option[(String, SummaryList)] =
      if (contractPackedOwnSiteSummary.rows.isEmpty) None
      else {
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

  private def contractPackedForRegisteredSmallProducersSection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val contractPackedForRegisteredSmallProducers: SummaryList =
      ExemptionsForSmallProducersSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
    val contractPackedForRegisteredSmallProducersList: Option[(String, SummaryList)] =
      if (contractPackedForRegisteredSmallProducers.rows.isEmpty) None
      else {
        Option(
          "correctReturn.exemptionsForSmallProducers.checkYourAnswersSectionHeader" ->
            contractPackedForRegisteredSmallProducers
        )
      }
    val showContractPackedForRegisteredSmallProducersList: Option[(String, SummaryList)] = if (changedPage) {
      contractPackedForRegisteredSmallProducersList
    } else {
      None
    }
    showContractPackedForRegisteredSmallProducersList
  }

  private def broughtIntoUKSection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val broughtIntoUKSummary: SummaryList =
      BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
    val broughtIntoUKList: Option[(String, SummaryList)] =
      if (broughtIntoUKSummary.rows.isEmpty) None
      else {
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

  private def broughtIntoUkFromSmallProducersSection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val broughtIntoUkFromSmallProducersSummary: SummaryList =
      BroughtIntoUkFromSmallProducersSummary.summaryListWithBandLevyRows(
        userAnswers,
        isCheckAnswers,
        levyCalculations
      )
    val broughtIntoUkFromSmallProducersList: Option[(String, SummaryList)] =
      if (broughtIntoUkFromSmallProducersSummary.rows.isEmpty) None
      else {
        Option(
          "correctReturn.broughtIntoUkFromSmallProducers.checkYourAnswersSectionHeader" ->
            broughtIntoUkFromSmallProducersSummary
        )
      }
    val showBroughtIntoUkFromSmallProducersList: Option[(String, SummaryList)] = if (changedPage) {
      broughtIntoUkFromSmallProducersList
    } else {
      None
    }
    showBroughtIntoUkFromSmallProducersList
  }

  private def claimCreditsForExportsSection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val claimCreditsForExportsSummary: SummaryList =
      ClaimCreditsForExportsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
    val claimCreditsForExportsList: Option[(String, SummaryList)] =
      if (claimCreditsForExportsSummary.rows.isEmpty) None
      else {
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

  private def claimCreditsForLostDamagedSection(
    userAnswers: UserAnswers,
    changedPage: Boolean = true,
    isCheckAnswers: Boolean = true,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Option[(String, SummaryList)] = {
    val claimCreditsForLostDamagedSummary: SummaryList =
      ClaimCreditsForLostDestroyedSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers, levyCalculations)
    val claimCreditsForLostDamagedList: Option[(String, SummaryList)] =
      if (claimCreditsForLostDamagedSummary.rows.isEmpty) None
      else {
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

  private def siteDetailsSection(
    userAnswers: UserAnswers,
    subscription: RetrievedSubscription,
    isCheckAnswers: Boolean = true
  )(implicit messages: Messages): Option[(String, SummaryList)] =
    UKSitesSummary.getHeadingAndSummary(userAnswers, isCheckAnswers, subscription)

  private def amountToPaySummarySection(amounts: Amounts)(implicit messages: Messages): Option[(String, SummaryList)] =
    if (AmountToPaySummary.amountToPaySummary(amounts).rows.isEmpty) {
      None
    } else {
      Option("correctReturn.checkYourAnswers.summary" -> AmountToPaySummary.amountToPaySummary(amounts))
    }
}
