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

package models

import base.SpecBase
import models.backend.Site
import org.scalatestplus.mockito.MockitoSugar

class SiteModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "Site" - {
    "getLines returns a list the ukAddresses lines and postcode as one list" in {
      val data = testSite(
        address = testUkAddress(lines = List("test line 1","test line 2","test line 3","test line 4"))
      )
      data.getLines mustBe  List("test line 1", "test line 2", "test line 3", "test line 4", "AA111AA")
    }

    "fromAddress returns a Site with the address converted into a UK address" in {
      val address = testAddress()
      val expectedResult = testSite(address = testUkAddress(lines = List("test line 1","test line 2","test line 3","test line 4")))
      Site.fromAddress(address) mustBe expectedResult
    }

    "fromWarehouse returns a Site with the address and trading name from the warehouse provided" in {
      val warehouse = testWarehouse(address = testAddress())
      val expectedResult = testSite(
        address = testUkAddress(lines = List("test line 1", "test line 2", "test line 3", "test line 4")),
        tradingName = Some("test trading name")
      )
      Site.fromWarehouse(warehouse) mustBe expectedResult
    }
  }

}
