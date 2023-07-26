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

import models.UserAnswers
import models.backend.UkAddress
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import pages.changeActivity.{AmountProducedPage, OperatePackagingSiteOwnBrandsPage}


import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>
  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(correctReturn.RemoveSmallProducerConfirmPage.type, JsValue)] ::
    arbitrary[(correctReturn.BroughtIntoUkFromSmallProducersPage.type, JsValue)] ::
    arbitrary[(correctReturn.BroughtIntoUKPage.type, JsValue)] ::
    arbitrary[(correctReturn.ExemptionsForSmallProducersPage.type, JsValue)] ::
    arbitrary[(correctReturn.CorrectionReasonPage.type, JsValue)] ::
    arbitrary[(correctReturn.PackagedAsContractPackerPage.type, JsValue)] ::
    arbitrary[(correctReturn.SelectPage.type, JsValue)] ::
    arbitrary[(correctReturn.RepaymentMethodPage.type, JsValue)] ::
    arbitrary[(changeActivity.ThirdPartyPackagersPage.type, JsValue)] ::
    arbitrary[(changeActivity.PackAtBusinessAddressPage.type, JsValue)] ::
    arbitrary[(changeActivity.PackagingSiteDetailsPage.type, JsValue)] ::
    arbitrary[(changeActivity.RemovePackagingSiteDetailsPage.type, JsValue)] ::
    arbitrary[(changeActivity.SecondaryWarehouseDetailsPage.type, JsValue)] ::
    arbitrary[(updateRegisteredDetails.PackagingSiteDetailsPage.type, JsValue)] ::
    arbitrary[(updateRegisteredDetails.RemoveWarehouseDetailsPage.type, JsValue)] ::
    arbitrary[(updateRegisteredDetails.PackingSiteDetailsRemovePage.type, JsValue)] ::
    arbitrary[(CancelRegistrationDatePage.type, JsValue)] ::
    arbitrary[(changeActivity.ContractPackingPage.type, JsValue)] ::
    arbitrary[(updateRegisteredDetails.WarehouseDetailsPage.type, JsValue)] ::
    arbitrary[(changeActivity.ImportsPage.type, JsValue)] ::
    arbitrary[(changeActivity.OperatePackagingSiteOwnBrandsPage.type, JsValue)] ::
    arbitrary[(correctReturn.OperatePackagingSiteOwnBrandsPage.type, JsValue)] ::
    arbitrary[(correctReturn.ClaimCreditsForLostDamagedPage.type, JsValue)] ::
    arbitrary[(ReasonPage.type, JsValue)] ::
    arbitrary[(changeActivity.AmountProducedPage.type, JsValue)] ::
    arbitrary[(UpdateContactDetailsPage.type, JsValue)] ::
    arbitrary[(SelectChangePage.type, JsValue)] ::
    Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        data    <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers (
        id = id,
        journeyType = SelectChange.UpdateRegisteredDetails,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
        , contactAddress = contactAddress
      )
    }
  }
}
