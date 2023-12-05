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

import controllers.actions._
import controllers.ControllerHelper
import forms.correctReturn.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.{Mode, NormalMode}
import navigation._
import pages.correctReturn.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AddressLookupService, PackingDetails, SessionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.PackagingSiteDetailsView
import views.summary.correctReturn.PackagingSiteDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: PackagingSiteDetailsFormProvider,
                                       addressLookupService: AddressLookupService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackagingSiteDetailsView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper with SummaryListFluency{

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
      )

      Ok(view(preparedForm, mode, siteList))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      val siteList: SummaryList = SummaryListViewModel(
        rows = PackagingSiteDetailsSummary.row2(request.userAnswers.packagingSiteList, mode)
      )

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            onwardUrl: Result <-
              if (value) {
                val alsOnRampUrl = updateDatabaseWithoutRedirect(request.userAnswers.set(PackagingSiteDetailsPage, value), PackagingSiteDetailsPage).flatMap(_ =>
                  addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode))
                alsOnRampUrl.map(Redirect(_))
              } else {
                val updatedAnswers = request.userAnswers.set(PackagingSiteDetailsPage, value)
                val subscription = if (mode == NormalMode) Some(request.subscription) else None
                updateDatabaseAndRedirect(updatedAnswers, PackagingSiteDetailsPage, mode, None, subscription)
              }
          } yield {
            onwardUrl
          }
      )
  }
}
