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

class AddressModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "Address" - {
    "fromString returns an address from a string split" in {
      val addressString = "test line 1,test line 2,test line 3,test line 4,AA111AA"
      val expectedAddress = testAddress()
      Address.fromString(addressString) mustBe expectedAddress
    }

    "fromUkAddress returns an address from a UK Address" in {
      val addressString = UkAddress(List("test line 1","test line 2","test line 3","test line 4"),"AA111AA")
      val expectedAddress = testAddress()
      Address.fromUkAddress(addressString) mustBe expectedAddress
    }
  }

}
