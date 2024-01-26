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
import errors.ReturnsStillPending
import forms.SelectChangeFormProvider
import handlers.ErrorHandler
import models.{NormalMode, SelectChange}
import orchestrators.SelectChangeOrchestrator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SelectChangeView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelectChangeController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        selectChangeOrchestrator: SelectChangeOrchestrator,
                                        sessionService: SessionService,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        formProvider: SelectChangeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: SelectChangeView,
                                        val errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val preparedForm = request.userAnswers match {
        case Some(userAnswers) if !userAnswers.submitted => form.fill(userAnswers.journeyType)
        case _ => form
      }
      selectChangeOrchestrator.hasReturnsToCorrect(request.subscription).value.map {
        case Right(hasVariableReturns) =>
          request.subscription.deregDate match {
            case None => Ok(view(preparedForm, hasVariableReturns))
            case _ => Ok(view(preparedForm, hasVariableReturns,isDeregistered = true))
          }

        case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => selectChangeOrchestrator.hasReturnsToCorrect(request.subscription).value
          .map{
            case Right(hasVariableReturns) => BadRequest(view(formWithErrors, hasVariableReturns))
            case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
          },
        value => {
          selectChangeOrchestrator.createUserAnswersAndSaveToDatabase(value, request.subscription).value.map{
            case Right(_) => Redirect(getRedirectUrl(value))
            case Left(ReturnsStillPending) => Redirect(cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad())
            case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
          }
        }
      )
  }

  private def getRedirectUrl(value: SelectChange): Call = {
    value match {
      case SelectChange.UpdateRegisteredDetails => updateRegisteredDetails.routes.ChangeRegisteredDetailsController.onPageLoad()
      case SelectChange.ChangeActivity => changeActivity.routes.AmountProducedController.onPageLoad(NormalMode)
      case SelectChange.CorrectReturn => correctReturn.routes.SelectController.onPageLoad
      case _ => cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)
    }
  }

}
