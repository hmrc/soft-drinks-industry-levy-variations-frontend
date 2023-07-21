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

package controllers.correctReturn

import utilities.GenericLogger
import controllers.actions._
import forms.correctReturn.CorrectionReasonFormProvider

import javax.inject.Inject
import models.Mode
import pages.correctReturn.CorrectionReasonPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.correctReturn.CorrectionReasonView
import handlers.ErrorHandler
import controllers.ControllerHelper
import models.SelectChange.CorrectReturn

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import play.api.data.Form

class CorrectionReasonController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                        controllerActions: ControllerActions,
                                       formProvider: CorrectionReasonFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: CorrectionReasonView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CorrectionReasonPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set(CorrectionReasonPage, value)
          updateDatabaseAndRedirect(updatedAnswers, CorrectionReasonPage, mode)
        }
      )
  }
}
