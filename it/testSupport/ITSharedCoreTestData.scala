/*
 * Copyright 2026 HM Revenue & Customs
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

package testSupport

import models.SmallProducer
import models.backend.{ Site, UkAddress }
import models.submission.Litreage

trait ITSharedCoreTestData {
  val ukAddress: UkAddress = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX")
  val packagingSitesFromSubscription: Map[String, Site] = Map(
    "0" -> Site(
      UkAddress(
        List("105B Godfrey Marchant Grove", "Guildford"),
        "GU14 8NL",
        None
      ),
      Some("96"),
      Some("Star Products Ltd"),
      None
    )
  )
  val warehousesFromSubscription: Map[String, Site] = Map(
    "0"      -> Site(UkAddress(List("33 Rhes Priordy"), "WR53 7CX"), Some("ABC Ltd")),
    "123456" -> Site(UkAddress(List("30 Main Street"), "WR53 7CX"), Some("XYZ Ltd"))
  )

  val smallProducersAddedList: List[SmallProducer] = List(
    SmallProducer("Star Products Ltd", "96", Litreage(1000L, 2000L)),
    SmallProducer("Super Star cola Ltd", "97", Litreage(1000L, 2000L))
  )

}
