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
import models.{CheckMode, Mode}
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
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      if (request.userAnswers.packagingSiteList.isEmpty) {
        Redirect(routes.PackAtBusinessAddressController.onPageLoad(mode).url)
      } else {
        val siteList: SummaryList = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
        )

        Ok(view(form, mode, siteList))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity).async {
    implicit request =>
      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value => {
          val updatedAnswers = request.userAnswers.set(PackagingSiteDetailsPage, value)
          updateDatabaseWithoutRedirect(updatedAnswers, PackagingSiteDetailsPage)
          println(Console.YELLOW + "packaging site updated answers = " + updatedAnswers + Console.WHITE)
          Thread.sleep(1000)
          println(Console.BLUE + "packaging site UAs - data = " + request.userAnswers.data + Console.WHITE)
          getOnwardUrl(value, mode).map(Redirect(_))
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
      Future.successful(controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url)
    }
  }
}
