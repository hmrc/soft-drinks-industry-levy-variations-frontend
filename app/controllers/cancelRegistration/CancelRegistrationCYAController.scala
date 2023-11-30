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

import com.google.inject.Inject
import controllers.actions.ControllerActions
import handlers.ErrorHandler
import models.NormalMode
import models.SelectChange.CancelRegistration
import models.requests.DataRequest
import orchestrators.CancelRegistrationOrchestrator
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.cancelRegistration.{CancelRegistrationDateSummary, ReasonSummary}
import views.html.cancelRegistration.CancelRegistrationCYAView

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class CancelRegistrationCYAController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 controllerActions: ControllerActions,
                                                 cancelRegistrationOrchestrator: CancelRegistrationOrchestrator,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: CancelRegistrationCYAView,
                                                 sessionService : SessionService,
                                                 errorHandler: ErrorHandler,
                                                 genericLogger: GenericLogger
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with SummaryListFluency {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(CancelRegistration) {
    implicit request =>
      withRequiredUserAnswers match {
        case Right(_) =>
          val orgName: String = " " + request.subscription.orgName
          val cancelRegistrationSummary: (String, SummaryList) = ("", SummaryListViewModel(
            rows = Seq(ReasonSummary.row(request.userAnswers), CancelRegistrationDateSummary.row(request.userAnswers)))
          )
          val list = Seq(cancelRegistrationSummary)
          Ok(view(orgName, list, routes.CancelRegistrationCYAController.onSubmit))
        case Left(call) => Redirect(call)
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CancelRegistration).async { implicit request =>
    withRequiredUserAnswers match {
      case Right(_) =>
        val subscription = request.subscription
        val userAnswers = request.userAnswers.copy(submittedOn = Some(Instant.now))
        sessionService.set(userAnswers).map {
          case Right(_) => true
          case Left(_) => genericLogger.logger.error(s"Failed to set value in session repository while attempting set on submittedOn")
            false
        }
        cancelRegistrationOrchestrator.submitVariation(subscription, userAnswers).value.map {
          case Right(_) =>
            Redirect(routes.CancellationRequestDoneController.onPageLoad.url)
          case Left(_) => genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - failed to cancel registration")
            InternalServerError(errorHandler.internalServerErrorTemplate)
        }
      case Left(call) => Future.successful(Redirect(call))
    }
  }

  private def withRequiredUserAnswers(implicit request: DataRequest[AnyContent]): Either[Call, Unit] = {
    (request.userAnswers.get(CancelRegistrationDatePage), request.userAnswers.get(ReasonPage)) match {
      case (None, None) =>
        Left(controllers.routes.SelectChangeController.onPageLoad)
      case (_, None) =>
        Left(routes.ReasonController.onPageLoad(NormalMode))
      case (None, _) =>
        Left(routes.CancelRegistrationDateController.onPageLoad(NormalMode))
      case _ => Right((): Unit)
    }
  }
}
