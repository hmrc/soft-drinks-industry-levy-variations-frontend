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
import models.SmallProducer
import models.submission.Litreage
import pages.correctReturn.ExemptionsForSmallProducersPage
import play.twirl.api.Html

class ExemptionsForSmallProducersSummarySpec extends SpecBase {

  "ExemptionsForSmallProducersSummary" - {
    val levyCalc = LevyCalculationTestHelper.levyCalculation(BigDecimal("180.00"), BigDecimal("480.00"))
    val levyCalculations = Map((2000L, 4000L) -> levyCalc)

    "must show Yes with small producer litres and zero levy when exemptions selected" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(ExemptionsForSmallProducersPage, true)
        .success
        .value
        .copy(smallProducerList = List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000))))

      val res = ExemptionsForSmallProducersSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows(2).value.content.asHtml mustBe Html("£0.00")
      res.rows(4).value.content.asHtml mustBe Html("£0.00")
      res.rows.size mustBe 5
    }

    "must show No when exemptions not selected" in {
      val userAnswers = emptyUserAnswersForCorrectReturn
        .set(ExemptionsForSmallProducersPage, false)
        .success
        .value

      val res = ExemptionsForSmallProducersSummary
        .summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true, levyCalculations)
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.size mustBe 1
    }

    "must return empty when page not answered" in {
      val res = ExemptionsForSmallProducersSummary
        .summaryListWithBandLevyRows(emptyUserAnswersForCorrectReturn, isCheckAnswers = true, levyCalculations)
      res.rows.size mustBe 0
    }
  }
}
