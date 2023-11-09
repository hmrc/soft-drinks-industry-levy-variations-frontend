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
import controllers.{ControllerHelper, routes}
import forms.correctReturn.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.{Mode, NormalMode, SdilReturn}
import navigation._
import pages.correctReturn.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, PackingDetails, SessionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utilities.{GenericLogger, UserTypeCheck}
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            onwardUrl:String <-
              if(value){
                updateDatabaseWithoutRedirect(request.userAnswers.set(PackagingSiteDetailsPage, value), PackagingSiteDetailsPage).flatMap(_ =>
                  addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode))
              } else {
                updateDatabaseWithoutRedirect(request.userAnswers.set(PackagingSiteDetailsPage, value), PackagingSiteDetailsPage).flatMap(_ =>
                  (Some(SdilReturn.apply(updatedAnswers)), Some(request.subscription)) match {
                    case (Some(sdilReturn), Some(subscription)) =>
                      if (UserTypeCheck.isNewImporter (sdilReturn, subscription) && mode == NormalMode) {
                        Future.successful(controllers.correctReturn.routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
                      } else {
                        Future.successful(controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
                      }
                    case (_, Some(subscription)) =>
                      genericLogger.logger.warn(s"SDIL return not provided for ${subscription.sdilRef}")
                      Future.successful(routes.JourneyRecoveryController.onPageLoad().url)
                    case _ =>
                      genericLogger.logger.warn("SDIL return or subscription not provided for current unknown user")
                      Future.successful(routes.JourneyRecoveryController.onPageLoad().url)
                  }
                )
              }
          } yield {
            Redirect(onwardUrl)
          }
      )
  }
}
