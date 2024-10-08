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

package controllers.cancelRegistration

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.ControllerActions
import controllers.routes
import handlers.ErrorHandler
import models.SelectChange
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancelRegistration.FileReturnBeforeDeregView
import views.summary.cancelRegistration.FileReturnBeforeDeregSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileReturnBeforeDeregController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 controllerActions: ControllerActions,
                                                 connector: SoftDrinksIndustryLevyConnector,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: FileReturnBeforeDeregView,
                                                 errorHandler: ErrorHandler
                                               )(implicit ec: ExecutionContext, config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(SelectChange.CancelRegistration).async {
    implicit request =>
    connector.getPendingReturnsFromCache(request.subscription.utr).value.flatMap {
      case Right(returns) if returns.nonEmpty => Future.successful(
        Ok(view(FileReturnBeforeDeregSummary.displayMessage(returns))))
      case Right(_) => Future.successful(Redirect(routes.SelectChangeController.onPageLoad))
      case Left(_) =>
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
    }
  }

  def onSubmit(): Action[AnyContent] = controllerActions.withRequiredJourneyData(SelectChange.CancelRegistration) {
    _ => Redirect(config.sdilHomeUrl)
  }

}
