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

package controllers.changeActivity

import controllers.ControllerHelper
import controllers.actions._
import forms.changeActivity.RemoveWarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.SelectChange.ChangeActivity
import models.backend.Site
import models.{Mode, UserAnswers}
import navigation._
import pages.changeActivity.RemoveWarehouseDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.changeActivity.RemoveWarehouseDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveWarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForChangeActivity,
                                       controllerActions: ControllerActions,
                                       formProvider: RemoveWarehouseDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RemoveWarehouseDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      request.userAnswers.warehouseList.get(index) match {
        case Some(warehouse) =>
          val formattedAddress = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
          Ok(view(form, formattedAddress, index, mode))
        case _ => indexNotFoundRedirect(index, request, controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(mode))
      }
  }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      val warehouseToRemove: Option[Site] = request.userAnswers.warehouseList.get(index)
      warehouseToRemove match {
        case None =>
          Future.successful(indexNotFoundRedirect(index, request, controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(mode)))
        case Some(warehouse) =>
          val formattedAddress: Html = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, formattedAddress, index, mode))),
            value => {
              val updatedAnswersFinal: UserAnswers = if (value) {
                request.userAnswers.copy(warehouseList = request.userAnswers.warehouseList.removed(index))
              } else {
                request.userAnswers
              }
              updateDatabaseAndRedirect(updatedAnswersFinal, RemoveWarehouseDetailsPage, mode = mode)
            }
          )
      }
  }
}
