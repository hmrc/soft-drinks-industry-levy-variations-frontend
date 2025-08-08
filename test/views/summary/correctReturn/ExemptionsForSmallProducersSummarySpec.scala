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

import base.SpecBase
import models.{CheckMode, LitresInBands, ReturnPeriod}
import pages.correctReturn.ExemptionsForSmallProducersPage
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import controllers.correctReturn.routes

class ExemptionsForSmallProducersSummarySpec extends SpecBase {

  "ExemptionsForSmallProducersSummary" - {

    val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
    val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

    val returnPeriodsWithLabels = List(
      (preApril2025ReturnPeriod, "- pre April 2025 rates"),
      (taxYear2025ReturnPeriod, "- 2025 tax year rates")
    )

    returnPeriodsWithLabels.foreach(returnPeriod => {
      s"must show correct row when Exemptions For Small Producers is true and checkAnswers is true for ${returnPeriod._2}" in {
        val userAnswers =
          emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
            .set(ExemptionsForSmallProducersPage, true).success.value

        val res = ExemptionsForSmallProducersSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)

        res.rows.size mustBe 1

        val row = res.rows.head
        row.key.content.asHtml mustBe Html("Exemptions for registered small producers?")
        row.value.content.asHtml mustBe Html("Yes")
        res.rows.head.actions.head.items.head.href mustBe routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
        row.actions.head.items.head.attributes mustBe Map("id" -> "change-exemptionsForSmallProducers")
        row.actions.head.items.head.content.asHtml mustBe Html("Change")
      }

      s"must show correct row when Exemptions For Small Producers is true and checkAnswers is false for ${returnPeriod._2}" in {
        val userAnswers =
          emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
            .set(ExemptionsForSmallProducersPage, false).success.value

        val res = ExemptionsForSmallProducersSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)

        res.rows.size mustBe 1

        val row = res.rows.head
        row.key.content.asHtml mustBe Html("Exemptions for registered small producers?")
        row.value.content.asHtml mustBe Html("No")
        res.rows.head.actions.head.items.head.href mustBe routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
        row.actions.head.items.head.attributes mustBe Map("id" -> "change-exemptionsForSmallProducers")
        row.actions.head.items.head.content.asHtml mustBe Html("Change")
      }

      s"must show correct row when Exemptions For Small Producers is false for ${returnPeriod._2}" in {
        val userAnswers =
          emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
            .set(ExemptionsForSmallProducersPage, false).success.value

        val res = ExemptionsForSmallProducersSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)

        res.rows.size mustBe 1

        val row = res.rows.head
        row.key.content.asHtml mustBe Html("Exemptions for registered small producers?")
        row.value.content.asHtml mustBe Html("No")
        res.rows.head.actions.head.items.head.href mustBe routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url
        row.actions.head.items.head.attributes mustBe Map("id" -> "change-exemptionsForSmallProducers")
        row.actions.head.items.head.content.asHtml mustBe Html("Change")
      }

      s"must return empty when no answer given ${returnPeriod._2}" in {
        val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))

        val res = ExemptionsForSmallProducersSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
        res.rows.size mustBe 0
      }

    })
  }
}
