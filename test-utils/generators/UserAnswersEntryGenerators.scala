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

import models.SelectChange
import models.changeActivity.AmountProduced
import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.updateRegisteredDetails.ContactDetails
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.SelectChangePage
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryUpdateRegisteredDetailsChangeRegisteredDetailsUserAnswersEntry: Arbitrary[(pages.updateRegisteredDetails.ChangeRegisteredDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.updateRegisteredDetails.ChangeRegisteredDetailsPage.type]
        value <- arbitrary[models.updateRegisteredDetails.ChangeRegisteredDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnRemovePackagingSiteConfirmUserAnswersEntry: Arbitrary[(pages.correctReturn.RemovePackagingSiteConfirmPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.RemovePackagingSiteConfirmPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnSecondaryWarehouseDetailsUserAnswersEntry: Arbitrary[(pages.correctReturn.SecondaryWarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.SecondaryWarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnRemoveWarehouseConfirmUserAnswersEntry: Arbitrary[(pages.correctReturn.RemoveWarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.RemoveWarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnAskSecondaryWarehouseInReturnUserAnswersEntry: Arbitrary[(pages.correctReturn.AskSecondaryWarehouseInReturnPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.AskSecondaryWarehouseInReturnPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnSmallProducerDetailsUserAnswersEntry: Arbitrary[(pages.correctReturn.SmallProducerDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.SmallProducerDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnPackagingSiteDetailsUserAnswersEntry: Arbitrary[(pages.correctReturn.PackagingSiteDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.PackagingSiteDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnBroughtIntoUkFromSmallProducersUserAnswersEntry: Arbitrary[(pages.correctReturn.BroughtIntoUkFromSmallProducersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.BroughtIntoUkFromSmallProducersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryCorrectReturnRemoveSmallProducerConfirmUserAnswersEntry: Arbitrary[(pages.correctReturn.RemoveSmallProducerConfirmPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.RemoveSmallProducerConfirmPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnClaimCreditsForExportsUserAnswersEntry: Arbitrary[(pages.correctReturn.ClaimCreditsForExportsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.ClaimCreditsForExportsPage.type]

        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnBroughtIntoUKUserAnswersEntry: Arbitrary[(pages.correctReturn.BroughtIntoUKPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.BroughtIntoUKPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnExemptionsForSmallProducersUserAnswersEntry: Arbitrary[(pages.correctReturn.ExemptionsForSmallProducersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.ExemptionsForSmallProducersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnCorrectionReasonUserAnswersEntry: Arbitrary[(pages.correctReturn.CorrectionReasonPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.CorrectionReasonPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnPackagedAsContractPackerUserAnswersEntry: Arbitrary[(pages.correctReturn.PackagedAsContractPackerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.correctReturn.PackagedAsContractPackerPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnAddASmallProducerUserAnswersEntry: Arbitrary[(pages.correctReturn.AddASmallProducerPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.AddASmallProducerPage.type]
        value <- arbitrary[AddASmallProducer].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnRepaymentMethodUserAnswersEntry: Arbitrary[(pages.correctReturn.RepaymentMethodPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.RepaymentMethodPage.type]
        value <- arbitrary[RepaymentMethod].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryThirdPartyPackagersUserAnswersEntry: Arbitrary[(pages.changeActivity.ThirdPartyPackagersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.ThirdPartyPackagersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityPackAtBusinessAddressUserAnswersEntry: Arbitrary[(pages.changeActivity.PackAtBusinessAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.PackAtBusinessAddressPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnPackAtBusinessAddressUserAnswersEntry: Arbitrary[(pages.correctReturn.PackAtBusinessAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.PackAtBusinessAddressPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityPackagingSiteDetailsUserAnswersEntry: Arbitrary[(pages.changeActivity.PackagingSiteDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.changeActivity.PackagingSiteDetailsPage.type]
        value <- arbitrary[Boolean].map (Json.toJson (_))
      } yield ( page, value)
    }

  implicit lazy val arbitraryChangeActivityRemovePackagingSiteDetailsUserAnswersEntry: Arbitrary[(pages.changeActivity.RemovePackagingSiteDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.changeActivity.RemovePackagingSiteDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityWarehouseDetailsUserAnswersEntry: Arbitrary[(pages.changeActivity.SecondaryWarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.changeActivity.SecondaryWarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsPackagingSiteDetailsUserAnswersEntry: Arbitrary[(pages.updateRegisteredDetails.PackagingSiteDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.updateRegisteredDetails.PackagingSiteDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsRemoveWarehouseDetailsUserAnswersEntry: Arbitrary[(pages.updateRegisteredDetails.RemoveWarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.updateRegisteredDetails.RemoveWarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsPackingSiteDetailsRemoveUserAnswersEntry: Arbitrary[(pages.updateRegisteredDetails.PackingSiteDetailsRemovePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.updateRegisteredDetails.PackingSiteDetailsRemovePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityContractPackingUserAnswersEntry: Arbitrary[(pages.changeActivity.ContractPackingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.ContractPackingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsWarehouseDetailsUserAnswersEntry: Arbitrary[(pages.updateRegisteredDetails.WarehouseDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.updateRegisteredDetails.WarehouseDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityImportsUserAnswersEntry: Arbitrary[(pages.changeActivity.ImportsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.ImportsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityOperatePackagingSiteOwnBrandsUserAnswersEntry: Arbitrary[(pages.changeActivity.OperatePackagingSiteOwnBrandsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.OperatePackagingSiteOwnBrandsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnOperatePackagingSiteOwnBrandsUserAnswersEntry: Arbitrary[(pages.correctReturn.OperatePackagingSiteOwnBrandsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.OperatePackagingSiteOwnBrandsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorrectReturnClaimCreditsForLostDamagedUserAnswersEntry: Arbitrary[(pages.correctReturn.ClaimCreditsForLostDamagedPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.correctReturn.ClaimCreditsForLostDamagedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCancelRegistrationReasonUserAnswersEntry: Arbitrary[(ReasonPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[ReasonPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChangeActivityAmountProducedUserAnswersEntry: Arbitrary[(pages.changeActivity.AmountProducedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.changeActivity.AmountProducedPage.type]
        value <- arbitrary[AmountProduced].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCancelRegistrationCancelRegistrationDateUserAnswersEntry: Arbitrary[(CancelRegistrationDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CancelRegistrationDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsUpdateContactDetailsUserAnswersEntry: Arbitrary[(UpdateContactDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UpdateContactDetailsPage.type]
        value <- arbitrary[ContactDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySelectChangeUserAnswersEntry: Arbitrary[(SelectChangePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SelectChangePage.type]
        value <- arbitrary[SelectChange].map(Json.toJson(_))
      } yield (page, value)
    }
}
