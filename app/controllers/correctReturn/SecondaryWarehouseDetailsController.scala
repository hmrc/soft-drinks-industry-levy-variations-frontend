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

import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.SecondaryWarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation._
import pages.correctReturn.SecondaryWarehouseDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, SessionService, WarehouseDetails}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utilities.GenericLogger
import viewmodels.govuk.summarylist._
import viewmodels.summary.correctReturn.SecondaryWarehouseDetailsSummary
import views.html.correctReturn.SecondaryWarehouseDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SecondaryWarehouseDetailsController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     val sessionService: SessionService,
                                                     val navigator: NavigatorForCorrectReturn,
                                                     controllerActions: ControllerActions,
                                                     formProvider: SecondaryWarehouseDetailsFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: SecondaryWarehouseDetailsView,
                                                     addressLookupService: AddressLookupService,
                                                     val genericLogger: GenericLogger,
                                                     val errorHandler: ErrorHandler
                                                   )(implicit val ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val preparedForm = request.userAnswers.get(SecondaryWarehouseDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val siteList: SummaryList = SummaryListViewModel(
        rows = SecondaryWarehouseDetailsSummary.row2(request.userAnswers.warehouseList, mode)
      )

      Ok(view(preparedForm, mode, siteList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>

      val siteList: SummaryList = SummaryListViewModel(
        rows = SecondaryWarehouseDetailsSummary.row2(request.userAnswers.warehouseList, mode)
      )

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          if (value) {
            addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode).map(Redirect(_))
          } else {
            Future.successful(Redirect(navigator.nextPage(SecondaryWarehouseDetailsPage, mode, request.userAnswers)))
          }
      )
  }

}
