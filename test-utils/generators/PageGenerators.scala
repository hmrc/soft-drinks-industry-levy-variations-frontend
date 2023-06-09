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

package generators

import org.scalacheck.Arbitrary
import pages._
import pages.cancelRegistration.ReasonPage
import pages.changeActivity.{AmountProducedPage, OperatePackagingSiteOwnBrandsPage}
import pages.updateRegisteredDetails.UpdateContactDetailsPage

trait PageGenerators {

  implicit lazy val arbitraryChangeActivityImportsPage: Arbitrary[changeActivity.ImportsPage.type] =
    Arbitrary(changeActivity.ImportsPage)

  implicit lazy val arbitraryChangeActivityOperatePackagingSiteOwnBrandsPage: Arbitrary[OperatePackagingSiteOwnBrandsPage.type] =
    Arbitrary(OperatePackagingSiteOwnBrandsPage)


  implicit lazy val arbitraryCancelRegistrationReasonPage: Arbitrary[ReasonPage.type] =
    Arbitrary(ReasonPage)
  implicit lazy val arbitraryChangeActivityAmountProducedPage: Arbitrary[AmountProducedPage.type] =
    Arbitrary(AmountProducedPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsUpdateContactDetailsPage: Arbitrary[UpdateContactDetailsPage.type] =
    Arbitrary(UpdateContactDetailsPage)

  implicit lazy val arbitrarySelectChangePage: Arbitrary[SelectChangePage.type] =
    Arbitrary(SelectChangePage)
}
