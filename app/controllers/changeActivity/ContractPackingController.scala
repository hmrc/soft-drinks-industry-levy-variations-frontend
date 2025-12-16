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
import forms.changeActivity.ContractPackingFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation._
import pages.changeActivity.{ ContractPackingPage, HowManyContractPackingPage }
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.ContractPackingView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import models.SelectChange.ChangeActivity
import play.api.data.Form

class ContractPackingController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: NavigatorForChangeActivity,
  controllerActions: ControllerActions,
  requiredUserAnswers: RequiredUserAnswersForChangeActivity,
  formProvider: ContractPackingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContractPackingView,
  val genericLogger: GenericLogger,
  val errorHandler: ErrorHandler
)(implicit val ec: ExecutionContext)
    extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    controllerActions.withRequiredJourneyData(ChangeActivity).async { implicit request =>
      requiredUserAnswers.requireData(ContractPackingPage, request.userAnswers, request.subscription) {
        val preparedForm = request.userAnswers.get(ContractPackingPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Future.successful(Ok(view(preparedForm, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    controllerActions.withRequiredJourneyData(ChangeActivity).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val updatedAnswers =
              request.userAnswers.setAndRemoveLitresIfReq(ContractPackingPage, HowManyContractPackingPage, value)
            updateDatabaseAndRedirect(updatedAnswers, ContractPackingPage, mode)
          }
        )
    }
}
