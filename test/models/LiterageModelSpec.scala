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
import models.submission.Litreage
import org.scalatestplus.mockito.MockitoSugar

class LiterageModelSpec extends SpecBase with MockitoSugar {

  "Litreage.total" - {
    "should total with equal the atLowRate plus the atHighRate" in {
      val data: Litreage = Litreage(100, 100)
      data.total mustBe 200
    }
  }

  "Litreage.combineN" - {
    "should muliply a litreage by n" in {
      val data: Litreage = Litreage(100, 200)
      val n = 4
      data.combineN(n) mustBe Litreage(400, 800)
    }
  }

  "Litreage.fromLitresInBands" - {
    "should convert a litresInBands to literage" in {
      val litresInBands = LitresInBands(100, 200)
      Litreage.fromLitresInBands(litresInBands) mustBe Litreage(100,200)
    }
  }

  "Litreage.sum" - {
    "should sum a list of litreage" in {
      val litreage1 = Litreage(100, 200)
      val litreage2 = Litreage(200, 400)
      val litreage3 = Litreage(50, 100)

      val litreageList = List(litreage1, litreage2, litreage3)

      Litreage.sum(litreageList) mustBe Litreage(350, 700)
    }
  }
}
