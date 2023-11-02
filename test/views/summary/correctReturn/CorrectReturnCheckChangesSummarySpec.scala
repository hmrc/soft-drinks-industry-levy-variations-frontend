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
import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.{LitresInBands, SmallProducer}
import pages.correctReturn._
import views.helpers.AmountToPaySummary

class CorrectReturnCheckChangesSummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000
    val litres = LitresInBands(lowLitres, highLitres)

    val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
      .copy(packagingSiteList = Map.empty, warehouseList = Map.empty,
        smallProducerList = List(SmallProducer("", "XZSDIL000000234", (2000, 4000))))
      .set(OperatePackagingSiteOwnBrandsPage, true).success.value
      .set(HowManyOperatePackagingSiteOwnBrandsPage, litres).success.value
      .set(PackagedAsContractPackerPage, true).success.value
      .set(HowManyPackagedAsContractPackerPage, litres).success.value
      .set(ExemptionsForSmallProducersPage, true).success.value
      .set(AddASmallProducerPage, AddASmallProducer(None, "XZSDIL000000234", litres)).success.value
      .set(BroughtIntoUKPage, true).success.value
      .set(HowManyBroughtIntoUKPage, litres).success.value
      .set(BroughtIntoUkFromSmallProducersPage, true).success.value
      .set(HowManyBroughtIntoUkFromSmallProducersPage, litres).success.value
      .set(ClaimCreditsForExportsPage, true).success.value
      .set(HowManyClaimCreditsForExportsPage, litres).success.value
      .set(ClaimCreditsForLostDamagedPage, true).success.value
      .set(HowManyCreditsForLostDamagedPage, litres).success.value
      .set(CorrectionReasonPage, "foo").success.value
      .set(RepaymentMethodPage, RepaymentMethod.values.head).success.value

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
      val correctionReasonSummaryRow = AmountToPaySummary.amountToPaySummary(amounts = amounts)

      correctionReasonSummaryRow.rows.head.key.content.asHtml.toString mustBe "Original return total"
      correctionReasonSummaryRow.rows.head.value.content.asHtml.toString mustBe "£0.00"

      correctionReasonSummaryRow.rows(1).key.content.asHtml.toString mustBe "New return total"
      correctionReasonSummaryRow.rows(1).value.content.asHtml.toString mustBe "£1,320.00"

      correctionReasonSummaryRow.rows(2).key.content.asHtml.toString mustBe "Account balance"
      correctionReasonSummaryRow.rows(2).value.content.asHtml.toString mustBe "£502.75"

      correctionReasonSummaryRow.rows(3).key.content.asHtml.toString mustBe "Net adjusted amount"
      correctionReasonSummaryRow.rows(3).value.content.asHtml.toString mustBe "£1,822.75"

      correctionReasonSummaryRow.rows.size mustBe 4
    }
  }
}
