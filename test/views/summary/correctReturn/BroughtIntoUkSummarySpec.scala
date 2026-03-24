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

package views.summary.correctReturn

import base.{ LevyCalculationTestHelper, SpecBase }
import models.LitresInBands
import pages.correctReturn.{ BroughtIntoUKPage, HowManyBroughtIntoUKPage }
import play.twirl.api.Html

class BroughtIntoUKSummarySpec extends SpecBase {

  "BroughtIntoUKSummary" - {
    val lowLitres = 1000L
    val highLitres = 2000L

    val levyCalc = LevyCalculationTestHelper.levyCalculation(BigDecimal("180.00"), BigDecimal("480.00"))
    val levyCalculations = Map((lowLitres, highLitres) -> levyCalc)

    "must show Yes with litres and levy rows when reporting brought into UK with check answers" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUKPage, true)
        .success
        .value
        .set(HowManyBroughtIntoUKPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows(2).value.content.asHtml mustBe Html("£180.00")
      res.rows(4).value.content.asHtml mustBe Html("£480.00")
      res.rows.size mustBe 5
    }

    "must show Yes with litres and levy rows without change links when not check answers" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUKPage, true)
        .success
        .value
        .set(HowManyBroughtIntoUKPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = false, levyCalculations)
      res.rows(1).actions mustBe None
      res.rows.size mustBe 5
    }

    "must show No with no litres rows when not reporting brought into UK" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUKPage, false)
        .success
        .value

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.size mustBe 1
    }

    "must return empty when page not answered" in {
      val res = BroughtIntoUKSummary
        .summaryListWithBandLevyRows(emptyUserAnswersForCorrectReturn, isCheckAnswers = true, levyCalculations)
      res.rows.size mustBe 0
    }
  }
}
