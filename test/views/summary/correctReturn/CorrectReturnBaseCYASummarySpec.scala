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
import models.LitresInBands
import pages.changeActivity.{ContractPackingPage, HowManyContractPackingPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import views.summary.changeActivity.ContractPackingSummary

class CorrectReturnBaseCYASummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000
    val includeLevyRowsOptions = List(true, false)
    includeLevyRowsOptions.foreach(includeLevyRows => {
      s"should return correct elements when passed in with TRUE and litres provided and check answers is true and include levy rows $includeLevyRows" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = ContractPackingSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = includeLevyRows)
        res.rows.head.key.content.asHtml mustBe Html("Package for customers at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-contract-packing"
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-contractPacking")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
        res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(1).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-how-many-contract-packing-next-12-months"
        res.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-lowband-litreage-contractPacking")
        res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

        val highLitresRowIndex = if (includeLevyRows) 3 else 2

        res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(highLitresRowIndex).key.classes mustBe ""
        res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
        res.rows(highLitresRowIndex).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(highLitresRowIndex).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-how-many-contract-packing-next-12-months"
        res.rows(highLitresRowIndex).actions.head.items.head.attributes mustBe Map("id" -> "change-highband-litreage-contractPacking")
        res.rows(highLitresRowIndex).actions.head.items.head.content.asHtml mustBe Html("Change")

        if (includeLevyRows) {

          res.rows(2).key.content.asHtml mustBe Html("litres.lowBandLevy")
          res.rows(2).key.classes mustBe ""
          res.rows(2).value.content.asHtml mustBe Html("£180.00")
          res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"

          res.rows(4).key.content.asHtml mustBe Html("litres.highBandLevy")
          res.rows(4).key.classes mustBe ""
          res.rows(4).value.content.asHtml mustBe Html("£480.00")
          res.rows(4).value.classes.trim mustBe "govuk-!-text-align-right"
        }

        res.rows.size mustBe (if (includeLevyRows) 5 else 3)
      }
      s"should return correct elements when passed in with TRUE and litres provided and check answers is false and include levy rows $includeLevyRows" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = ContractPackingSummary.summaryList(userAnswers, isCheckAnswers = false, includeLevyRows = includeLevyRows)
        res.rows.head.key.content.asHtml mustBe Html("Package for customers at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.get mustBe Actions("", List.empty)

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
        res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(1).actions mustBe None

        val highLitresRowIndex = if (includeLevyRows) 3 else 2

        res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(highLitresRowIndex).key.classes mustBe ""
        res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
        res.rows(highLitresRowIndex).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(highLitresRowIndex).actions mustBe None

        if (includeLevyRows) {

          res.rows(2).key.content.asHtml mustBe Html("litres.lowBandLevy")
          res.rows(2).key.classes mustBe ""
          res.rows(2).value.content.asHtml mustBe Html("£180.00")
          res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"

          res.rows(4).key.content.asHtml mustBe Html("litres.highBandLevy")
          res.rows(4).key.classes mustBe ""
          res.rows(4).value.content.asHtml mustBe Html("£480.00")
          res.rows(4).value.classes.trim mustBe "govuk-!-text-align-right"
        }

        res.rows.size mustBe (if (includeLevyRows) 5 else 3)
      }
      s"should return correct elements when passed in with FALSE and NO litres provided and include levy rows $includeLevyRows" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(ContractPackingPage, false).success.value

        val res = ContractPackingSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = includeLevyRows)
        res.rows.head.key.content.asHtml mustBe Html("Package for customers at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("No")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-contract-packing"
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-contractPacking")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows.size mustBe 1
      }
      s"should return correct elements when no elements provided and include levy rows $includeLevyRows" in {
        val userAnswers = emptyUserAnswersForChangeActivity

        val res = ContractPackingSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = includeLevyRows)
        res.rows.size mustBe 0
      }
    })
  }
}
