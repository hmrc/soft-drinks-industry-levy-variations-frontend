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

package views.summary.changeActivity

import base.SpecBase
import models.LitresInBands
import pages.changeActivity.{ HowManyOperatePackagingSiteOwnBrandsPage, OperatePackagingSiteOwnBrandsPage }
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions

class OperatePackagingSiteOwnBrandsSummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000
    s"should return correct elements when passed in with TRUE and litres provided and check answers is true" in {
      val userAnswers = emptyUserAnswersForChangeActivity
        .set(OperatePackagingSiteOwnBrandsPage, true)
        .success
        .value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithoutBandLevyRows(userAnswers, isCheckAnswers = true)
      res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-operate-packaging-site"
      res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
      res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      res.rows(1).key.classes mustBe ""
      res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
      res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
      res
        .rows(1)
        .actions
        .head
        .items
        .head
        .href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-how-many-own-brands-next-12-months"
      res.rows(1).actions.head.items.head.attributes mustBe Map(
        "id" -> "change-lowband-litreage-operatePackagingSiteOwnBrands"
      )
      res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

      val highLitresRowIndex = 2

      res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
      res.rows(highLitresRowIndex).key.classes mustBe ""
      res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(
        java.text.NumberFormat.getInstance.format(highLitres)
      )
      res.rows(highLitresRowIndex).value.classes.trim mustBe "sdil-right-align--desktop"
      res
        .rows(highLitresRowIndex)
        .actions
        .head
        .items
        .head
        .href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-how-many-own-brands-next-12-months"
      res.rows(highLitresRowIndex).actions.head.items.head.attributes mustBe Map(
        "id" -> "change-highband-litreage-operatePackagingSiteOwnBrands"
      )
      res.rows(highLitresRowIndex).actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows.size mustBe 3
    }
    s"should return correct elements when passed in with TRUE and litres provided and check answers is false" in {
      val userAnswers = emptyUserAnswersForChangeActivity
        .set(OperatePackagingSiteOwnBrandsPage, true)
        .success
        .value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres))
        .success
        .value

      val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithoutBandLevyRows(userAnswers, isCheckAnswers = false)
      res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows.head.actions.get mustBe Actions("", List.empty)

      res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      res.rows(1).key.classes mustBe ""
      res.rows(1).value.content.asHtml mustBe Html(java.text.NumberFormat.getInstance.format(lowLitres))
      res.rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows(1).actions mustBe None

      val highLitresRowIndex = 2

      res.rows(highLitresRowIndex).key.content.asHtml mustBe Html("Litres in the high band")
      res.rows(highLitresRowIndex).key.classes mustBe ""
      res.rows(highLitresRowIndex).value.content.asHtml mustBe Html(
        java.text.NumberFormat.getInstance.format(highLitres)
      )
      res.rows(highLitresRowIndex).value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows(highLitresRowIndex).actions mustBe None

      res.rows.size mustBe 3
    }
    s"should return correct elements when passed in with FALSE and NO litres provided" in {
      val userAnswers = emptyUserAnswersForChangeActivity
        .set(OperatePackagingSiteOwnBrandsPage, false)
        .success
        .value

      val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithoutBandLevyRows(userAnswers, isCheckAnswers = true)
      res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
      res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-variations-frontend/change-activity/change-operate-packaging-site"
      res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
      res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows.size mustBe 1
    }
    s"should return correct elements when no elements provided" in {
      val userAnswers = emptyUserAnswersForChangeActivity

      val res = OperatePackagingSiteOwnBrandsSummary.summaryListWithoutBandLevyRows(userAnswers, isCheckAnswers = true)
      res.rows.size mustBe 0
    }
  }
}
