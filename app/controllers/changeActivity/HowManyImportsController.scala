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
import forms.HowManyLitresFormProvider
import handlers.ErrorHandler
import models.{Mode, NormalMode}
import navigation._
import pages.changeActivity.{ContractPackingPage, HowManyImportsPage, ImportsPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.HowManyImportsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.SelectChange.ChangeActivity

class HowManyImportsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForChangeActivity,
                                         controllerActions: ControllerActions,
                                         formProvider: HowManyLitresFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: HowManyImportsView,
                                         val genericLogger: GenericLogger,
                                         val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>

      val preparedForm = request.userAnswers.get(HowManyImportsPage) match {
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
          val updatedAnswers = request.userAnswers.set(HowManyImportsPage, value)
          val contractPacker = request.userAnswers.get(ContractPackingPage).getOrElse(false)
          val hasProductionSites = request.subscription.productionSites.nonEmpty

          (contractPacker, hasProductionSites, mode) match {
            case(true, false, NormalMode) =>
              updateDatabaseWithoutRedirect(updatedAnswers, HowManyImportsPage).flatMap {
                case true => Future.successful(Redirect(routes.PackAtBusinessAddressController.onPageLoad(mode)))
                case false => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
              }
            case _ => updateDatabaseAndRedirect(updatedAnswers, HowManyImportsPage, mode)
          }
        }
      )
  }
}
