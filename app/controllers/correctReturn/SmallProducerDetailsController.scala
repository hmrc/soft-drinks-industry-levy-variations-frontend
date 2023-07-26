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
import forms.correctReturn.SmallProducerDetailsFormProvider

import javax.inject.Inject
import models.{Mode, SmallProducer}
import models.SelectChange.CorrectReturn
import pages.correctReturn.SmallProducerDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.correctReturn.SmallProducerDetailsView
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import play.api.data.Form

class SmallProducerDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: SmallProducerDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: SmallProducerDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>
      val smallProducerList:List[SmallProducer] = request.userAnswers.smallProducerList
      val preparedForm = request.userAnswers.get(SmallProducerDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, smallProducerList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
      val smallProducerList:List[SmallProducer] = request.userAnswers.smallProducerList
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, smallProducerList))),

        value => {
          val updatedAnswers = request.userAnswers.set(SmallProducerDetailsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, SmallProducerDetailsPage, mode)
        }
      )
  }

}
