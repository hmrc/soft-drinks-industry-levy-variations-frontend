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

package views.summary.changeActivity

import base.SpecBase
import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import views.summary.changeActivity.AmountProducedSummary

class AmountProducedSummarySpec extends SpecBase {

  "row" - {

    "should return nothing when no Amount Produced answers are passed in" in {
      val amountProducedSummaryRow = AmountProducedSummary.row(emptyUserAnswersForChangeActivity, isCheckAnswers = true)

      amountProducedSummaryRow mustBe None
    }

    "should return a summary list row with the selected answer if amountProduced page has been answered" in {
      val userAnswersWithAmountProduced = UserAnswers(sdilNumber, SelectChange.ChangeActivity, Json.obj(
        "changeActivity" -> Json.obj("amountProduced" -> "large")), contactAddress = contactAddress)

      val amountProducedSummaryRow = AmountProducedSummary.row(userAnswersWithAmountProduced, isCheckAnswers = true)

      amountProducedSummaryRow.head.key.content.asHtml.toString mustBe "Own brand packaged in past 12 months?"
      amountProducedSummaryRow.head.value.content.asHtml.toString mustBe "1 million litres or more"
      amountProducedSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}
