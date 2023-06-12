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

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.updateRegisteredDetails.WarehouseDetailsFormProvider

import javax.inject.Inject
import models.{Mode, Warehouse}
import pages.updateRegisteredDetails.WarehouseDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.updateRegisteredDetails.WarehouseDetailsView
import views.summary.updateRegisteredDetails.WarehouseDetailsSummary
import handlers.ErrorHandler
import models.backend.UkAddress

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency

class WarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForUpdateRegisteredDetails,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: WarehouseDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WarehouseDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WarehouseDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val Warehouse = request.userAnswers.warehouseList

      val message: Option[SummaryList] = Warehouse match {
        case warehouseList  if !warehouseList.isEmpty => Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(warehouseList)
        ))
        case warehouseList if warehouseList.isEmpty => None
      }

      Ok(view(preparedForm, mode, message))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val Warehouse = request.userAnswers.warehouseList

      val message: Option[SummaryList] = Warehouse match {
        case warehouseList  if !warehouseList.isEmpty => Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(warehouseList))
        )
        case warehouseList if warehouseList.isEmpty => None
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, message))),

        value => {
          val updatedAnswers = request.userAnswers.set(WarehouseDetailsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, WarehouseDetailsPage, mode)
        }
      )
  }
}
