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
import models.ReturnPeriod
import models.SelectChange.CancelRegistration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancelRegistration.CancellationRequestDoneView

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import javax.inject.Inject


class CancellationRequestDoneController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   controllerActions: ControllerActions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CancellationRequestDoneView
                                                 )(implicit config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(CancelRegistration) {

    implicit request =>
      val getSentDateTime = LocalDateTime.now(ZoneId.of("UTC")) //LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("UTC"))
      val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
      val timeFormatter = DateTimeFormatter.ofPattern("H:MMa")
      val formattedDate = getSentDateTime.format(dateFormatter)
      val formattedTime = getSentDateTime.format(timeFormatter)

      val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
      val nextReturnPeriod = ReturnPeriod(getSentDateTime.toLocalDate).next
      val returnPeriodStart = nextReturnPeriod.start.format(returnPeriodFormat)
      val returnPeriodEnd = nextReturnPeriod.end.format(returnPeriodFormat)

      val deadlineStartFormat = DateTimeFormatter.ofPattern("d MMMM")
      val deadlineStart = nextReturnPeriod.end.plusDays(1).format(deadlineStartFormat)

      val deadlineEndFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")
      val deadlineEnd = nextReturnPeriod.deadline.format(deadlineEndFormat)

      Ok(view(formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd, deadlineStart, deadlineEnd, request.subscription.orgName))
  }
}
