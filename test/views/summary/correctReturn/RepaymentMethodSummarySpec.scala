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

import base.SpecBase
import models.{SelectChange, UserAnswers}
import pages.correctReturn.RepaymentMethodPage
import play.api.libs.json.Json

class RepaymentMethodSummarySpec extends SpecBase {

  "row" - {

    s"should return nothing when the $RepaymentMethodPage has not been answered" in {
      val repaymentMethodSummaryRow = RepaymentMethodSummary.row(emptyUserAnswersForCorrectReturn)

      repaymentMethodSummaryRow mustBe None
    }

    "should return a summary list row with credit to SDIL acct repayment method if this answer has been added" in {
      val userAnswersWithRepaymentMethod = UserAnswers(sdilNumber, SelectChange.CorrectReturn,
        Json.obj("correctReturn" -> Json.obj("repaymentMethod" -> "sdilAccount")), contactAddress = contactAddress)

      val repaymentMethodSummaryRow = RepaymentMethodSummary.row(userAnswersWithRepaymentMethod)

      repaymentMethodSummaryRow.head.key.content.asHtml.toString mustBe "Repayment method"
      repaymentMethodSummaryRow.head.value.content.asHtml.toString mustBe "Credited to your Soft Drinks Industry Levy account"
      repaymentMethodSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }

    "should return a summary list row with deposit to bank acct repayment method if this answer has been added" in {
      val userAnswersWithRepaymentMethod = UserAnswers(sdilNumber, SelectChange.CorrectReturn,
        Json.obj("correctReturn" -> Json.obj("repaymentMethod" -> "bankAccount")), contactAddress = contactAddress)

      val repaymentMethodSummaryRow = RepaymentMethodSummary.row(userAnswersWithRepaymentMethod)

      repaymentMethodSummaryRow.head.key.content.asHtml.toString mustBe "Repayment method"
      repaymentMethodSummaryRow.head.value.content.asHtml.toString mustBe "Paid into the bank account for this business"
      repaymentMethodSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}
