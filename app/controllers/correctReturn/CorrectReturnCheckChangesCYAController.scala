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
import controllers.ControllerHelper
import controllers.actions.{ControllerActions, RequiredUserAnswersForCorrectReturn}
import handlers.ErrorHandler
import models.SdilReturn
import models.correctReturn.ChangedPage
import navigation.NavigatorForCorrectReturn
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.{BalanceRepaymentRequired, CorrectReturnCheckChangesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CorrectReturnCheckChangesCYAController @Inject()(
                                                        override val messagesApi: MessagesApi,
                                                        val sessionService: SessionService,
                                                        val navigator: NavigatorForCorrectReturn,
                                                        controllerActions: ControllerActions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                                        view: CorrectReturnCheckChangesCYAView,
                                                        correctReturnOrchestrator: CorrectReturnOrchestrator,
                                                        val genericLogger: GenericLogger,
                                                        val errorHandler: ErrorHandler
                                                      )(implicit config: FrontendAppConfig, val ec: ExecutionContext)
  extends ControllerHelper with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      val calculateAmounts = correctReturnOrchestrator.calculateAmounts(
        request.sdilEnrolment, request.userAnswers, request.returnPeriod, request.originalSdilReturn)

      calculateAmounts.value.flatMap {
        case Right(amounts) =>
          val balanceRepaymentRequired = amounts.newReturnTotal < amounts.originalReturnTotal
          val updatedAnswers = request.userAnswers.set(BalanceRepaymentRequired, balanceRepaymentRequired)
          updateDatabaseWithoutRedirect(updatedAnswers, CorrectReturnCheckChangesPage).flatMap(_ => {
            updatedAnswers match {
              case Failure(_) =>
                genericLogger.logger.error(s"Failed to resolve user answers while on ${CorrectReturnCheckChangesPage.toString}")
                Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
              case Success(answers) =>
                requiredUserAnswers.requireData(CorrectReturnCheckChangesPage, answers, request.subscription) {
                  val currentSDILReturn = SdilReturn.generateFromUserAnswers(request.userAnswers)
                  val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(request.originalSdilReturn, currentSDILReturn)
                  val orgName: String = " " + request.subscription.orgName
                  val sections = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(request.userAnswers,
                    request.subscription, changedPages, isCheckAnswers = true, amounts)
                  Future.successful(Ok(view(orgName, sections, routes.CorrectReturnCheckChangesCYAController.onSubmit)))
                }
            }
          })
        case Left(_) =>
          genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
          Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
      }
    }

  def onSubmit: Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
    correctReturnOrchestrator.submitReturn(request.userAnswers, request.subscription, request.returnPeriod, request.originalSdilReturn).value.map {
      case Right(_) => Redirect(routes.CorrectReturnUpdateDoneController.onPageLoad.url)
      case Left(_) => genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - received a failed response from return submission")
        InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }
}
