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
import forms.changeActivity.ImportsFormProvider
import handlers.ErrorHandler
import models.Mode
import models.SelectChange.ChangeActivity
import navigation._
import pages.changeActivity.{HowManyImportsPage, ImportsPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.ImportsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForChangeActivity,
                                         controllerActions: ControllerActions,
                                         requiredUserAnswers: RequiredUserAnswersForChangeActivity,
                                         formProvider: ImportsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ImportsView,
                                         val genericLogger: GenericLogger,
                                         val errorHandler: ErrorHandler
                                 )(implicit val ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      requiredUserAnswers.requireData(ImportsPage, request.userAnswers, request.subscription) {
        val preparedForm = request.userAnswers.get(ImportsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>

      val userAnswers = request.userAnswers
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = userAnswers.setAndRemoveLitresIfReq(ImportsPage, HowManyImportsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, ImportsPage, mode)
          }
      )
  }
}
