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
import models.changeActivity.AmountProduced
import models.{Mode, UserAnswers}
import pages.{Page, RequiredPage}

case object ChangeActivityCYAPage extends Page {

  override def toString: String = "checkYourAnswers"

  override val url: Mode => String = _ => routes.ChangeActivityCYAController.onPageLoad.url

  override val redirectConditions: UserAnswers => List[RequiredPage] = userAnswers => {
    val isSmallOrLargeProducer: Boolean =
      userAnswers.get(AmountProducedPage).contains(AmountProduced.Large) || userAnswers.get(AmountProducedPage).contains(AmountProduced.Small)
    val isSmallOrNoneProducer: Boolean =
      userAnswers.get(AmountProducedPage).contains(AmountProduced.Small) || userAnswers.get(AmountProducedPage).contains(AmountProduced.None)
    val eitherOperatePackagingSitesOrContractPacking = userAnswers.get(OperatePackagingSiteOwnBrandsPage).flatMap(ops => {
      userAnswers.get(ContractPackingPage).map(cp => {
        ops || cp
      })
    }).getOrElse(false)

    List(
      RequiredPage(AmountProducedPage),
      RequiredPage(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small))),
      RequiredPage(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(isSmallOrLargeProducer)),
      RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true))),
      RequiredPage(ContractPackingPage),
      RequiredPage(HowManyContractPackingPage, additionalPreconditions = List(userAnswers.get(ContractPackingPage).contains(true))),
      RequiredPage(ImportsPage),
      RequiredPage(HowManyImportsPage, additionalPreconditions = List(userAnswers.get(ImportsPage).contains(true))),
      RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
        isSmallOrNoneProducer,
        userAnswers.get(ContractPackingPage).contains(true),
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
        userAnswers.get(AmountProducedPage).contains(AmountProduced.Large),
        eitherOperatePackagingSitesOrContractPacking,
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPage(PackagingSiteDetailsPage, additionalPreconditions = List(
        isSmallOrNoneProducer,
        userAnswers.get(ContractPackingPage).contains(true)
      )),
      RequiredPage(PackagingSiteDetailsPage, additionalPreconditions = List(
        userAnswers.get(AmountProducedPage).contains(AmountProduced.Large),
        eitherOperatePackagingSitesOrContractPacking
      )),
      RequiredPage(SecondaryWarehouseDetailsPage, additionalPreconditions = List(
        userAnswers.get(ImportsPage).contains(true),
        !userAnswers.isEmpty(PackagingSiteDetailsPage)
      ))
    )
  }
}
