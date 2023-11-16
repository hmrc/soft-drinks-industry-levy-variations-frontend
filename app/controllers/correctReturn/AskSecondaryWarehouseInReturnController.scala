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

import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.AskSecondaryWarehouseInReturnFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation._
import pages.correctReturn.AskSecondaryWarehouseInReturnPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import services.{AddressLookupService, SessionService, WarehouseDetails}
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import views.html.correctReturn.AskSecondaryWarehouseInReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AskSecondaryWarehouseInReturnController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: AskSecondaryWarehouseInReturnFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AskSecondaryWarehouseInReturnView,
                                       addressLookupService: AddressLookupService,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val preparedForm = request.userAnswers.get(AskSecondaryWarehouseInReturnPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          updateDatabaseWithoutRedirect(request.userAnswers.set(AskSecondaryWarehouseInReturnPage, value), AskSecondaryWarehouseInReturnPage).flatMap {
            case true => getOnwardUrl(value, mode).map(Redirect(_))
            case false => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }
      )
  }

  private def getOnwardUrl(value: Boolean, mode: Mode)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages,
                                                       requestHeader: RequestHeader): Future[String] = {
    if (value) {
      addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode)(hc, ec, messages, requestHeader)
    } else {
      Future.successful(controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad.url)
    }
  }
}
