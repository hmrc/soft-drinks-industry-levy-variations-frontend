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

class LiterageModelSpec extends SpecBase with MockitoSugar with DataHelper {

    "Litreage" - {
      "total with equal the atLowRate plus the atHighRate" in {
        val data: Litreage = testLiterage(atLowRate = 100, atHighRate = 100)
        data.total mustBe 200
      }
    }
}
