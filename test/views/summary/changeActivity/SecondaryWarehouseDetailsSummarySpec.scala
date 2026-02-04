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
import models.NormalMode
import models.backend.{ Site, UkAddress }

import java.time.LocalDate

class SecondaryWarehouseDetailsSummarySpec extends SpecBase {
  lazy val warehouse1: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("Wild Lemonade Group"),
    Some("88"),
    Some(LocalDate.of(2018, 2, 26))
  )
  lazy val warehouse2: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("88"),
    Some(LocalDate.of(2018, 2, 26))
  )
  lazy val warehouses2: Map[String, Site] = Map("000001" -> warehouse1, "00002" -> warehouse2)
  lazy val warehouses1: Map[String, Site] = Map("00213" -> warehouse1)
  lazy val warehouses3: Map[String, Site] = Map("000001" -> warehouse1, "00002" -> warehouse2, "00213" -> warehouse1)

  "summaryRows" - {

    "should return a summary list row with the correct information and action links" in {

      val warehouseDetailsSummaryRow = SecondaryWarehouseDetailsSummary.summaryRows(warehouses2, NormalMode)
      val rowActionListItems = warehouseDetailsSummaryRow.head.actions.toList.head.items

      warehouseDetailsSummaryRow.head.key.content.asHtml.toString mustBe "Wild Lemonade Group<br>33 Rhes Priordy, East London<br>E73 2RP"

      rowActionListItems.size mustBe 1
      rowActionListItems.head.content.asHtml.toString mustBe "Remove"
    }
  }

  "row should contain the same number of rows as the number of small producers" - {
    "should have 2 rows" in {
      val warehouseDetailsSummaryRow = SecondaryWarehouseDetailsSummary.summaryRows(warehouses2, NormalMode)

      warehouseDetailsSummaryRow.size mustBe 2
    }

    "should have 1 row" in {
      val warehouseDetailsSummaryRow = SecondaryWarehouseDetailsSummary.summaryRows(warehouses1, NormalMode)

      warehouseDetailsSummaryRow.size mustBe 1
    }

    "should have 3 rows" in {
      val warehouseDetailsSummaryRow = SecondaryWarehouseDetailsSummary.summaryRows(warehouses3, NormalMode)

      warehouseDetailsSummaryRow.size mustBe 3
    }
  }

}
