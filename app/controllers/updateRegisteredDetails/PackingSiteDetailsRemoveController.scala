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

package controllers.updateRegisteredDetails

import utilities.GenericLogger
import controllers.{ControllerHelper, routes}
import controllers.actions._
import forms.updateRegisteredDetails.PackingSiteDetailsRemoveFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import pages.updateRegisteredDetails.PackingSiteDetailsRemovePage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.updateRegisteredDetails.PackingSiteDetailsRemoveView
import viewmodels.AddressFormattingHelper
import handlers.ErrorHandler
import models.backend.Site

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import play.twirl.api.Html

class PackingSiteDetailsRemoveController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForUpdateRegisteredDetails,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PackingSiteDetailsRemoveFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackingSiteDetailsRemoveView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.packagingSiteList.get(index) match {
        case Some(site) =>
          val formattedAddress = AddressFormattingHelper.addressFormatting(site.address, site.tradingName)
          Ok(view(form, mode, formattedAddress, index))
        case _ => genericLogger.logger.warn(s"Packing Site index $index doesn't exist ${request.userAnswers.id} packing site list length:" +
          s"${request.userAnswers.packagingSiteList.size}")
          Redirect(routes.IndexController.onPageLoad)
      }
  }

  def onSubmit(mode: Mode, index: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val warehouseToRemove: Option[Site] = request.userAnswers.packagingSiteList.get(index)
      warehouseToRemove match {
        case None =>
          genericLogger.logger.warn(s"Packing Site index $index doesn't exist ${request.userAnswers.id} packing site list length:" +
            s"${request.userAnswers.packagingSiteList.size}")
          Future.successful(Redirect(routes.IndexController.onPageLoad))
        case Some(site) =>
          val formattedAddress: Html = AddressFormattingHelper.addressFormatting(site.address, site.tradingName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress, index))),
            value => {
              val updatedAnswersFinal: UserAnswers = if (value) {
                request.userAnswers.copy(packagingSiteList = request.userAnswers.packagingSiteList.removed(index))
              } else {
                request.userAnswers
              }
              updateDatabaseAndRedirect(updatedAnswersFinal, PackingSiteDetailsRemovePage, mode)
            }
          )
      }
  }
}
