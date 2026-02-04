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
import models.correctReturn.{ AddASmallProducer, RepaymentMethod }
import models.submission.Litreage
import models.{ LitresInBands, SmallProducer }
import pages.correctReturn._

class CorrectReturnCheckChangesSummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000
    val litres = LitresInBands(lowLitres, highLitres)

    val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
      .copy(
        packagingSiteList = Map.empty,
        warehouseList = Map.empty,
        smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000)))
      )
      .set(OperatePackagingSiteOwnBrandsPage, true)
      .success
      .value
      .set(HowManyOperatePackagingSiteOwnBrandsPage, litres)
      .success
      .value
      .set(PackagedAsContractPackerPage, true)
      .success
      .value
      .set(HowManyPackagedAsContractPackerPage, litres)
      .success
      .value
      .set(ExemptionsForSmallProducersPage, true)
      .success
      .value
      .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres))
      .success
      .value
      .set(BroughtIntoUKPage, true)
      .success
      .value
      .set(HowManyBroughtIntoUKPage, litres)
      .success
      .value
      .set(BroughtIntoUkFromSmallProducersPage, true)
      .success
      .value
      .set(HowManyBroughtIntoUkFromSmallProducersPage, litres)
      .success
      .value
      .set(ClaimCreditsForExportsPage, true)
      .success
      .value
      .set(HowManyClaimCreditsForExportsPage, litres)
      .success
      .value
      .set(ClaimCreditsForLostDamagedPage, true)
      .success
      .value
      .set(HowManyCreditsForLostDamagedPage, litres)
      .success
      .value
      .set(CorrectionReasonPage, "foo")
      .success
      .value
      .set(RepaymentMethodPage, RepaymentMethod.values.head)
      .success
      .value

    "should return a summary list row with the selected answer if Correction reason page has been answered" in {
      val correctionReasonSummaryRow = CorrectionReasonSummary.row(userAnswers)

      correctionReasonSummaryRow.head.key.content.asHtml.toString mustBe "Reason for correcting"
      correctionReasonSummaryRow.head.value.content.asHtml.toString mustBe "foo"
      correctionReasonSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }

    "should return a summary list row with credit to SDIL acct repayment method if this answer has been added" in {
      val repaymentMethodSummaryRow = RepaymentMethodSummary.row(userAnswers)

      repaymentMethodSummaryRow.head.key.content.asHtml.toString mustBe "Repayment method"
      repaymentMethodSummaryRow.head.value.content.asHtml.toString mustBe "Paid into the bank account for this business"
      repaymentMethodSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }

    "should return a summary list row with balance information" in {
      val balanceSummaryRow = AmountToPaySummary.amountToPayBalance(amounts = amounts)

      balanceSummaryRow.rows.head.key.content.asHtml.toString mustBe "Original return total"
      balanceSummaryRow.rows.head.value.content.asHtml.toString mustBe "£1,525.32"

      balanceSummaryRow.rows(1).key.content.asHtml.toString mustBe "New return total"
      balanceSummaryRow.rows(1).value.content.asHtml.toString mustBe "£1,320.00"

      balanceSummaryRow.rows(2).key.content.asHtml.toString mustBe "Account balance"
      balanceSummaryRow.rows(2).value.content.asHtml.toString mustBe "£502.75"

      balanceSummaryRow.rows(3).key.content.asHtml.toString mustBe "Net adjusted amount"
      balanceSummaryRow.rows(3).value.content.asHtml.toString mustBe "£297.43"

      balanceSummaryRow.rows.size mustBe 4
    }
  }
}
