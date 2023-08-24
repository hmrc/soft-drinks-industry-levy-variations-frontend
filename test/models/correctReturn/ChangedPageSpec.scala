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

import models.{SdilReturn, SmallProducer}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.correctReturn._

class ChangedPageSpec extends AnyFreeSpec with Matchers {
  "returnLiteragePagesThatChangedComparedToOriginalReturn" - {
    "should return all pages with answers changed true that have changed if all answers have changed" in {
      val originalSdilReturn = SdilReturn((0, 0), (0, 0), List(), (0, 0), (0, 0), (0, 0), (0, 0), None)
      val currentSdilReturn = SdilReturn((1, 1), (3, 4), List(SmallProducer("", "", (1, 1))), (5, 6), (33, 22), (32, 22), (22, 22), None)
      val res = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSdilReturn)
      res mustBe List(
        ChangedPage(OperatePackagingSiteOwnBrandsPage, true),
        ChangedPage(HowManyOperatePackagingSiteOwnBrandsPage, true),
        ChangedPage(PackagedAsContractPackerPage, true),
        ChangedPage(HowManyPackagedAsContractPackerPage, true),
        ChangedPage(BroughtIntoUKPage, true),
        ChangedPage(HowManyBroughtIntoUKPage, true),
        ChangedPage(BroughtIntoUkFromSmallProducersPage, true),
        ChangedPage(HowManyBroughtIntoUkFromSmallProducersPage, true),
        ChangedPage(ClaimCreditsForExportsPage, true),
        ChangedPage(HowManyClaimCreditsForExportsPage, true),
        ChangedPage(ExemptionsForSmallProducersPage, true)
      )
    }
    "should return all pages with answers changed false that have changed if all answers remain the same" in {
      val originalSdilReturn = SdilReturn((0, 0), (0, 0), List(), (0, 0), (0, 0), (0, 0), (0, 0), None)
      val currentSdilReturn = SdilReturn((0, 0), (0, 0), List(), (0, 0), (0, 0), (0, 0), (0, 0), None)
      val res = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSdilReturn)
      res mustBe List(
        ChangedPage(OperatePackagingSiteOwnBrandsPage, false),
        ChangedPage(HowManyOperatePackagingSiteOwnBrandsPage, false),
        ChangedPage(PackagedAsContractPackerPage, false),
        ChangedPage(HowManyPackagedAsContractPackerPage, false),
        ChangedPage(BroughtIntoUKPage, false),
        ChangedPage(HowManyBroughtIntoUKPage, false),
        ChangedPage(BroughtIntoUkFromSmallProducersPage, false),
        ChangedPage(HowManyBroughtIntoUkFromSmallProducersPage, false),
        ChangedPage(ClaimCreditsForExportsPage, false),
        ChangedPage(HowManyClaimCreditsForExportsPage, false),
        ChangedPage(ExemptionsForSmallProducersPage, false)
      )
    }
    s"should return all pages answered change true bar the $ExemptionsForSmallProducersPage if the small producers literages has not changed" in {
      val originalSdilReturn = SdilReturn((0, 0), (0, 0), List(SmallProducer("", "", (1, 1))), (0, 0), (0, 0), (0, 0), (0, 0), None)
      val currentSdilReturn = SdilReturn((1, 1), (3, 4), List(SmallProducer("", "", (1, 1))), (5, 6), (33, 22), (32, 22), (22, 22), None)
      val res = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSdilReturn)
       res mustBe List(
        ChangedPage(OperatePackagingSiteOwnBrandsPage, true),
        ChangedPage(HowManyOperatePackagingSiteOwnBrandsPage, true),
        ChangedPage(PackagedAsContractPackerPage, true),
        ChangedPage(HowManyPackagedAsContractPackerPage, true),
        ChangedPage(BroughtIntoUKPage, true),
        ChangedPage(HowManyBroughtIntoUKPage, true),
        ChangedPage(BroughtIntoUkFromSmallProducersPage, true),
        ChangedPage(HowManyBroughtIntoUkFromSmallProducersPage, true),
        ChangedPage(ClaimCreditsForExportsPage, true),
        ChangedPage(HowManyClaimCreditsForExportsPage, true),
        ChangedPage(ExemptionsForSmallProducersPage, false)
      )
    }
  }
}
