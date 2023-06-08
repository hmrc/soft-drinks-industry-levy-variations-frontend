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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancelRegistration.FileReturnBeforeDeregView
import views.summary.cancelRegistration.FileReturnBeforeDeregSummary

import javax.inject.Inject
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class FileReturnBeforeDeregController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 service: ReturnService,
                                                 connector: SoftDrinksIndustryLevyConnector,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: FileReturnBeforeDeregView
                                               ) extends FrontendBaseController with I18nSupport {



  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
    connector.returns_pending(request.subscription.utr).map(returns => returns match {
        case Some(returns)  if returns.size == 1 =>  Ok(view(FileReturnBeforeDeregSummary.displayMessage(showReturnSize = None, showReturnDate = Some(returns))))
        case Some(returns) if returns.size > 1 => Ok(view(FileReturnBeforeDeregSummary.displayMessage(showReturnSize = Some(returns.size), showReturnDate = None)))
        case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
      })
  }
}
