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
import models.backend.RetrievedSubscription
import models.correctReturn.ChangedPage
import models.{ Amounts, UserAnswers }
import pages.correctReturn.BalanceRepaymentRequired
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ SummaryList, SummaryListRow }

object CorrectReturnCheckChangesSummary {

  def changeSpecificSummaryListAndHeadings(
    userAnswers: UserAnswers,
    subscription: RetrievedSubscription,
    changedPages: List[ChangedPage],
    isCheckAnswers: Boolean = true,
    amounts: Amounts
  )(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    val mainSection =
      CorrectReturnBaseCYASummary.changedSummaryListAndHeadings(userAnswers, subscription, changedPages, isCheckAnswers)
    val correctionDetailsSection = correctionSection(userAnswers, isCheckAnswers)
    val balanceSection = Map(
      "correctReturn.checkChanges.balanceHeader" -> AmountToPaySummary.amountToPayBalance(amounts)
    )
    mainSection ++ correctionDetailsSection ++ balanceSection
  }

  private def correctionSection(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit
    messages: Messages
  ): Option[(String, SummaryList)] = {
    val correctionReasonSummary: Option[SummaryListRow] = CorrectionReasonSummary.row(userAnswers, isCheckAnswers)
    val repaymentMethodSummary: Option[SummaryListRow] = userAnswers.get(BalanceRepaymentRequired) match {
      case Some(true) => RepaymentMethodSummary.row(userAnswers, isCheckAnswers)
      case _          => None
    }
    val correctionSectionSummaryList = Option(SummaryList(Seq(correctionReasonSummary, repaymentMethodSummary).flatten))
    correctionSectionSummaryList.map("correctReturn.correctionSection.checkYourAnswersLabel" -> _)
  }

}
