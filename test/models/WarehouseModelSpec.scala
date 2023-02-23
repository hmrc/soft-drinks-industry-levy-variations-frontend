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
import org.scalatestplus.mockito.MockitoSugar

class WarehouseModelSpec  extends SpecBase with MockitoSugar with DataHelper {

  "Warehouse" - {
    "nonEmptyLines returns all the non empty lines of the Warehouse including Trading name" in {
      val data = testWarehouse(address = testAddress())
      data.nonEmptyLines mustBe List("test trading name", "test line 1", "test line 2", "test line 3", "test line 4", "AA111AA")
    }

    "nonEmptyLines returns all the non empty lines of the Warehouse excluding lines of the address that are empty" in {
      val data = testWarehouse(address = testAddress(line3 = "", line4 = ""))
      data.nonEmptyLines mustBe List("test trading name", "test line 1", "test line 2", "AA111AA")
    }

    "fromSite" in {
      val data = testSite(address = testUkAddress(lines = List("test line 1","test line 2","test line 3","test line 4")),
        tradingName = Some("test trading name"))
      Warehouse.fromSite(data) mustBe Warehouse("test trading name", Address("test line 1", "test line 2", "test line 3", "test line 4", "AA111AA"))
    }
  }

}
