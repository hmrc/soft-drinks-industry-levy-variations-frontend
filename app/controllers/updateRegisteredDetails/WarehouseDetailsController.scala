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

import controllers.ControllerHelper
import controllers.actions._
import forms.updateRegisteredDetails.WarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.SelectChange.UpdateRegisteredDetails
import models.{CheckMode, Mode}
import navigation._
import pages.updateRegisteredDetails.{ChangeRegisteredDetailsPage, WarehouseDetailsPage}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import services.{AddressLookupService, SessionService, WarehouseDetails}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.WarehouseDetailsView
import views.summary.updateRegisteredDetails.WarehouseDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WarehouseDetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            val sessionService: SessionService,
                                            val navigator: NavigatorForUpdateRegisteredDetails,
                                            controllerActions: ControllerActions,
                                            formProvider: WarehouseDetailsFormProvider,
                                            addressLookupService: AddressLookupService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: WarehouseDetailsView,
                                            val genericLogger: GenericLogger,
                                            val errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WarehouseDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(warehouseList, mode))
        )
        case _ => None
      }

      Ok(view(preparedForm, mode, summaryList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {
    implicit request =>

      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows = WarehouseDetailsSummary.row2(warehouseList, mode))
        )
        case _ => None
      }
      val desiredRegisteredDetailsToChangeList = request.userAnswers.get(ChangeRegisteredDetailsPage) match {
        case None => genericLogger.logger.error(
          s"Failed to obtain which registered details to change from user answers while on ${WarehouseDetailsPage.toString}")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(values) => values.toList
      }

      val addContactDetails: Boolean = desiredRegisteredDetailsToChangeList.toString.contains("contactDetails")
      val addBusinessAddress: Boolean = desiredRegisteredDetailsToChangeList.toString.contains("businessAddress")

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, summaryList))),

        value =>
          updateDatabaseWithoutRedirect(request.userAnswers.set(WarehouseDetailsPage, value), WarehouseDetailsPage).flatMap {
            case true => getOnwardUrl(value, addContactDetails, addBusinessAddress, mode).map(Redirect(_))
            case false => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }

      )
  }

  private def getOnwardUrl(value: Boolean, addContactDetails: Boolean, addBusinessAddress: Boolean, mode: Mode)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages, requestHeader: RequestHeader): Future[String] = {
    if (value) {
      addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode)(hc, ec, messages, requestHeader)
    } else {
      Future.successful((mode, addContactDetails, addBusinessAddress) match {
        case (CheckMode, _, _) => controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url
        case (_, true, _) => controllers.updateRegisteredDetails.routes.UpdateContactDetailsController.onPageLoad(mode).url
        case (_, false, true) => controllers.updateRegisteredDetails.routes.BusinessAddressController.onPageLoad().url
        case (_, false, false) => controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url
      })
    }
  }

}
