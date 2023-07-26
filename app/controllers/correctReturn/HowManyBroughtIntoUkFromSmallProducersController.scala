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
import controllers.ControllerHelper
import controllers.actions._
import forms.HowManyLitresFormProvider
import javax.inject.Inject
import models.Mode
import models.SelectChange.CorrectReturn
import pages.correctReturn.HowManyBroughtIntoUkFromSmallProducersPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.correctReturn.HowManyBroughtIntoUkFromSmallProducersView
import handlers.ErrorHandler
import navigation._

import scala.concurrent.{ExecutionContext, Future}

class HowManyBroughtIntoUkFromSmallProducersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForCorrectReturn,
                                         controllerActions: ControllerActions,
                                         formProvider: HowManyLitresFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: HowManyBroughtIntoUkFromSmallProducersView,
                                         val genericLogger: GenericLogger,
                                         val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>

      val preparedForm = request.userAnswers.get(HowManyBroughtIntoUkFromSmallProducersPage) match {
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
          val updatedAnswers = request.userAnswers.set(HowManyBroughtIntoUkFromSmallProducersPage, value)
          updateDatabaseAndRedirect(updatedAnswers, HowManyBroughtIntoUkFromSmallProducersPage, mode)
        }
      )
  }
}
