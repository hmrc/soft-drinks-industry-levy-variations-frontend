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

package controllers

import controllers.actions._
import forms.SelectChangeFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.SelectChangePage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.SelectChangeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectChangeController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        val sessionRepository: SessionRepository,
                                        val navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: SelectChangeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: SelectChangeView,
                                        val errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers match {
        case Some(userAnswers) =>
          userAnswers.get(SelectChangePage)
            .fold(form)(pageContent => form.fill(pageContent))
      }
      Ok(view(preparedForm, mode))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      request.userAnswers match {
        case optUserAnswers =>
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value => {
              val updatedAnswers = optUserAnswers
                .getOrElse(UserAnswers(id = request.sdilEnrolment))
                .set(SelectChangePage, value)
              updateDatabaseAndRedirect(updatedAnswers, SelectChangePage, mode)
            }
          )
      }
  }
}
