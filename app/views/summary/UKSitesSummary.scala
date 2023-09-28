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

package views.summary

import models.{CheckMode, RetrievedSubscription, SdilReturn, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utilities.UserTypeCheck
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UKSitesSummary {

  private def getPackAtBusinessAddressRow(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key = if (userAnswers.packagingSiteList.size != 1) {
          messages("checkYourAnswers.packing.checkYourAnswersLabel.multiple", userAnswers.packagingSiteList.size.toString)
        } else {
          messages("checkYourAnswers.packing.checkYourAnswersLabel.one")
        },
        value = Value(),
        actions = if (isCheckAnswers) {
          val onwardRoute = if (userAnswers.packagingSiteList.nonEmpty) {
            controllers.correctReturn.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
          } else {
            controllers.correctReturn.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url
          }

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

  private def getAskSecondaryWarehouseRow (userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key = if (userAnswers.warehouseList.size != 1) {
          messages("checkYourAnswers.warehouse.checkYourAnswersLabel.multiple", userAnswers.warehouseList.size.toString)
        } else {
          messages("checkYourAnswers.warehouse.checkYourAnswersLabel.one")
        },
        value = Value(),
        actions = if (isCheckAnswers) {
          val onwardRoute = if (userAnswers.warehouseList.nonEmpty) {
            controllers.correctReturn.routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url
          } else {
            controllers.correctReturn.routes.AskSecondaryWarehouseInReturnController.onPageLoad(CheckMode).url
          }

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

  def getHeadingAndSummary(userAnswers: UserAnswers, isCheckAnswers: Boolean, subscription: RetrievedSubscription)
                          (implicit messages: Messages): Option[(String, SummaryList)] = {
    val optSummaryList = (
      userAnswers.packagingSiteList.nonEmpty,
      userAnswers.warehouseList.nonEmpty
    ) match {
      case (true, false) => Option(
        SummaryListViewModel(
          Seq(
            getPackAtBusinessAddressRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case (false, true) => Option(
        SummaryListViewModel(
          Seq(
            getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case (true, true) => Option(
        SummaryListViewModel(
          Seq(
            getPackAtBusinessAddressRow(userAnswers, isCheckAnswers),
            getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case _ => None
    }
    optSummaryList.map(list => "checkYourAnswers.sites" -> list)
  }

  def getHeadingAndSummaryForCorrectReturn(userAnswers: UserAnswers, isCheckAnswers: Boolean, subscription: RetrievedSubscription)
                                          (implicit messages: Messages): Option[(String, SummaryList)] = {
    val optSummaryList = (
      UserTypeCheck.isNewPacker(SdilReturn.apply(userAnswers), subscription) && subscription.productionSites.isEmpty,
      UserTypeCheck.isNewImporter(SdilReturn.apply(userAnswers), subscription) && subscription.warehouseSites.isEmpty
    ) match {
      case (true, false) => Option(
        SummaryListViewModel(
          Seq(
            getPackAtBusinessAddressRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case (false, true) => Option(
        SummaryListViewModel(
          Seq(
            getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case (true, true) => Option(
        SummaryListViewModel(
          Seq(
            getPackAtBusinessAddressRow(userAnswers, isCheckAnswers),
            getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers)
          )
        )
      )
      case _ => None
    }
      optSummaryList.map(list => "checkYourAnswers.sites" -> list)
    }

  }
