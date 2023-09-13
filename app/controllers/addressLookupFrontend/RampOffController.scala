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

package controllers.addressLookupFrontend

import controllers.actions.ControllerActions
import models.{NormalMode, SelectChange}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressLookupService, ContactDetails, PackingDetails, WarehouseDetails}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RampOffController @Inject()(controllerActions: ControllerActions,
                                  addressLookupService: AddressLookupService,
                                  sessionRepository: SessionRepository,
                                  val controllerComponents: MessagesControllerComponents)
                                 (implicit val ex: ExecutionContext) extends FrontendBaseController {

  def secondaryWareHouseDetailsOffRamp(sdilId: String, alfId: String): Action[AnyContent] = controllerActions.withRequiredData.async {
    implicit request =>
      for {
        alfResponse <- addressLookupService.getAddress(alfId)
        updatedUserAnswers = addressLookupService.addAddressUserAnswers(WarehouseDetails, alfResponse.address, request.userAnswers, sdilId, alfId)
        _ <- sessionRepository.set(updatedUserAnswers)
      } yield {
        val redirectUrl = updatedUserAnswers.journeyType match {
          case SelectChange.UpdateRegisteredDetails => controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(NormalMode)
          case SelectChange.ChangeActivity => controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad
          case _ => controllers.routes.IndexController.onPageLoad
        }
        Redirect(redirectUrl)
      }
  }

  def packingSiteDetailsOffRamp(sdilId: String, alfId: String): Action[AnyContent] = controllerActions.withRequiredData.async {
    implicit request =>
      for {
        alfResponse <- addressLookupService.getAddress(alfId)
        updatedUserAnswers = addressLookupService.addAddressUserAnswers(PackingDetails, alfResponse.address, request.userAnswers, sdilId, alfId)
        _ <- sessionRepository.set(updatedUserAnswers)
      } yield {
        val redirectUrl = updatedUserAnswers.journeyType match {
          case SelectChange.ChangeActivity => controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
          case SelectChange.UpdateRegisteredDetails => controllers.updateRegisteredDetails.routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
          case _ => controllers.routes.IndexController.onPageLoad
        }
        Redirect(redirectUrl)
      }
  }

  def contactDetailsOffRamp(sdilId: String, alfId: String): Action[AnyContent] = controllerActions.withRequiredData.async {
    implicit request =>
      for {
        alfResponse <- addressLookupService.getAddress(alfId)
        updatedUserAnswers = addressLookupService.addAddressUserAnswers(ContactDetails, alfResponse.address, request.userAnswers, sdilId, alfId)
        _ <- sessionRepository.set(updatedUserAnswers)
      } yield {
        Redirect(controllers.updateRegisteredDetails.routes.BusinessAddressController.onPageLoad())
      }
  }
}
