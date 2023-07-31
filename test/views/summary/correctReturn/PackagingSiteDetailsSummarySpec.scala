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
import models.SmallProducer
import models.backend.{Site, UkAddress}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList

import java.time.LocalDate

class PackagingSiteDetailsSummarySpec extends SpecBase {
  lazy val pSite: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("88"),
    Some("Wild Lemonade Group"),
    Some(LocalDate.of(2018, 2, 26)))
  lazy val pSite2: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("88"),
    None,
    Some(LocalDate.of(2018, 2, 26)))
  lazy val packagingSites2: Map[String, Site] = Map("000001" -> pSite, "00002" -> pSite2)
  lazy val packagingSite1: Map[String, Site] = Map("00213" -> pSite)
  lazy val packagingSites3: Map[String, Site] = Map("000001" -> pSite, "00002" -> pSite2, "00213" -> pSite)

  "row" - {

    "should return nothing when the list is empty" in {
      val packagingSiteDetailsSummaryRow = PackagingSiteDetailsSummary.row(emptyUserAnswersForCorrectReturn)

      packagingSiteDetailsSummaryRow mustBe None
    }

    "should return a summary list row with the correct information and action links" in {
      val userAnswersWithProductionSites = emptyUserAnswersForCorrectReturn.copy(packagingSiteList = packagingSites2)

      val packagingSiteDetailsSummaryRow = PackagingSiteDetailsSummary.row2(packagingSites2)
      val rowActionListItems = packagingSiteDetailsSummaryRow.head.actions.toList.head.items

      packagingSiteDetailsSummaryRow.head.key.content.asHtml.toString mustBe "Wild Lemonade Group<br>33 Rhes Priordy, East London<br>E73 2RP"

      rowActionListItems.size mustBe 1
      rowActionListItems.head.content.asHtml.toString mustBe "Remove"
    }
  }

  "row should contain the same number of rows as the number of small producers" - {
    "should have 2 rows" in {
      val packagingSiteDetailsSummaryRow = PackagingSiteDetailsSummary.row2(packagingSites2)

      packagingSiteDetailsSummaryRow.size mustBe 2
    }

    "should have 1 row" in {
      val packagingSiteDetailsSummaryRow = PackagingSiteDetailsSummary.row2(packagingSite1)

      packagingSiteDetailsSummaryRow.size mustBe 1
    }

    "should have 3 rows" in {
      val packagingSiteDetailsSummaryRow = PackagingSiteDetailsSummary.row2(packagingSites3)

      packagingSiteDetailsSummaryRow.size mustBe 3
    }
  }

}
