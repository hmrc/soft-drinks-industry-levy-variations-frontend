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
import forms.changeActivity.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.SelectChange.ChangeActivity
import models.{CheckMode, Mode, NormalMode}
import navigation._
import pages.changeActivity.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import services.{AddressLookupService, PackingDetails, SessionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.changeActivity.PackagingSiteDetailsSummary
import views.html.changeActivity.PackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForChangeActivity,
                                       controllerActions: ControllerActions,
                                       formProvider: PackagingSiteDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackagingSiteDetailsView,
                                       val addressLookupService: AddressLookupService,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      if (request.userAnswers.packagingSiteList.isEmpty && mode == CheckMode) {
        Redirect(routes.PackAtBusinessAddressController.onPageLoad(mode).url)
      } else {
        val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        val siteList: SummaryList = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList)
        )

        Ok(view(preparedForm, mode, siteList))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList)
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          updateDatabaseWithoutRedirect(request.userAnswers.set(PackagingSiteDetailsPage, value), PackagingSiteDetailsPage).flatMap {
            case true => getOnwardUrl(value, mode).map(Redirect(_))
            case false => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }

      )
  }

  private def getOnwardUrl(addPackagingSite: Boolean, mode: Mode)
    (implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages, requestHeader: RequestHeader): Future[String] = {

    if(addPackagingSite) {
      addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)(hc, ec, messages, requestHeader)
    } else if(mode == CheckMode) {
      Future.successful(controllers.changeActivity.routes.ChangeActivityCYAController.onPageLoad.url)
    } else {
      Future.successful(controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
    }
  }
}
