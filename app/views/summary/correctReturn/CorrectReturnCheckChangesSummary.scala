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
import models.{Amounts, RetrievedSubscription, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import views.helpers.AmountToPaySummary

object CorrectReturnCheckChangesSummary {

  def changeSpecificSummaryListAndHeadings(userAnswers: UserAnswers, subscription: RetrievedSubscription, changedPages: List[ChangedPage], amounts: Amounts)
                                          (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    val mainSection = CorrectReturnBaseCYASummary.changedSummaryListAndHeadings(userAnswers, subscription, changedPages)
    val correctionDetailsSection = correctionSection(userAnswers)
    val BalanceSection = Map("correctReturn.balance"-> AmountToPaySummary.amountToPaySummary(amounts))
    mainSection ++ correctionDetailsSection ++ BalanceSection
  }

  private def correctionSection(userAnswers: UserAnswers)
                                     (implicit messages: Messages): Option[(String, SummaryList)] = {
    val correctionReasonSummary: Option[SummaryListRow] = CorrectionReasonSummary.row(userAnswers)
    val repaymentMethodSummary: Option[SummaryListRow] = RepaymentMethodSummary.row(userAnswers)

    val correctionSectionSummaryList: Option[SummaryList] = for {
      correctionReason <- correctionReasonSummary
      repaymentMethod <- repaymentMethodSummary
    } yield SummaryList(Seq(correctionReason, repaymentMethod))
    correctionSectionSummaryList.map("correctReturn.correctionSection.checkYourAnswersLabel" -> _)}

}
