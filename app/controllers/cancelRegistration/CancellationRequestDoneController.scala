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

import config.FrontendAppConfig
import controllers.actions._
import controllers.routes
import handlers.ErrorHandler
import models.ReturnPeriod
import models.SelectChange.CancelRegistration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.cancelRegistration.{CancelRegistrationDateSummary, ReasonSummary}
import views.html.cancelRegistration.CancellationRequestDoneView

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import javax.inject.Inject


class CancellationRequestDoneController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   controllerActions: ControllerActions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CancellationRequestDoneView,
                                                   genericLogger: GenericLogger,
                                                   val errorHandler: ErrorHandler
                                                 )(implicit config: FrontendAppConfig) extends FrontendBaseController with I18nSupport with SummaryListFluency {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(CancelRegistration, ignoreSubmitted = true) {

    implicit request =>

      request.userAnswers.submittedOn match {
        case Some(submittedOnDate) =>
          val getSentDateTime = LocalDateTime.ofInstant(submittedOnDate, ZoneId.of("Europe/London"))
          val formattedDate = getSentDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
          val formattedTime = getSentDateTime.format(DateTimeFormatter.ofPattern("h:mma"))

          val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
          val nextReturnPeriod = ReturnPeriod(getSentDateTime.toLocalDate)
          val returnPeriodStart = nextReturnPeriod.start.format(returnPeriodFormat)
          val returnPeriodEnd = nextReturnPeriod.end.format(returnPeriodFormat)

          val deadlineStartFormat = DateTimeFormatter.ofPattern("d MMMM")
          val deadlineStart = nextReturnPeriod.end.plusDays(1).format(deadlineStartFormat)

          val deadlineEndFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")
          val deadlineEnd = nextReturnPeriod.deadline.format(deadlineEndFormat)

          val cancelRegistrationSummary: (String, SummaryList) = ("", SummaryListViewModel(
            rows = Seq(
              ReasonSummary.row(request.userAnswers, isCheckAnswers = false),
              CancelRegistrationDateSummary.row(request.userAnswers, isCheckAnswers = false)
            ))
          )

          Ok(view(
            formattedDate, formattedTime,
            returnPeriodStart, returnPeriodEnd,
            deadlineStart, deadlineEnd,
            request.subscription.orgName, Seq(cancelRegistrationSummary)))

        case None => genericLogger.logger.error(s"[SoftDrinksIndustryLevyService [submitVariation] - unexpected response while attempting to retreive userAnswers submittedOnDate")
          Redirect(routes.SelectChangeController.onPageLoad)
      }
  }
}
