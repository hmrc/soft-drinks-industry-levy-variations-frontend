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

package navigation

import controllers.updateRegisteredDetails.routes
import models.updateRegisteredDetails.ChangeRegisteredDetails
import models.{CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.updateRegisteredDetails._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForUpdateRegisteredDetails @Inject()() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ChangeRegisteredDetailsPage => userAnswers => changeRegisteredDetailNavigation(userAnswers)
    case WarehouseDetailsPage => userAnswers => defaultCall
    case RemoveWarehouseDetailsPage => _ => routes.WarehouseDetailsController.onPageLoad(NormalMode)
    case PackagingSiteDetailsPage => userAnswers => PackagingSiteDetailsNavigation(userAnswers)
    case PackingSiteDetailsRemovePage => _ => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    case UpdateContactDetailsPage => userAnswers => UpdateContactDetailsNavigation(userAnswers)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case RemoveWarehouseDetailsPage => _ => routes.WarehouseDetailsController.onPageLoad(CheckMode)
    case PackingSiteDetailsRemovePage => _ => routes.PackagingSiteDetailsController.onPageLoad(CheckMode)
    case _ => _ => routes.UpdateRegisteredDetailsCYAController.onPageLoad
  }

  def PackagingSiteDetailsNavigation(userAnswers: UserAnswers): Call = {
    val changeRegisteredDetailsPageAnswers = userAnswers.get(ChangeRegisteredDetailsPage).head
    if (changeRegisteredDetailsPageAnswers.contains(ChangeRegisteredDetails.ContactDetails)) {
      routes.UpdateContactDetailsController.onPageLoad(NormalMode)
    } else {
      routes.UpdateRegisteredDetailsCYAController.onPageLoad
    }
  }

  def UpdateContactDetailsNavigation(userAnswers: UserAnswers): Call = {
    val changeRegisteredDetailsPageAnswers = userAnswers.get(ChangeRegisteredDetailsPage).head
    if (changeRegisteredDetailsPageAnswers.contains(ChangeRegisteredDetails.BusinessAddress)) {
      routes.BusinessAddressController.onPageLoad()
    } else {
      routes.UpdateRegisteredDetailsCYAController.onPageLoad
    }
  }

  def changeRegisteredDetailNavigation(userAnswers: UserAnswers) = {
    val changeRegisteredDetailsPageAnswers = userAnswers.get(ChangeRegisteredDetailsPage).head
    if (changeRegisteredDetailsPageAnswers.contains(ChangeRegisteredDetails.Sites)) {
      userAnswers match {
        case userAnswers if userAnswers.packagingSiteList.nonEmpty =>
          routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
        case _ =>
          routes.WarehouseDetailsController.onPageLoad(NormalMode)
      }
    } else if (changeRegisteredDetailsPageAnswers.contains(ChangeRegisteredDetails.ContactDetails)) {
      routes.UpdateContactDetailsController.onPageLoad(NormalMode)
    } else {
      routes.BusinessAddressController.onPageLoad()
    }
  }
}
