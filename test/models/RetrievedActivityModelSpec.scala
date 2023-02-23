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

class RetrievedActivityModelSpec extends SpecBase with MockitoSugar with DataHelper {

  "RetrievedActivity" - {
    "isLiable returns true if not a small producer and is a large producer" in {
      val data = testRetrievedActivity(largeProducer = true)
      data.isLiable mustBe true
    }

    "isLiable returns true if not a small producer and is a contractPackerr" in {
      val data = testRetrievedActivity(contractPacker = true)
      data.isLiable mustBe true
    }

    "isLiable returns true if not a small producer and is a importer" in {
      val data = testRetrievedActivity(importer = true)
      data.isLiable mustBe true
    }

    "isLiable returns false if small producer" in {
      val data = testRetrievedActivity(smallProducer = true)
      data.isLiable mustBe false
    }

    "isLiable returns false if all parts are false" in {
      val data = testRetrievedActivity()
      data.isLiable mustBe false
    }

    "isVoluntaryMandatory returns true if a small producer and contractPacker" in {
      val data = testRetrievedActivity(smallProducer = true, contractPacker = true)
      data.isVoluntaryMandatory mustBe true
    }

    "isVoluntaryMandatory returns true if a small producer and importer" in {
      val data = testRetrievedActivity(smallProducer = true, importer = true)
      data.isVoluntaryMandatory mustBe true
    }

    "isVoluntaryMandatory returns false if a small producer contractPacker and importer is false" in {
      val data = testRetrievedActivity(smallProducer = true)
      data.isVoluntaryMandatory mustBe false
    }

    "isVoluntaryMandatory returns false if not a small producer contractPacker and importer is true" in {
      val data = testRetrievedActivity(contractPacker = true, importer = true)
      data.isVoluntaryMandatory mustBe false
    }
  }

}
