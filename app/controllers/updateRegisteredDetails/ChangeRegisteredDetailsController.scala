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

import controllers.ControllerHelper
import controllers.actions._
import forms.updateRegisteredDetails.ChangeRegisteredDetailsFormProvider
import handlers.ErrorHandler
import models.NormalMode
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails
import navigation._
import pages.updateRegisteredDetails.ChangeRegisteredDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.updateRegisteredDetails.ChangeRegisteredDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeRegisteredDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        val sessionService: SessionService,
                                        val navigator: NavigatorForUpdateRegisteredDetails,
                                        controllerActions: ControllerActions,
                                        formProvider: ChangeRegisteredDetailsFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ChangeRegisteredDetailsView,
                                        val genericLogger: GenericLogger,
                                        val errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends ControllerHelper {

  def form(isVoluntary: Boolean): Form[Seq[ChangeRegisteredDetails]] = formProvider.apply(isVoluntary)

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      val isVoluntary: Boolean = request.subscription.activity.voluntaryRegistration
      val preparedForm = request.userAnswers.get(ChangeRegisteredDetailsPage) match {
        case None => form(isVoluntary)
        case Some(value) => form(isVoluntary).fill(value)
      }

      Ok(view(preparedForm, isVoluntary))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {
    implicit request =>
      val isVoluntary: Boolean = request.subscription.activity.voluntaryRegistration
      form(isVoluntary).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, isVoluntary))),

        value => {
          val updatedAnswers = request.userAnswers.set(ChangeRegisteredDetailsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, ChangeRegisteredDetailsPage, NormalMode)
        }
      )
  }
}
