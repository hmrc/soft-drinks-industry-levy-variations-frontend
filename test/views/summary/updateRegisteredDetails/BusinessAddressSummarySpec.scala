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

package views.summary.updateRegisteredDetails

import base.SpecBase
import models.backend.UkAddress

class BusinessAddressSummarySpec extends SpecBase {
  lazy val businessAddress: UkAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")

  "row" - {

    "should return a summary list row with the correct information with action links when isCheckAnswers is true" in {
      val businessAddressSummaryRow = BusinessAddressSummary.row(List(businessAddress))
      val rowActionListItems = businessAddressSummaryRow.head.actions.toList.head.items

      businessAddressSummaryRow.head.key.content.asHtml.toString mustBe "33 Rhes Priordy, East London<br>E73 2RP"

      rowActionListItems.size mustBe 1
      rowActionListItems.head.content.asHtml.toString mustBe "Change"
    }

    "should return a summary list row with the correct information without action links when isCheckAnswers is false" in {
      val businessAddressSummaryRow = BusinessAddressSummary.row(List(businessAddress), isCheckAnswers = false)
      val rowActionListItems = businessAddressSummaryRow.head.actions.toList

      businessAddressSummaryRow.head.value.content.asHtml.toString mustBe "33 Rhes Priordy, East London<br>E73 2RP"

      rowActionListItems.size mustBe 0
    }
  }

}
