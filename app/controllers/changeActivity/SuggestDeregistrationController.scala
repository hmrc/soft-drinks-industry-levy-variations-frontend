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
import controllers.actions._
import handlers.ErrorHandler
import models.NormalMode
import models.SelectChange.ChangeActivity
import navigation.NavigatorForChangeActivity
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeActivity.SuggestDeregistrationView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SuggestDeregistrationController @Inject() (
  override val messagesApi: MessagesApi,
  controllerActions: ControllerActions,
  val navigator: NavigatorForChangeActivity,
  connector: SoftDrinksIndustryLevyConnector,
  val controllerComponents: MessagesControllerComponents,
  val errorHandler: ErrorHandler,
  view: SuggestDeregistrationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) { implicit request =>
    Ok(view())
  }

  def onSubmit: Action[AnyContent] =
    controllerActions.withRequiredJourneyData(ChangeActivity).async { implicit request =>
      connector.returnsPending(request.subscription.utr).value.flatMap {
        case Right(returns) if returns.nonEmpty =>
          Future
            .successful(Redirect(controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad()))
        case Right(_) =>
          Future.successful(Redirect(controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)))
        case _ =>
          errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
      }
    }
}
