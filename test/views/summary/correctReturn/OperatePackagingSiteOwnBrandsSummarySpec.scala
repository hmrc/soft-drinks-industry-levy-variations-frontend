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
import pages.correctReturn.{HowManyOperatePackagingSiteOwnBrandsPage, OperatePackagingSiteOwnBrandsPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions

class OperatePackagingSiteOwnBrandsSummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000

    val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
    val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

    def lowBandLevyValue(returnPeriod: ReturnPeriod) = returnPeriod match {
      case ReturnPeriod(2025, 0) => "£180.00"
      case ReturnPeriod(2026, 0) => "£194.00"
    }

    def highBandLevyValue(returnPeriod: ReturnPeriod) = returnPeriod match {
      case ReturnPeriod(2025, 0) => "£480.00"
      case ReturnPeriod(2026, 0) => "£518.00"
    }

    val returnPeriods = List(
      (preApril2025ReturnPeriod, "- pre April 2025 rates"),
      (taxYear2025ReturnPeriod, "- 2025 tax year rates")
    )

    returnPeriods.foreach(returnPeriod => {
      s"should return correct elements when passed in with TRUE and litres provided and check answers is true ${returnPeriod._2}" in {
        val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
        res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows.head.actions.head.items.head.href mustBe controllers.correctReturn.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
        res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(1).actions.head.items.head.href mustBe controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        res.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-lowband-litreage-operatePackagingSiteOwnBrands")
        res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

        val highLitresRowIndex = 3

        res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(highLitresRowIndex).key.classes mustBe ""
        res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
        res.rows(highLitresRowIndex).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(highLitresRowIndex).actions.head.items.head.href mustBe controllers.correctReturn.routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        res.rows(highLitresRowIndex).actions.head.items.head.attributes mustBe Map("id" -> "change-highband-litreage-operatePackagingSiteOwnBrands")
        res.rows(highLitresRowIndex).actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(2).key.content.asHtml mustBe Html("Low band levy")
        res.rows(2).key.classes mustBe ""
        res.rows(2).value.content.asHtml mustBe Html(lowBandLevyValue(returnPeriod._1))
        res.rows(2).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows(4).key.content.asHtml mustBe Html("High band levy")
        res.rows(4).key.classes mustBe ""
        res.rows(4).value.content.asHtml mustBe Html(highBandLevyValue(returnPeriod._1))
        res.rows(4).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows.size mustBe 5
      }
      s"should return correct elements when passed in with TRUE and litres provided and check answers is false ${returnPeriod._2}" in {
        val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = false)
        res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows.head.actions.get mustBe Actions("", List.empty)

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
        res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(1).actions mustBe None

        val highLitresRowIndex = 3

        res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(highLitresRowIndex).key.classes mustBe ""
        res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(highLitres))
        res.rows(highLitresRowIndex).value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows(highLitresRowIndex).actions mustBe None

        res.rows(2).key.content.asHtml mustBe Html("Low band levy")
        res.rows(2).key.classes mustBe ""
        res.rows(2).value.content.asHtml mustBe Html(lowBandLevyValue(returnPeriod._1))
        res.rows(2).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows(4).key.content.asHtml mustBe Html("High band levy")
        res.rows(4).key.classes mustBe ""
        res.rows(4).value.content.asHtml mustBe Html(highBandLevyValue(returnPeriod._1))
        res.rows(4).value.classes.trim mustBe "sdil-right-align--desktop"

        res.rows.size mustBe 5
      }
      s"should return correct elements when passed in with FALSE and NO litres provided ${returnPeriod._2}" in {
        val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))
          .set(OperatePackagingSiteOwnBrandsPage, false).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
        res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("No")
        res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
        res.rows.head.actions.head.items.head.href mustBe controllers.correctReturn.routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows.size mustBe 1
      }
      s"should return correct elements when no elements provided ${returnPeriod._2}" in {
          val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod._1))

          val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithBandLevyRows(userAnswers, isCheckAnswers = true)
          res.rows.size mustBe 0
        }
    })
  }
}
