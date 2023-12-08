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

import models.Amounts
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utilities.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary  {
  def amountToPaySummary(amounts: Amounts)(implicit messages: Messages): SummaryList = {
    val totalForQuarter: BigDecimal = amounts.newReturnTotal
    val balanceBroughtForward: BigDecimal = amounts.balanceBroughtForward
    val total: BigDecimal = amounts.totalForQuarterLessForwardBalance

    val negatedBalanceBroughtForward = balanceBroughtForward * -1

    SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "correctReturn.checkYourAnswers.totalThisQuarter",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(totalForQuarter)
          .replace("-", "&minus;")))
          .withCssClass("total-for-quarter govuk-!-text-align-right")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.checkYourAnswers.balanceBroughtForward",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(negatedBalanceBroughtForward)
          .replace("-", "&minus;")))
          .withCssClass("balance-brought-forward govuk-!-text-align-right")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.checkYourAnswers.total",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total)
          .replace("-", "&minus;")))
          .withCssClass("total govuk-!-text-align-right govuk-!-font-weight-bold")
      ))
    )
  }

  def subheader(total: BigDecimal)(implicit messages: Messages):String = {
    if (total < 0) {
      Messages("correctReturn.checkYourAnswers.yourSoftDrinksLevyAccountsWillBeCredited",
        CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total * -1).replace("-", "&minus;"))
    } else {
      Messages("correctReturn.checkYourAnswers.youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total))
    }
  }

  def amountToPayBalance(amounts: Amounts)(implicit messages: Messages): SummaryList = {

    val originalReturnTotal: BigDecimal = amounts.originalReturnTotal
    val newReturnTotal: BigDecimal = amounts.newReturnTotal
    val balanceBroughtForward: BigDecimal = amounts.balanceBroughtForward
    val adjustedAmount: BigDecimal = amounts.netAdjustedAmount

    val negatedBalanceBroughtForward = balanceBroughtForward * -1

    SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "correctReturn.checkChanges.originalReturnTotal",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(originalReturnTotal).replace("-", "&minus;")))
          .withCssClass("original-return-total govuk-!-text-align-right")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.checkChanges.newReturnTotal",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(newReturnTotal).replace("-", "&minus;")))
          .withCssClass("new-return-total govuk-!-text-align-right")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.checkChanges.balanceBroughtForward",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(negatedBalanceBroughtForward).replace("-", "&minus;")))
          .withCssClass("balance-brought-forward govuk-!-text-align-right")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.checkChanges.adjustedAmount",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(adjustedAmount).replace("-", "&minus;")))
          .withCssClass("net-adjusted-amount govuk-!-text-align-right govuk-!-font-weight-bold")
      ))
    )
  }
}

