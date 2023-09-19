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
import models.correctReturn.ChangedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.summary.correctReturn.ExemptionsForSmallProducersSummary

object CorrectReturnChangeSummary {
  def summaryList(userAnswers: UserAnswers)
                 (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {

    val ownBrandsSummary: Option[(String, SummaryList)] =
      Some(OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true))
    val contractPackedOwnSiteSummary: (String, SummaryList) = PackagedAsContractPackerSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val contractPackedForRegisteredSmallProducers: (String, SummaryList) =
      ExemptionsForSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val broughtIntoUKSummary: (String, SummaryList) = BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val broughtIntoUkFromSmallProducersSummary: (String, SummaryList) =
      BroughtIntoUkFromSmallProducersSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val claimCreditsForExportsSummary: (String, SummaryList) = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val claimCreditsForExportsSummary: (String, SummaryList) = ClaimCreditsForExportsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = true)
    val packagingSitesSummary: Option[String, SummaryList]

    Seq(
      Some(ownBrandsSummary),
      contractPackedOwnSiteSummary,
      contractPackedForRegisteredSmallProducers,
      broughtIntoUKSummary,
      broughtIntoUkFromSmallProducersSummary,
      claimCreditsForExportsSummary,
      claimCreditsForExportsSummary
    ).flatten
  }




}