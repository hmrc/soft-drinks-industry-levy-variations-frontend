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
import controllers.routes
import handlers.ErrorHandler
import models.SdilReturn
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.CorrectReturnUpdateDonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnUpdateDoneView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnUpdateDoneController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                            val controllerComponents: MessagesControllerComponents,
                                            val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                            genericLogger: GenericLogger,
                                            view: CorrectReturnUpdateDoneView,
                                            errorHandler: ErrorHandler
                                          )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnUpdateDonePage) {
        request.userAnswers.getCorrectReturnOriginalSDILReturnData.map(originalSdilReturn => {
            val calculateAmounts = correctReturnOrchestrator.calculateAmounts(request.sdilEnrolment, request.userAnswers, request.returnPeriod)
            val result = calculateAmounts.value.map {
              case Right(amounts) =>
                val orgName: String = " " + request.subscription.orgName
                val currentSDILReturn = SdilReturn.apply(request.userAnswers)
                val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)
                val returnPeriod = request.userAnswers.correctReturnPeriod
                val sections = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(
                  request.userAnswers, request.subscription, changedPages, isCheckAnswers = false, amounts)
                request.userAnswers.submittedOn match {
                  case Some(submittedOnDate) =>
                    val getSentDateTime = LocalDateTime.ofInstant(submittedOnDate, ZoneId.of("Europe/London"))
                    val formattedDate = getSentDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                    val formattedTime = getSentDateTime.format(DateTimeFormatter.ofPattern("h:mma"))

                    val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
                    val returnPeriodStart = returnPeriod.head.start.format(returnPeriodFormat)
                    val returnPeriodEnd = returnPeriod.head.end.format(returnPeriodFormat)

                    Ok(view(orgName, sections, formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd))
                  case None => genericLogger.logger
                    .error(s"[SoftDrinksIndustryLevyService [submitVariation] - unexpected response while attempting to retreive userAnswers submittedOnDate")
                    Redirect(routes.SelectChangeController.onPageLoad)
                }
              case Left(_) =>
                genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
                Redirect(controllers.routes.SelectChangeController.onPageLoad.url)
            }
            result
          }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
      }
  }
}
