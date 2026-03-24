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
import pages.correctReturn.{ ClaimCreditsForLostDamagedPage, HowManyCreditsForLostDamagedPage }
import play.twirl.api.Html

class ClaimCreditsForLostDamagedSummarySpec extends SpecBase {

  "ClaimCreditsForLostDestroyedSummary" - {
    val lowLitres = 1000L
    val highLitres = 2000L

    val levyCalc = LevyCalculationTestHelper.levyCalculation(BigDecimal("180.00"), BigDecimal("480.00"))
    val levyCalculations = Map((lowLitres, highLitres) -> levyCalc)

    "must show Yes with litres and negative levy rows when claiming lost or destroyed credits" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(ClaimCreditsForLostDamagedPage, true)
        .success
        .value
        .set(HowManyCreditsForLostDamagedPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = ClaimCreditsForLostDestroyedSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.key.content.asHtml mustBe Html("Claiming credits for lost or destroyed liable drinks?")
      res.rows(2).key.content.asHtml mustBe Html("Low band levy")
      res.rows.size mustBe 5
    }

    "must show No when not claiming lost or destroyed credits" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(ClaimCreditsForLostDamagedPage, false)
        .success
        .value

      val res = ClaimCreditsForLostDestroyedSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.size mustBe 1
    }

    "must return empty when page not answered" in {
      val res = ClaimCreditsForLostDestroyedSummary
        .summaryListWithBandLevyRows(emptyUserAnswersForCorrectReturn, isCheckAnswers = true, levyCalculations)
      res.rows.size mustBe 0
    }
  }
}
