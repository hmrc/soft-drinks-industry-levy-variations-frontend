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
import models.{ReturnPeriod, SdilReturn}
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.CorrectReturnUpdateDonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.correctReturn.CorrectReturnUpdateDoneView
import views.summary.correctReturn.CorrectReturnUpdateDoneSummary

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnUpdateDoneController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                            val controllerComponents: MessagesControllerComponents,
                                            val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                            view: CorrectReturnUpdateDoneView
                                          )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnUpdateDonePage) {
        (for {
          originalSdilReturn <- request.userAnswers.getCorrectReturnOriginalSDILReturnData
          returnPeriod <- request.userAnswers.correctReturnPeriod
        } yield {
          val orgName: String = " " + request.subscription.orgName
          val currentSDILReturn = SdilReturn.apply(request.userAnswers)
          val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)
          val sections = CorrectReturnUpdateDoneSummary.changeSpecificSummaryListAndHeadings(request.userAnswers, request.subscription, changedPages)

          val getSentDateTime = LocalDateTime.now(ZoneId.of("UTC")) //LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("UTC"))
          val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
          val timeFormatter = DateTimeFormatter.ofPattern("H:MMa")
          val formattedDate = getSentDateTime.format(dateFormatter)
          val formattedTime = getSentDateTime.format(timeFormatter)

          val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
          val returnPeriodStart = returnPeriod.start.format(returnPeriodFormat)
          val returnPeriodEnd = returnPeriod.end.format(returnPeriodFormat)

          Future.successful(Ok(view(orgName, sections, formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd, config.sdilHomeUrl)))
        }).getOrElse(Future(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
      }
  }

}
