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

package views.summary.cancelRegistration

import models.{SelectChange, UserAnswers}
import viewmodels.summary.cancelRegistration.ReasonSummary
import base.SpecBase
import play.api.libs.json.Json

class ReasonSummarySpec extends SpecBase {

  "row" - {
    "should return a summary list row with the appropriate cancellation reason if an answer has been added" in {
      val userAnswersWithCancelReason = UserAnswers(sdilNumber, SelectChange.CancelRegistration,
        Json.obj("cancelRegistration" -> Json.obj("reason" -> "incorrectly registered")), contactAddress = contactAddress)

      val cancelReasonSummaryRow = ReasonSummary.row(userAnswersWithCancelReason)

      cancelReasonSummaryRow.key.content.asHtml.toString mustBe "Reason for cancelling"
      cancelReasonSummaryRow.value.content.asHtml.toString mustBe "incorrectly registered"
      cancelReasonSummaryRow.actions.toList.head.items.head.content.asHtml.toString() must include("Change")

    }
  }
}
