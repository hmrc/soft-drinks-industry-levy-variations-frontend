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
import pages.cancelRegistration.CancelRegistrationDatePage
import pages.cancelRegistration.ReasonPage
import pages.updateRegisteredDetails.UpdateContactDetailsPage

trait PageGenerators {

  implicit lazy val arbitraryCorrectReturnCorrectionReasonPage: Arbitrary[correctReturn.CorrectionReasonPage.type] =
    Arbitrary(correctReturn.CorrectionReasonPage)

  implicit lazy val arbitraryThirdPartyPackagersPage: Arbitrary[changeActivity.ThirdPartyPackagersPage.type] =
    Arbitrary(changeActivity.ThirdPartyPackagersPage)

  implicit lazy val arbitraryCorrectReturnSelectPage: Arbitrary[correctReturn.SelectPage.type] =
    Arbitrary(correctReturn.SelectPage)

  implicit lazy val arbitraryChangeActivityPackAtBusinessAddressPage: Arbitrary[changeActivity.PackAtBusinessAddressPage.type] =
    Arbitrary(changeActivity.PackAtBusinessAddressPage)

  implicit lazy val arbitraryChangeActivityPackagingSiteDetailsPage: Arbitrary[changeActivity.PackagingSiteDetailsPage.type] =
    Arbitrary(changeActivity.PackagingSiteDetailsPage)

  implicit lazy val arbitraryChangeActivityRemovePackagingSiteDetailsPage: Arbitrary[changeActivity.RemovePackagingSiteDetailsPage.type] =
    Arbitrary(changeActivity.RemovePackagingSiteDetailsPage)

  implicit lazy val arbitraryChangeActivityRemoveWarehouseDetailsPage: Arbitrary[changeActivity.SecondaryWarehouseDetailsPage.type] =
    Arbitrary(changeActivity.SecondaryWarehouseDetailsPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsPackagingSiteDetailsPage: Arbitrary[updateRegisteredDetails.PackagingSiteDetailsPage.type] =
    Arbitrary(updateRegisteredDetails.PackagingSiteDetailsPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsRemoveWarehouseDetailsPage: Arbitrary[updateRegisteredDetails.RemoveWarehouseDetailsPage.type] =
    Arbitrary(updateRegisteredDetails.RemoveWarehouseDetailsPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsPackingSiteDetailsRemovePage: Arbitrary[updateRegisteredDetails.PackingSiteDetailsRemovePage.type] =
    Arbitrary(updateRegisteredDetails.PackingSiteDetailsRemovePage)

  implicit lazy val arbitraryCancelRegistrationCancelRegistrationDatePage: Arbitrary[CancelRegistrationDatePage.type] =
    Arbitrary(CancelRegistrationDatePage)

  implicit lazy val arbitraryChangeActivityContractPackingPage: Arbitrary[changeActivity.ContractPackingPage.type] =
    Arbitrary(changeActivity.ContractPackingPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsWarehouseDetailsPage: Arbitrary[updateRegisteredDetails.WarehouseDetailsPage.type] =
    Arbitrary(updateRegisteredDetails.WarehouseDetailsPage)

  implicit lazy val arbitraryChangeActivityImportsPage: Arbitrary[changeActivity.ImportsPage.type] =
    Arbitrary(changeActivity.ImportsPage)

  implicit lazy val arbitraryChangeActivityOperatePackagingSiteOwnBrandsPage: Arbitrary[changeActivity.OperatePackagingSiteOwnBrandsPage.type] =
    Arbitrary(changeActivity.OperatePackagingSiteOwnBrandsPage)

  implicit lazy val arbitraryCorrectReturnOperatePackagingSiteOwnBrandsPage: Arbitrary[correctReturn.OperatePackagingSiteOwnBrandsPage.type] =
    Arbitrary(correctReturn.OperatePackagingSiteOwnBrandsPage)

  implicit lazy val arbitraryCancelRegistrationReasonPage: Arbitrary[ReasonPage.type] =
    Arbitrary(ReasonPage)

  implicit lazy val arbitraryChangeActivityAmountProducedPage: Arbitrary[changeActivity.AmountProducedPage.type] =
    Arbitrary(changeActivity.AmountProducedPage)

  implicit lazy val arbitraryUpdateRegisteredDetailsUpdateContactDetailsPage: Arbitrary[UpdateContactDetailsPage.type] =
    Arbitrary(UpdateContactDetailsPage)

  implicit lazy val arbitrarySelectChangePage: Arbitrary[SelectChangePage.type] =
    Arbitrary(SelectChangePage)
}
