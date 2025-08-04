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
import pages.correctReturn.{BroughtIntoUKPage, HowManyBroughtIntoUKPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import controllers.correctReturn.routes

class BroughtIntoUKSummarySpec extends SpecBase {

  "BroughtIntoUKSummary" - {
    val lowLitres = 1000
    val highLitres = 2000

    val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
    val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)


    def lowBandLevyValue(returnPeriod: ReturnPeriod): String = returnPeriod match {
      case ReturnPeriod(2025, 0) => "£180.00"
      case ReturnPeriod(2026, 0) => "£194.00"
    }

    def highBandLevyValue(returnPeriod: ReturnPeriod): String = returnPeriod match {
      case ReturnPeriod(2025, 0) => "£480.00"
      case ReturnPeriod(2026, 0) => "£518.00"
    }

    val returnPeriodsWithLabels = List(
      (preApril2025ReturnPeriod, "- pre April 2025 rates"),
      (taxYear2025ReturnPeriod, "- 2025 tax year rates")
    )

    returnPeriodsWithLabels.foreach(returnPeriod => {
      s"must show correct rows when brought into UK is true, litres provided, and checkAnswers is true ${returnPeriod._2}" in {
        val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
          .set(BroughtIntoUKPage, true).success.value
          .set(HowManyBroughtIntoUKPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
        res.rows.head.key.content.asHtml mustBe Html("Reporting liable drinks brought into the UK?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows.head.actions.head.items.head.href mustBe routes.BroughtIntoUKController.onPageLoad(CheckMode).url
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-broughtIntoUK")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
        res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(1).actions.head.items.head.href mustBe routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        res.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-lowband-litreage-broughtIntoUK")
        res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(2).key.content.asHtml mustBe Html("Low band levy")
        res.rows(2).key.classes mustBe ""
        res.rows(2).value.content.asHtml mustBe Html(lowBandLevyValue(returnPeriod._1))
        res.rows(2).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows(3).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(3).key.classes mustBe ""
        res.rows(3).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
        res.rows(3).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(3).actions.head.items.head.href mustBe routes.HowManyBroughtIntoUKController.onPageLoad(CheckMode).url
        res.rows(3).actions.head.items.head.attributes mustBe Map("id" -> "change-highband-litreage-broughtIntoUK")
        res.rows(3).actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(4).key.content.asHtml mustBe Html("High band levy")
        res.rows(4).key.classes mustBe ""
        res.rows(4).value.content.asHtml mustBe Html(highBandLevyValue(returnPeriod._1))
        res.rows(4).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows.size mustBe 5
      }

    s"must show correct rows when brought into UK is true, litres provided, and checkAnswers is false ${returnPeriod._2}" in {
      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(lowLitres, highLitres)).success.value

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = false)
      res.rows.head.key.content.asHtml mustBe Html("Reporting liable drinks brought into the UK?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows.head.actions mustBe Some(Actions("", List.empty))

      res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      res.rows(1).key.classes mustBe ""
      res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
      res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows(1).actions mustBe None

      res.rows(2).key.content.asHtml mustBe Html("Low band levy")
      res.rows(2).key.classes mustBe ""
      res.rows(2).value.content.asHtml mustBe Html(lowBandLevyValue(returnPeriod._1))
      res.rows(2).value.classes.trim mustBe "sdil-right-align--desktop"

      res.rows(3).key.content.asHtml mustBe Html("Litres in the high band")
      res.rows(3).key.classes mustBe ""
      res.rows(3).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
      res.rows(3).value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows(3).actions mustBe None

      res.rows(4).key.content.asHtml mustBe Html("High band levy")
      res.rows(4).key.classes mustBe ""
      res.rows(4).value.content.asHtml mustBe Html(highBandLevyValue(returnPeriod._1))
      res.rows(4).value.classes.trim mustBe "sdil-right-align--desktop"


      res.rows.size mustBe 5
    }

    s"should return correct elements when passed in with false and NO litres provided  ${returnPeriod._2}" in {
      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
        .set(BroughtIntoUKPage, false).success.value

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)

      res.rows.head.key.content.asHtml mustBe Html("Reporting liable drinks brought into the UK?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows.head.actions.head.items.head.href mustBe controllers.correctReturn.routes.BroughtIntoUKController.onPageLoad(CheckMode).url
      res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-broughtIntoUK")
      res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows.size mustBe 1
    }

    s"must return empty when no answer given ${returnPeriod._2}" in {
      val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))

      val res = BroughtIntoUKSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
      res.rows.size mustBe 0
    }
    })
  }
}
