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

package controllers.changeActivity

import controllers.ControllerHelper
import controllers.actions._
import forms.changeActivity.SecondaryWarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.Mode
import models.SelectChange.ChangeActivity
import navigation._
import pages.changeActivity.SecondaryWarehouseDetailsPage
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import services.{AddressLookupService, SessionService, WarehouseDetails}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.changeActivity.SecondaryWarehouseDetailsView
import views.summary.changeActivity.SecondaryWarehouseDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryWarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForChangeActivity,
                                       controllerActions: ControllerActions,
                                       formProvider: SecondaryWarehouseDetailsFormProvider,
                                       addressLookupService: AddressLookupService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: SecondaryWarehouseDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider(hasWarehouses = true)

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      val form: Form[Boolean] = formProvider(hasWarehouses = request.userAnswers.warehouseList.nonEmpty)
      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows = SecondaryWarehouseDetailsSummary.summaryRows(warehouseList, mode))
        )
        case _ => None
      }

      Ok(view(form, summaryList, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      val form: Form[Boolean] = formProvider(hasWarehouses = request.userAnswers.warehouseList.nonEmpty)
      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows = SecondaryWarehouseDetailsSummary.summaryRows(warehouseList, mode))
        )
        case _ => None
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, summaryList, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set(SecondaryWarehouseDetailsPage, value)
          updateDatabaseWithoutRedirect(updatedAnswers, SecondaryWarehouseDetailsPage).flatMap(_ => {
            getOnwardUrl(value, mode).map(Redirect(_))
          })
        }
      )
  }

  private def getOnwardUrl(value: Boolean, mode: Mode)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages, requestHeader: RequestHeader): Future[String] = {
    if (value) {
      addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode)(hc, ec, messages, requestHeader)
    } else {
      Future.successful(routes.ChangeActivityCYAController.onPageLoad.url)
    }
  }

}
