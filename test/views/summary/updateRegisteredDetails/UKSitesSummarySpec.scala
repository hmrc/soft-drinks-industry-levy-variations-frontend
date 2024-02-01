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
import models.NormalMode
import models.backend.{Site, UkAddress}

import java.time.LocalDate

class UKSitesSummarySpec extends SpecBase {
  lazy val pSite: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("Wild Lemonade Group"),
    Some("88"),
    Some(LocalDate.of(2018, 2, 26)))
  lazy val pSite2: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("88"),
    Some(LocalDate.of(2018, 2, 26)))
  lazy val packagingSites2: Map[String, Site] = Map("000001" -> pSite, "00002" -> pSite2)
  lazy val packagingSites1: Map[String, Site] = Map("00213" -> pSite)
  lazy val packagingSites3: Map[String, Site] = Map("000001" -> pSite, "00002" -> pSite2, "00213" -> pSite)

  lazy val warehouse1: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("Wild Lemonade Group"),
    Some("88"),
    Some(LocalDate.of(2018, 2, 26)))
  lazy val warehouse2: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("88"),
    Some(LocalDate.of(2018, 2, 26)))
  lazy val warehouses2: Map[String, Site] = Map("000001" -> warehouse1, "00002" -> warehouse2)
  lazy val warehouses1: Map[String, Site] = Map("00213" -> warehouse1)
  lazy val warehouses3: Map[String, Site] = Map("000001" -> warehouse1, "00002" -> warehouse2, "00213" -> warehouse1)

  lazy val packagingSitesValues = List(Map.empty[String, Site], packagingSites1, packagingSites2, packagingSites3)
  lazy val warehousesValues = List(Map.empty[String, Site], warehouses1, warehouses2, warehouses3)

  "getHeadingAndSummary" - {
    List(true, false).foreach(isCheckAnswers => {
      s"should return the numbers of packaging sites and warehouses with ${if (isCheckAnswers) "a Change link action" else "no actions"} " +
        s"when isCheckAnswers $isCheckAnswers" - {
        packagingSitesValues.foreach(packagingSites => {
          warehousesValues.foreach(warehouses => {
            s"when ${packagingSites.size} packaging sites and ${warehouses.size} warehouses" in {
              val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(
                packagingSiteList = packagingSites, warehouseList = warehouses
              )
              val ukSitesSummary = UKSitesSummary.getHeadingAndSummary(userAnswers, isCheckAnswers)
              ukSitesSummary.head._2.rows.size mustEqual 2
              ukSitesSummary.head._2.rows(0).key.content.asHtml.toString mustBe
                s"You have ${packagingSites.size} packaging site${if (packagingSites.size != 1) "s" else ""}"
              ukSitesSummary.head._2.rows(1).key.content.asHtml.toString mustBe
                s"You have ${warehouses.size} warehouse${if (warehouses.size != 1) "s" else ""}"
              if (isCheckAnswers) {
                ukSitesSummary.head._2.rows(0).actions.toList.head.items.head.content.asHtml.toString mustBe "Change"
                ukSitesSummary.head._2.rows(1).actions.toList.head.items.head.content.asHtml.toString mustBe "Change"
              } else {
                ukSitesSummary.head._2.rows(0).actions.head.items.isEmpty mustBe true
                ukSitesSummary.head._2.rows(1).actions.head.items.isEmpty mustBe true
              }
            }
          })
        })
      }
    })
  }

}
