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

import controllers.actions._
import models.{Mode, NormalMode}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.BusinessAddressView
import models.SelectChange.UpdateRegisteredDetails
import navigation.NavigatorForUpdateRegisteredDetails
import pages.updateRegisteredDetails.BusinessAddressPage
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.helper.form
import views.summary.updateRegisteredDetails.BusinessAddressSummary

class BusinessAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           controllerActions: ControllerActions,
                                           val navigator: NavigatorForUpdateRegisteredDetails,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: BusinessAddressView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>

      val summaryList: SummaryList = {
        SummaryListViewModel(
          rows =  BusinessAddressSummary.row(request.userAnswers)
        )

      }

      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(warehouseList))
        )
        case _ => None
      }

//      Ok(view(preparedForm, mode, summaryList))
//      val businessAddress: SummaryList = SummaryListViewModel(
//        rows = BusinessAddressSummary.row2(request.userAnswers.)
//      )

      Ok(view(mode, summaryList))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      Redirect(navigator.nextPage(BusinessAddressPage, NormalMode, request.userAnswers))
  }

}
