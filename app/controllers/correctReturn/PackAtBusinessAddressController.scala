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
import forms.correctReturn.PackAtBusinessAddressFormProvider
import handlers.ErrorHandler
import models.backend.{Site, UkAddress}
import models.{Mode, UserAnswers}
import navigation._
import pages.correctReturn.PackAtBusinessAddressPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, PackingDetails, SessionService}
import utilities.{AddressHelper, GenericLogger}
import viewmodels.AddressFormattingHelper
import views.html.correctReturn.PackAtBusinessAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PackAtBusinessAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: PackAtBusinessAddressFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackAtBusinessAddressView,
                                       addressLookupService: AddressLookupService,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper with AddressHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>
      val formattedAddress = AddressFormattingHelper.addressFormatting(request.subscription.address, Option(request.subscription.orgName))
      val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, formattedAddress))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      val businessName = request.subscription.orgName
      val businessAddress = request.subscription.address
      val formattedAddress = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            onwardUrl <-
              if (value) {
                updateDatabaseWithoutRedirect(Try(updatedAnswers.copy(
                  packagingSiteList = updatedAnswers.packagingSiteList ++ Map(
                    "1" ->
                      Site(
                        address = request.subscription.address,
                        ref = None,
                        tradingName = Some(request.subscription.orgName),
                        closureDate = None
                      )
                  ))), PackAtBusinessAddressPage).flatMap(_ =>
                  Future.successful(routes.PackagingSiteDetailsController.onPageLoad(mode).url))
              } else {
                updateDatabaseWithoutRedirect(Try(updatedAnswers), PackAtBusinessAddressPage).flatMap(_ =>
                  addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode))
              }
          } yield {
            Redirect(onwardUrl)
          }
      )
  }
}
