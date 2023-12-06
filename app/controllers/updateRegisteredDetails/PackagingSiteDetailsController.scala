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
import forms.updateRegisteredDetails.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.SelectChange.UpdateRegisteredDetails
import models.{CheckMode, Mode}
import navigation._
import pages.updateRegisteredDetails.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import services.{AddressLookupService, PackingDetails, SessionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.PackagingSiteDetailsView
import views.summary.updateRegisteredDetails.PackagingSiteDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForUpdateRegisteredDetails,
                                       controllerActions: ControllerActions,
                                       formProvider: PackagingSiteDetailsFormProvider,
                                       addressLookupService: AddressLookupService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackagingSiteDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
      )

      Ok(view(form, mode, siteList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {
    implicit request =>

      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          getOnwardUrl(value, mode).map(Redirect(_))
      )
  }

  private def getOnwardUrl(value: Boolean, mode: Mode)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages,
                                                       requestHeader: RequestHeader): Future[String] = {
    if (value) {
      addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)(hc, ec, messages, requestHeader)
    } else {
      if (mode == CheckMode) {
        Future.successful(controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
      } else {
        Future.successful(controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(mode).url)
      }
    }
  }

}
