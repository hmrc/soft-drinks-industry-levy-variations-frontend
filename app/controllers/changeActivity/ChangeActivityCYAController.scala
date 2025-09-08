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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{ControllerActions, RequiredUserAnswersForChangeActivity}
import handlers.ErrorHandler
import models.SelectChange.ChangeActivity
import models.UserAnswers
import models.backend.RetrievedSubscription
import models.requests.DataRequest
import orchestrators.ChangeActivityOrchestrator
import pages.changeActivity.ChangeActivityCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.changeActivity.ChangeActivityCYAView
import views.summary.changeActivity.ChangeActivitySummary

import scala.concurrent.{ExecutionContext, Future}

class ChangeActivityCYAController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             controllerActions: ControllerActions,
                                             requiredUserAnswers: RequiredUserAnswersForChangeActivity,
                                             implicit val config: FrontendAppConfig,
                                             val controllerComponents: MessagesControllerComponents,
                                             changeActivityOrchestrator: ChangeActivityOrchestrator,
                                             genericLogger: GenericLogger,
                                             view: ChangeActivityCYAView,
                                             errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      requiredUserAnswers.requireData(ChangeActivityCYAPage, request.userAnswers, request.subscription) {
        val orgName: String = " " + request.subscription.orgName
        val sections = ChangeActivitySummary.summaryListsAndHeadings(request.userAnswers, isCheckAnswers = true)
        Future.successful(Ok(view(orgName, sections, routes.ChangeActivityCYAController.onSubmit)))
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async { implicit request =>
    val userAnswers = request.userAnswers
    val subscription = request.subscription
    submitUserAnswers(userAnswers, subscription)
  }

  private def submitUserAnswers(userAnswers: UserAnswers, subscription: RetrievedSubscription)(implicit request: DataRequest[AnyContent]):Future[Result]  = {
    changeActivityOrchestrator.submitVariation(subscription, userAnswers).value.flatMap {
      case Right(_) => Future.successful(
        Redirect(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad()))
      case Left(_) => genericLogger.logger.error(s"${getClass.getName} - ${userAnswers.id} - failed to submit change activity variation")
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
    }
  }
}

