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
import pages.correctReturn.{ BroughtIntoUkFromSmallProducersPage, HowManyBroughtIntoUkFromSmallProducersPage }
import play.twirl.api.Html

class BroughtIntoUkFromSmallProducersSummarySpec extends SpecBase {

  "BroughtIntoUkFromSmallProducersSummary" - {
    val lowLitres = 1000L
    val highLitres = 2000L

    val levyCalc = LevyCalculationTestHelper.levyCalculation(BigDecimal("180.00"), BigDecimal("480.00"))
    val levyCalculations = Map((lowLitres, highLitres) -> levyCalc)

    "must show Yes with litres and zero levy rows when reporting brought into UK from small producers" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUkFromSmallProducersPage, true)
        .success
        .value
        .set(HowManyBroughtIntoUkFromSmallProducersPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = BroughtIntoUkFromSmallProducersSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows(2).value.content.asHtml mustBe Html("£0.00")
      res.rows(4).value.content.asHtml mustBe Html("£0.00")
      res.rows.size mustBe 5
    }

    "must show No when not reporting brought into UK from small producers" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(BroughtIntoUkFromSmallProducersPage, false)
        .success
        .value

      val res = BroughtIntoUkFromSmallProducersSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.size mustBe 1
    }

    "must return empty when page not answered" in {
      val res = BroughtIntoUkFromSmallProducersSummary
        .summaryListWithBandLevyRows(emptyUserAnswersForCorrectReturn, isCheckAnswers = true, levyCalculations)
      res.rows.size mustBe 0
    }
  }
}
