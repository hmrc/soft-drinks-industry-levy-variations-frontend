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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{ControllerActions, RequiredUserAnswersForCorrectReturn}
import errors.UnexpectedResponseFromSDIL
import handlers.ErrorHandler
import models.SdilReturn
import models.SelectChange.CorrectReturn
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.CorrectReturnUpdateDonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnCheckChangesCYAController @Inject()(
                                                        override val messagesApi: MessagesApi,
                                                        controllerActions: ControllerActions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                                        view: CorrectReturnCheckChangesCYAView,
                                                        correctReturnOrchestrator: CorrectReturnOrchestrator,
                                                        genericLogger: GenericLogger,
                                                        val errorHandler: ErrorHandler
                                                      )(implicit config: FrontendAppConfig, ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnUpdateDonePage) {
        request.userAnswers.getCorrectReturnOriginalSDILReturnData.map(originalSdilReturn => {
          val currentSDILReturn = SdilReturn.generateFromUserAnswers(request.userAnswers)
          val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)
          val calculateAmounts = correctReturnOrchestrator.calculateAmounts(request.sdilEnrolment, request.userAnswers, request.returnPeriod)

          val result = calculateAmounts.value.map {
            case Right(amounts) =>
              val orgName: String = " " + request.subscription.orgName
              val sections = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(request.userAnswers,
                request.subscription, changedPages, isCheckAnswers = true, amounts)
              Ok(view(orgName, sections, routes.CorrectReturnCheckChangesCYAController.onSubmit))
            case Left(_) =>
              genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
              Redirect(controllers.routes.SelectChangeController.onPageLoad.url)
          }
          result
        }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
    correctReturnOrchestrator.submitReturn(request.userAnswers, request.subscription).value.map {
      case Right(_) => Redirect(routes.CorrectReturnUpdateDoneController.onPageLoad.url)
      case Left(_) => genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - received a failed response from return submission")
        InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }
}
