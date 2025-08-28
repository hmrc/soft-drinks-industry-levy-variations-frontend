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

class ThirdPartyPackagersSummarySpec extends SpecBase {

  "row" - {

    "should return nothing when no ThirdPartyPackagers answers are passed in" in {
      val thirdPartyPackagersSummaryRow = ThirdPartyPackagersSummary.row(emptyUserAnswersForChangeActivity, isCheckAnswers = true)

      thirdPartyPackagersSummaryRow mustBe None
    }

    "should return a summary list row with the selected answer if thirdPartyPackagers page has been answered" in {
      val userAnswersWithThirdPartyPackagers = UserAnswers(sdilNumber, SelectChange.ChangeActivity, Json.obj(
        "changeActivity" -> Json.obj("thirdPartyPackagers" -> true)), contactAddress = contactAddress)

      val thirdPartyPackagersSummaryRow = ThirdPartyPackagersSummary.row(userAnswersWithThirdPartyPackagers, isCheckAnswers = true)

      thirdPartyPackagersSummaryRow.head.key.content.asHtml.toString mustBe "Use third party packagers?"
      thirdPartyPackagersSummaryRow.head.value.content.asHtml.toString mustBe "Yes"
      thirdPartyPackagersSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}
