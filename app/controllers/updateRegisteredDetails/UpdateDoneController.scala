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

import config.FrontendAppConfig
import controllers.actions._
import controllers.routes

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.UpdateDoneView
import models.SelectChange.UpdateRegisteredDetails
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utilities.GenericLogger
import views.summary.updateRegisteredDetails.{BusinessAddressSummary, UKSitesSummary, UpdateContactDetailsSummary}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

class UpdateDoneController @Inject()(
                                       override val messagesApi: MessagesApi,
                                        controllerActions: ControllerActions,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: UpdateDoneView,
                                       genericLogger: GenericLogger
                                     )(implicit config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {

    implicit request =>
      request.userAnswers.submittedOn match {
        case Some(submittedOnDate) =>
          val ukSiteDetailsSummary: Option[(String, SummaryList)] = UKSitesSummary.getHeadingAndSummary (request.userAnswers, isCheckAnswers = false)
          val updateContactDetailsSummary: Option[(String, SummaryList)] = UpdateContactDetailsSummary.rows (request.userAnswers, isCheckAnswers = false)
          val businessAddressSummary: Option[(String, SummaryList)] = BusinessAddressSummary.rows (request.userAnswers, isCheckAnswers = false)
          val summaryList = Seq (ukSiteDetailsSummary, updateContactDetailsSummary, businessAddressSummary).flatten

          val getSentDateTime = LocalDateTime.ofInstant(submittedOnDate, ZoneId.of("Europe/London"))
          val formattedDate = getSentDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
          val formattedTime = getSentDateTime.format(DateTimeFormatter.ofPattern("h:mma"))

          Ok (view (summaryList, formattedDate, formattedTime, request.subscription.orgName) )

        case None => genericLogger.logger.error(s"[SoftDrinksIndustryLevyService [submitVariation] - unexpected response while attempting to retreive userAnswers submittedOnDate")
          Redirect(routes.SelectChangeController.onPageLoad)
    }
  }
}
