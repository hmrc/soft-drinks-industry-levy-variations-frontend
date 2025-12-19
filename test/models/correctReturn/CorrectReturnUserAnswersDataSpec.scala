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

package models.correctReturn

import models.submission.Litreage
import models.{ LitresInBands, SdilReturn, SmallProducer }
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CorrectReturnUserAnswersDataSpec extends AnyFreeSpec with Matchers {

  "CorrectReturnUserAnswersData" - {
    val correctReturnDataAllNo =
      CorrectReturnUserAnswersData(false, None, false, None, false, false, None, false, None, false, None, false, None)
    val litresInBands = LitresInBands(100, 200)
    val correctReturnDataAllYes = CorrectReturnUserAnswersData(
      true,
      Some(litresInBands),
      true,
      Some(litresInBands),
      true,
      true,
      Some(litresInBands),
      true,
      Some(litresInBands),
      true,
      Some(litresInBands),
      true,
      Some(litresInBands)
    )
    val litreage0 = Litreage(0, 0)
    val litreageOneItem = Litreage(100, 200)
    val smallProducer = SmallProducer("", "", litreageOneItem)

    "fromSdilReturn" - {
      "when the sdilReturn has all 0 values" - {
        "must return a correctReturnData model with None LitresInBands and all false" in {
          val sdilReturn = SdilReturn(litreage0, litreage0, List(), litreage0, litreage0, litreage0, litreage0, None)

          val res = CorrectReturnUserAnswersData.fromSdilReturn(sdilReturn)
          res mustBe correctReturnDataAllNo
        }
      }

      "when the sdilReturn has all litreage values" - {
        "must return a correctReturnData model with None LitresInBands and all false" in {
          val sdilReturn = SdilReturn(
            litreageOneItem,
            litreageOneItem,
            List(smallProducer),
            litreageOneItem,
            litreageOneItem,
            litreageOneItem,
            litreageOneItem,
            None
          )

          val res = CorrectReturnUserAnswersData.fromSdilReturn(sdilReturn)
          res mustBe correctReturnDataAllYes
        }
      }
    }
    "when all answers are no" - {
      "should return 0 litreage for total imported" in {
        correctReturnDataAllNo.totalImported mustBe litreage0
      }

      "should return 0 litreage for total packed" in {
        correctReturnDataAllNo.totalPacked(List()) mustBe litreage0
      }

      "should return 0 litreage for ownBrandsLitreage" in {
        correctReturnDataAllNo.ownBrandsLitreage mustBe litreage0
      }

      "should return 0 litreage for contractPackerLitreage" in {
        correctReturnDataAllNo.contractPackerLitreage mustBe litreage0
      }

      "should return 0 litreage for broughtIntoUkLitreage" in {
        correctReturnDataAllNo.broughtIntoUkLitreage mustBe litreage0
      }

      "should return 0 litreage for broughtIntoUkFromSmallProducerLitreage" in {
        correctReturnDataAllNo.broughtIntoUkFromSmallProducerLitreage mustBe litreage0
      }

      "should return 0 litreage for exportsLitreage" in {
        correctReturnDataAllNo.exportsLitreage mustBe litreage0
      }

      "should return 0 litreage for lostDamagedLitreage" in {
        correctReturnDataAllNo.lostDamagedLitreage mustBe litreage0
      }
    }

    "when all answers are yes" - {
      "should return the litreage for total imported" in {
        correctReturnDataAllYes.totalImported mustBe Litreage(200, 400)
      }

      "should return the litreage for total packed" in {
        correctReturnDataAllYes.totalPacked(List(smallProducer)) mustBe Litreage(200, 400)
      }

      "should return the litreage for ownBrandsLitreage" in {
        correctReturnDataAllYes.ownBrandsLitreage mustBe litreageOneItem
      }

      "should return the litreage for contractPackerLitreage" in {
        correctReturnDataAllYes.contractPackerLitreage mustBe litreageOneItem
      }

      "should return the litreage for broughtIntoUkLitreage" in {
        correctReturnDataAllYes.broughtIntoUkLitreage mustBe litreageOneItem
      }

      "should return the litreage for broughtIntoUkFromSmallProducerLitreage" in {
        correctReturnDataAllYes.broughtIntoUkFromSmallProducerLitreage mustBe litreageOneItem
      }

      "should return the litreage for exportsLitreage" in {
        correctReturnDataAllYes.exportsLitreage mustBe litreageOneItem
      }

      "should return the litreage for lostDamagedLitreage" in {
        correctReturnDataAllYes.lostDamagedLitreage mustBe litreageOneItem
      }
    }
  }
}
