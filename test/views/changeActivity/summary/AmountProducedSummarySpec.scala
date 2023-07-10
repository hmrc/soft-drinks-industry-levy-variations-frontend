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

package views.changeActivity.summary

import base.SpecBase
import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import viewmodels.summary.changeActivity.AmountProducedSummary

class AmountProducedSummarySpec extends SpecBase {

  "row" - {

    "should return nothing when no Amount Produced answers are passed in" in {
      val amountProducedSummaryRow = AmountProducedSummary.row(emptyUserAnswersForChangeActivity)

      amountProducedSummaryRow mustBe None
    }

    "should return a summary list row with the selected answer if amountProduced page has been answered" in {
      val userAnswersWithAmountProduced = UserAnswers(sdilNumber, SelectChange.ChangeActivity, Json.obj(
        "changeActivity" -> Json.obj("amountProduced" -> "large")))

      val amountProducedSummaryRow = AmountProducedSummary.row(userAnswersWithAmountProduced)

      amountProducedSummaryRow.head.key.content.asHtml.toString mustBe "Amount Produced"
      amountProducedSummaryRow.head.value.content.asHtml.toString mustBe "1 million litres or more"
      amountProducedSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}
