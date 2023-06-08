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
import models.updateRegisteredDetails.UpdateContactDetails
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.cancelRegistration.CancelRegistrationDatePage
import pages.SelectChangePage
import pages.cancelRegistration.ReasonPage
import pages.changeActivity.{AmountProducedPage, OperatePackagingSiteOwnBrandsPage}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryChangeActivityOperatePackagingSiteOwnBrandsUserAnswersEntry: Arbitrary[(OperatePackagingSiteOwnBrandsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OperatePackagingSiteOwnBrandsPage.type]
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

  implicit lazy val arbitraryChangeActivityAmountProducedUserAnswersEntry: Arbitrary[(AmountProducedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmountProducedPage.type]
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
        value <- arbitrary[UpdateContactDetails].map(Json.toJson(_))
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
