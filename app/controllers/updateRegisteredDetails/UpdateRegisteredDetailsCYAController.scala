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

package controllers.updateRegisteredDetails

import com.google.inject.Inject
import controllers.actions.ControllerActions
import handlers.ErrorHandler
import models.SelectChange.UpdateRegisteredDetails
import orchestrators.UpdateRegisteredDetailsOrchestrator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.updateRegisteredDetails.UpdateRegisteredDetailsCYAView
import views.summary.updateRegisteredDetails.{BusinessAddressSummary, UKSitesSummary, UpdateContactDetailsSummary}

import scala.concurrent.{ExecutionContext, Future}

class UpdateRegisteredDetailsCYAController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      controllerActions: ControllerActions,
                                                      updateRegisteredDetailsOrchestrator: UpdateRegisteredDetailsOrchestrator,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      genericLogger: GenericLogger,
                                                      view: UpdateRegisteredDetailsCYAView,
                                                      errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      val ukSiteDetailsSummary: Option[(String, SummaryList)] = UKSitesSummary.getHeadingAndSummary(request.userAnswers, true)
      val updateContactDetailsSummary: Option[(String, SummaryList)] = UpdateContactDetailsSummary.rows(request.userAnswers)
      val businessAddressSummary: Option[(String, SummaryList)] = BusinessAddressSummary.rows(request.userAnswers)
      val summaryList = Seq(ukSiteDetailsSummary, updateContactDetailsSummary, businessAddressSummary).flatten
      val orgName: String = " " + request.subscription.orgName

      Ok(view(orgName, summaryList, routes.UpdateRegisteredDetailsCYAController.onSubmit))
  }
  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {implicit request =>
    val subscription = request.subscription
    val userAnswers = request.userAnswers
    updateRegisteredDetailsOrchestrator.submitVariation(subscription, userAnswers).value.flatMap {
      case Right(_) => Future.successful(
        Redirect(routes.UpdateDoneController.onPageLoad.url)
      )
      case Left(_) => genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - failed to update registered details")
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
    }
  }

}
