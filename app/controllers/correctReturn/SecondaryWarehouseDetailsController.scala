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

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.SecondaryWarehouseDetailsFormProvider

import javax.inject.Inject
import models.{Mode, Warehouse}
import models.SelectChange.CorrectReturn
import pages.correctReturn.SecondaryWarehouseDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.correctReturn.SecondaryWarehouseDetailsView
import handlers.ErrorHandler
import models.backend.UkAddress

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.summary.correctReturn.SecondaryWarehouseDetailsSummary
import viewmodels.govuk.summarylist._

import scala.collection.immutable.Map

class SecondaryWarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: SecondaryWarehouseDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: SecondaryWarehouseDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>

      val twoWarehouses: Map[String,Warehouse] = Map(
        "1"-> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX")),
        "2" -> Warehouse(Some("Super Cola Ltd"), UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE"))
      )


      val preparedForm = request.userAnswers.get(SecondaryWarehouseDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val siteList: SummaryList = SummaryListViewModel(
        rows = SecondaryWarehouseDetailsSummary.row2(twoWarehouses)
      )

      Ok(view(preparedForm, mode, siteList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>

      val siteList: SummaryList = SummaryListViewModel(
        rows = SecondaryWarehouseDetailsSummary.row2(request.userAnswers.warehouseList)
      )

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value => {
          val updatedAnswers = request.userAnswers.set(SecondaryWarehouseDetailsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, SecondaryWarehouseDetailsPage, mode)
        }
      )
  }
}