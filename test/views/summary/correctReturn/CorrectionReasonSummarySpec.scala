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
import play.api.libs.json.Json

class CorrectionReasonSummarySpec extends SpecBase {

  "row" - {

    "should return nothing when the reason question page has not been answered" in {
      val correctionReasonSummaryRow = CorrectionReasonSummary.row(emptyUserAnswersForCorrectReturn)

      correctionReasonSummaryRow mustBe None
    }

    "should return a summary list row with the appropriate cancellation reason if an answer has been added" in {
      val userAnswersWithCorrectionReason = UserAnswers(sdilNumber, SelectChange.CorrectReturn,
        Json.obj("correctReturn" -> Json.obj("correctionReason" -> "I was not paying close enough attention and I entered the wrong value")))

      val correctionReasonSummaryRow = CorrectionReasonSummary.row(userAnswersWithCorrectionReason)

      correctionReasonSummaryRow.head.key.content.asHtml.toString mustBe "Reason for correcting this return"
      correctionReasonSummaryRow.head.value.content.asHtml.toString mustBe "I was not paying close enough attention and I entered the wrong value"
      correctionReasonSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}
