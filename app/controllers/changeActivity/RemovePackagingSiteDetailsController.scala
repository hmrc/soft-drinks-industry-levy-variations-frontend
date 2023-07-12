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

import controllers.actions._
import controllers.ControllerHelper
import forms.changeActivity.RemovePackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.backend.Site
import models.{Mode, UserAnswers}
import navigation._
import pages.changeActivity.RemovePackagingSiteDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.changeActivity.RemovePackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.SelectChange.ChangeActivity

class RemovePackagingSiteDetailsController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      val sessionService: SessionService,
                                                      val navigator: NavigatorForChangeActivity,
                                                      controllerActions: ControllerActions,
                                                      formProvider: RemovePackagingSiteDetailsFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: RemovePackagingSiteDetailsView,
                                                      val genericLogger: GenericLogger,
                                                      val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      request.userAnswers.packagingSiteList.get(index) match {
        case Some(site) =>
          val formattedAddress = AddressFormattingHelper.addressFormatting(site.address, site.tradingName)
          Ok(view(form, mode, formattedAddress, index))
        case _ => genericLogger.logger.warn(s"Packing Site index $index doesn't exist ${request.userAnswers.id} packing site list length:" +
          s"${request.userAnswers.packagingSiteList.size}")
          Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode).url)
      }
  }

  def onSubmit(mode: Mode, index: String): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      val packagingSiteToRemove: Option[Site] = request.userAnswers.packagingSiteList.get(index)
      packagingSiteToRemove match {
        case None =>
          genericLogger.logger.warn(s"Packing Site index $index doesn't exist ${request.userAnswers.id} packing site list length:" +
            s"${request.userAnswers.packagingSiteList.size}")
          Future.successful(Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode).url))
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
              updateDatabaseAndRedirect(updatedAnswersFinal, RemovePackagingSiteDetailsPage, mode)
            }
          )
      }
  }
}
