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

package controllers.cancelRegistration

import controllers.ControllerHelper
import controllers.actions._
import forms.cancelRegistration.ReasonFormProvider
import handlers.ErrorHandler
import models.{Mode, SelectChange}
import navigation._
import pages.cancelRegistration.ReasonPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.cancelRegistration.ReasonView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReasonController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCancelRegistration,
                                       controllerActions: ControllerActions,
                                       formProvider: ReasonFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReasonView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(SelectChange.CancelRegistration) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ReasonPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(SelectChange.CancelRegistration).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set(ReasonPage, value)
          updateDatabaseAndRedirect(updatedAnswers, ReasonPage, mode)
        }
      )
  }
}
