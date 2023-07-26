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
import models.backend.UkAddress
import models.{SelectChange, SmallProducer, UserAnswers}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList

import java.time.Instant

class SmallProducerDetailsSummarySpec extends SpecBase {
  override val smallProducerList: List[SmallProducer] = List(SmallProducer("Super Cola Plc", "XCSDIL000000069", (20, 10)))

  "row" - {

    "should return nothing when the list is empty" in {
      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(List.empty)

      smallProducerDetailsSummaryRow mustBe SummaryList(List(), None, "", Map())
    }

    "should return a summary list row with the correct information and action links" in {
      val userAnswersWithSmallProducers = emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList)

      val smallProducerDetailsSummaryRow = SmallProducerDetailsSummary.producerList(userAnswersWithSmallProducers.smallProducerList)

      smallProducerDetailsSummaryRow.rows.head.key.content.asHtml.toString mustBe "XCSDIL000000069"
      smallProducerDetailsSummaryRow.rows.head.value.content.asHtml.toString mustBe "Super Cola Plc"
      smallProducerDetailsSummaryRow.rows.head.actions.toList.head.items.head.content.asHtml.toString() mustBe "Edit"
      smallProducerDetailsSummaryRow.rows.head.actions.toList.last.items.last.content.asHtml.toString() mustBe "Remove"
    }
  }
}
