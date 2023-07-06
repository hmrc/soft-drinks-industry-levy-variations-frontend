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

package views.changeActivity.summary

import base.SpecBase
import models.LitresInBands
import pages.changeActivity.{HowManyOperatePackagingSiteOwnBrandsPage, OperatePackagingSiteOwnBrandsPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import views.summary.changeActivity.OperatePackagingSiteOwnBrandsSummary

class OperatePackagingSiteOwnBrandsSummarySpec extends SpecBase {

  "summaryList" - {
    val lowLitres = 1000
    val highLitres = 2000
    val includeLevyRowsOptions = List(true, false)
    includeLevyRowsOptions.foreach(value => {
      s"should return correct elements when passed in with TRUE and litres provided and check answers is true and include levy rows $value" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = value)
        res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.head.items.head.href mustBe "/change-operate-packaging-site"
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        //      res.rows(1).value.content.asHtml mustBe Html("1,000")
        res.rows(1).value.content.asHtml mustBe Html(lowLitres.toString)
        res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(1).actions.head.items.head.href mustBe "/change-how-many-own-brands-next-12-months"
        res.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-lowband-litreage-operatePackagingSiteOwnBrands")
        res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(2).key.classes mustBe ""
        //      res.rows(2).value.content.asHtml mustBe Html("2,000")
        res.rows(2).value.content.asHtml mustBe Html(highLitres.toString)
        res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(2).actions.head.items.head.href mustBe "/change-how-many-own-brands-next-12-months"
        res.rows(2).actions.head.items.head.attributes mustBe Map("id" -> "change-highband-litreage-operatePackagingSiteOwnBrands")
        res.rows(2).actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows.size mustBe 3
      }
      s"should return correct elements when passed in with TRUE and litres provided and check answers is false and include levy rows $value" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(lowLitres, highLitres)).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = false, includeLevyRows = value)
        res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("Yes")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.get mustBe Actions("", List.empty)

        res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
        res.rows(1).key.classes mustBe ""
        //      res.rows(1).value.content.asHtml mustBe Html("1,000")
        res.rows(1).value.content.asHtml mustBe Html(lowLitres.toString)
        res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(1).actions mustBe None

        res.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
        res.rows(2).key.classes mustBe ""
        //      res.rows(2).value.content.asHtml mustBe Html("2,000")
        res.rows(2).value.content.asHtml mustBe Html(highLitres.toString)
        res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows(2).actions mustBe None

        res.rows.size mustBe 3
      }
      s"should return correct elements when passed in with FALSE and NO litres provided and include levy rows $value" in {
        val userAnswers = emptyUserAnswersForChangeActivity
          .set(OperatePackagingSiteOwnBrandsPage, false).success.value

        val res = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = value)
        res.rows.head.key.content.asHtml mustBe Html("Package your own brand at your own sites?")
        res.rows.head.key.classes mustBe ""
        res.rows.head.value.content.asHtml mustBe Html("No")
        res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
        res.rows.head.actions.head.items.head.href mustBe "/change-operate-packaging-site"
        res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSiteOwnBrands")
        res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

        res.rows.size mustBe 1
      }
      s"should return correct elements when no elements provided and include levy rows $value" in {
        val userAnswers = emptyUserAnswersForChangeActivity

        val res = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = value)
        res.rows.size mustBe 1
        //      res.rows.size mustBe 0
      }
    })
  }
}
