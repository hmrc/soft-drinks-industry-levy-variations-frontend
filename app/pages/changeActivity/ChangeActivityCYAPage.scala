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

package pages.changeActivity

import controllers.changeActivity.routes
import models.backend.RetrievedSubscription
import models.{Mode, UserAnswers}
import pages.{Page, RequiredPage}

case object ChangeActivityCYAPage extends Page {

  override def toString: String = "checkYourAnswers"

  override val url: Mode => String = _ => routes.ChangeActivityCYAController.onPageLoad.url

  override val previousPagesRequired: (UserAnswers, RetrievedSubscription) => List[RequiredPage] = (userAnswers, _) =>
    List(
      RequiredPage(AmountProducedPage),
      RequiredPage(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.getChangeActivityData.exists(_.isSmall))),
      RequiredPage(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.getChangeActivityData.exists(_.isLargeOrSmall))),
      RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true))),
      RequiredPage(ContractPackingPage),
      RequiredPage(HowManyContractPackingPage, additionalPreconditions = List(userAnswers.get(ContractPackingPage).contains(true))),
      RequiredPage(ImportsPage),
      RequiredPage(HowManyImportsPage, additionalPreconditions = List(userAnswers.get(ImportsPage).contains(true))),
      RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
        !userAnswers.getChangeActivityData.exists(_.isLarge),
        userAnswers.get(ContractPackingPage).contains(true),
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
        userAnswers.getChangeActivityData.exists(_.isLarge),
        userAnswers.getChangeActivityData.exists(_.operatePackagingSitesOrContractPacking),
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPage(PackagingSiteDetailsPage, additionalPreconditions = List(
        !userAnswers.getChangeActivityData.exists(_.isLarge),
        userAnswers.get(ContractPackingPage).contains(true)
      )),
      RequiredPage(PackagingSiteDetailsPage, additionalPreconditions = List(
        userAnswers.getChangeActivityData.exists(_.isLarge),
        userAnswers.getChangeActivityData.exists(_.operatePackagingSitesOrContractPacking)
      )),
      RequiredPage(SecondaryWarehouseDetailsPage, additionalPreconditions = List(
        userAnswers.get(ImportsPage).contains(true),
        !userAnswers.isEmpty(PackagingSiteDetailsPage)
      ))
    )
}
