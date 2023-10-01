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

package views.summary.updateRegisteredDetails

import controllers.updateRegisteredDetails.routes
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UKSitesSummary {

  private def getPackagingSiteRow(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
    val key = if (userAnswers.packagingSiteList.size != 1) {
      messages("checkYourAnswers.packing.checkYourAnswersLabel.multiple", userAnswers.packagingSiteList.size.toString)
    } else {
      messages("checkYourAnswers.packing.checkYourAnswersLabel.one")
    }
    SummaryListRowViewModel(
      key = Key(
        content = key,
        classes = "govuk-!-width-full"
      ),
      value = Value(),
      actions = if (isCheckAnswers) {
        val onwardRoute = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url

        Seq(
          if (userAnswers.packagingSiteList.size != 1) {
            ActionItemViewModel("site.change", onwardRoute)
              .withAttribute(("id", "change-packaging-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.packing.change.hidden.multiple"))
          } else {
            ActionItemViewModel("site.change", onwardRoute)
              .withAttribute(("id", "change-packaging-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.packing.change.hidden.one"))
          }
        )
      } else {
        Seq.empty
      }
    )
  }

  private def getWarehousesRow (userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
    val key = if (userAnswers.warehouseList.size != 1) {
      messages("checkYourAnswers.warehouse.checkYourAnswersLabel.multiple", userAnswers.warehouseList.size.toString)
    } else {
      messages("checkYourAnswers.warehouse.checkYourAnswersLabel.one")
    }
    SummaryListRowViewModel(
      key = Key(
        content = key,
        classes = "govuk-!-width-full"
      ),
      value = Value(),
      actions = if (isCheckAnswers) {
        val onwardRoute = routes.WarehouseDetailsController.onPageLoad(CheckMode).url

        Seq(
          if (userAnswers.warehouseList.size != 1) {
            ActionItemViewModel("site.change", onwardRoute)
              .withAttribute(("id", "change-warehouse-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.warehouse.change.hidden.multiple"))
          } else {
            ActionItemViewModel("site.change", onwardRoute)
              .withAttribute(("id", "change-warehouse-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.warehouse.change.hidden.one"))
          }
        )

      } else {
        Seq.empty
      }
    )
  }

  def getHeadingAndSummary(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                          (implicit messages: Messages): Option[(String, SummaryList)] = {
//    val optSummaryList = (
//      userAnswers.packagingSiteList.nonEmpty,
//      userAnswers.warehouseList.nonEmpty
//    ) match {
//      case (true, false) => Option(
//        SummaryListViewModel(
//          Seq(
//            getPackagingSiteRow(userAnswers, isCheckAnswers)
//          )
//        )
//      )
//      case (false, true) => Option(
//        SummaryListViewModel(
//          Seq(
//            getWarehousesRow(userAnswers, isCheckAnswers)
//          )
//        )
//      )
//      case (true, true) => Option(
//        SummaryListViewModel(
//          Seq(
//            getPackagingSiteRow(userAnswers, isCheckAnswers),
//            getWarehousesRow(userAnswers, isCheckAnswers)
//          )
//        )
//      )
//      case _ => None
//    }
    val optSummaryList = Option(
      SummaryListViewModel(
        Seq(
          getPackagingSiteRow(userAnswers, isCheckAnswers),
          getWarehousesRow(userAnswers, isCheckAnswers)
        )
      )
    )
    optSummaryList.map(list => messages("checkYourAnswers.sites") -> list)
  }

}

