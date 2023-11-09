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
import forms.changeActivity.ThirdPartyPackagersFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation.NavigatorForChangeActivity
import pages.changeActivity.ThirdPartyPackagersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.ThirdPartyPackagersView
import models.SelectChange.ChangeActivity

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ThirdPartyPackagersController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               val sessionService: SessionService,
                                               val navigator: NavigatorForChangeActivity,
                                               controllerActions: ControllerActions,
                                               formProvider: ThirdPartyPackagersFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: ThirdPartyPackagersView,
                                               val errorHandler: ErrorHandler,
                                               val genericLogger: GenericLogger
                                             )(implicit val ec: ExecutionContext) extends ControllerHelper  with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ThirdPartyPackagersPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val updatedAnswers = request.userAnswers.set(ThirdPartyPackagersPage, value)
          updateDatabaseAndRedirect(updatedAnswers, ThirdPartyPackagersPage, mode)
        }
      )
  }
}