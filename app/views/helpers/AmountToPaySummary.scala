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

package views.helpers

import models.Amounts
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utilities.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary  {
  def amountToPaySummary(amounts: Amounts)(implicit messages: Messages): SummaryList = {

    val originalReturnTotal: BigDecimal = amounts.originalReturnTotal
    val newReturnTotal: BigDecimal = amounts.newReturnTotal
    val accountBalance: BigDecimal = amounts.accountBalance
    val adjustedAmount: BigDecimal = amounts.adjustedAmount

    SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "correctReturn.originalReturnTotal",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(originalReturnTotal).replace("-", "&minus;")))
          .withCssClass("original-return-total sdil-right-align--desktop")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.newReturnTotal",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(newReturnTotal).replace("-", "&minus;")))
          .withCssClass("new-return-total sdil-right-align--desktop")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.accountBalance",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(accountBalance).replace("-", "&minus;")))
          .withCssClass("balance-brought-forward sdil-right-align--desktop")
      ),
      SummaryListRowViewModel(
        key = "correctReturn.adjustedAmount",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(adjustedAmount).replace("-", "&minus;")))
          .withCssClass("total sdil-right-align--desktop govuk-!-font-weight-bold")
      ))
    )
  }

  def subheader(total: BigDecimal)(implicit messages: Messages):String = {
    if (total < 0) {
      Messages("yourSoftDrinksLevyAccountsWillBeCredited", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total * -1).replace("-", "&minus;"))
    } else {
      Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total))
    }
  }
}

