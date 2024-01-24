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
import scala.concurrent.ExecutionContext

class CorrectReturnUpdateDoneController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   controllerActions: ControllerActions,
                                                   val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                                   genericLogger: GenericLogger,
                                                   view: CorrectReturnUpdateDoneView
                                                 )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyDataNew(ignoreSubmitted = true).async {
    implicit request =>
      val calculateAmounts = correctReturnOrchestrator.calculateAmounts(request.sdilEnrolment, request.userAnswers, request.returnPeriod, request.originalSdilReturn)
      calculateAmounts.value.map {
        case Right(amounts) =>
          val orgName: String = " " + request.subscription.orgName
          val currentSDILReturn = SdilReturn.generateFromUserAnswers(request.userAnswers)
          val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(request.originalSdilReturn, currentSDILReturn)
          val getSentDateTime = LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("Europe/London"))
          val formattedDate = getSentDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
          val formattedTime = getSentDateTime.format(DateTimeFormatter.ofPattern("h:mma"))
          val returnPeriod = request.userAnswers.correctReturnPeriod
          val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
          val returnPeriodStart = returnPeriod.head.start.format(returnPeriodFormat)
          val returnPeriodEnd = returnPeriod.head.end.format(returnPeriodFormat)
          val sections = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(request.userAnswers,
            request.subscription, changedPages, isCheckAnswers = false, amounts)
          Ok(view(orgName, sections, formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd))
        case Left(_) =>
          genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
          Redirect(controllers.routes.SelectChangeController.onPageLoad.url)
      }
  }
}
