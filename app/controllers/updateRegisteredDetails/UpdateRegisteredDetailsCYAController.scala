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
import models.SelectChange.UpdateRegisteredDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.UpdateRegisteredDetailsCYAView
import views.summary.updateRegisteredDetails.{BusinessAddressSummary, PackagingSiteDetailsSummary, UpdateContactDetailsSummary, WarehouseDetailsSummary}

import java.time.{LocalDateTime, ZoneId}

class UpdateRegisteredDetailsCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: UpdateRegisteredDetailsCYAView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
//      TODO: Below two are getting answers - not list values
      val packagingSiteDetailsSummary: Option[SummaryListRow] = PackagingSiteDetailsSummary.row(request.userAnswers)
      val warehousesDetailsSummary: Option[SummaryListRow] = WarehouseDetailsSummary.row(request.userAnswers)
      val ukSiteDetailsSummary: Option[(String, SummaryList)] = Option(
//        TODO: PUT IN MESSAGES
        "updateRegisteredDetails.checkYourAnswers.ukSiteDetails.title" ->
          SummaryList(rows = List(packagingSiteDetailsSummary, warehousesDetailsSummary).flatten)
      )
      val updateContactDetailsSummary: Option[(String, SummaryList)] = UpdateContactDetailsSummary.rows(request.userAnswers)
      val businessAddressSummary: Option[(String, SummaryList)] = BusinessAddressSummary.rows(request.userAnswers)
      val summaryList = Seq(ukSiteDetailsSummary, updateContactDetailsSummary, businessAddressSummary).flatten

      Ok(view(summaryList, routes.UpdateRegisteredDetailsCYAController.onSubmit))
  }
  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
      Redirect(routes.UpdateDoneController.onPageLoad.url)
  }
}
