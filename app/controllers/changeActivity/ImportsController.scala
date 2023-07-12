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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.ControllerHelper
import controllers.actions._
import forms.changeActivity.ImportsFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
import navigation._
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyImportsPage, ImportsPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.ImportsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import views.summary.cancelRegistration.FileReturnBeforeDeregSummary

class ImportsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForChangeActivity,
                                         controllerActions: ControllerActions,
                                         formProvider: ImportsFormProvider,
                                         connector: SoftDrinksIndustryLevyConnector,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ImportsView,
                                          val genericLogger: GenericLogger,
                                          val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ImportsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>

      val userAnswers = request.userAnswers

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = userAnswers.setAndRemoveLitresIfReq(ImportsPage, HowManyImportsPage, value)
          val contractPacker = userAnswers.get(ContractPackingPage).contains(true)
          val noneProduced = userAnswers.get(AmountProducedPage).contains(AmountProduced.None)

          if(noneProduced && !contractPacker && !value) {
            updateDatabaseWithoutRedirect(updatedAnswers, ImportsPage)(
              connector.returns_pending(request.subscription.utr).map {
                case Some(returns) if returns.nonEmpty =>
                  Redirect(controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad())
                case _ =>
                  Redirect(routes.SuggestDeregistrationController.onPageLoad())
              })
          } else {
            updateDatabaseAndRedirect(updatedAnswers, ImportsPage, mode)
          }
        }
      )
  }
}
