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

class SmallProducerModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "SmallProducer" - {
    "getNameAndRef returns the alias plus </br> and the sdil ref" in {
      val data = testSmallProducer(alias = "test alias", sdilRef = "testRef", litreage = Litreage(15, 15))
      data.getNameAndRef mustBe "test alias</br>testRef"
    }

    "getNameAndRef returns the sdil ref only if alias is empty" in {
      val data = testSmallProducer(alias = "", sdilRef = "testRef", litreage = Litreage(15, 15))
      data.getNameAndRef mustBe "testRef"
    }
    "totalOfAllSmallProducers returns the total of one smallProducers literages" in {
      val data = List(testSmallProducer("test", "test", Litreage(15, 14)))
      SmallProducer.totalOfAllSmallProducers(data) mustBe Litreage(15, 14)
    }

    "totalOfAllSmallProducers returns the total of all smallProducers literages" in {
      val data =
        List(testSmallProducer("test", "test", Litreage(15, 14)), testSmallProducer("test", "test", Litreage(15, 14)))
      SmallProducer.totalOfAllSmallProducers(data) mustBe Litreage(30, 28)
    }
  }

}
