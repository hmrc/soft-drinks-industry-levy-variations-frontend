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

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.changeActivity.PackAtBusinessAddressFormProvider

import javax.inject.Inject
import models.Mode
import pages.changeActivity.PackAtBusinessAddressPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.changeActivity.PackAtBusinessAddressView
import handlers.ErrorHandler
import models.backend.Site

import scala.concurrent.{ExecutionContext, Future}
import navigation._
import viewmodels.AddressFormattingHelper

class PackAtBusinessAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForChangeActivity,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PackAtBusinessAddressFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackAtBusinessAddressView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val formattedAddress = AddressFormattingHelper.addressFormatting(request.subscription.address, Option(request.subscription.orgName))
      val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, formattedAddress))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val businessName = request.subscription.orgName
      val businessAddress = request.subscription.address
      val formattedAddress = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            onwardUrl <- if (value) {
              updateDatabaseAndRedirect(updatedAnswers.copy(packagingSiteList = updatedAnswers.packagingSiteList ++ Map("1" ->
                Site(
                  address = businessAddress,
                  ref = None,
                  tradingName = Some(businessName),
                  closureDate = None
                )
              )), PackAtBusinessAddressPage, mode)
            } else {
              updateDatabaseAndRedirect(updatedAnswers, PackAtBusinessAddressPage, mode)
            }
          } yield onwardUrl
        }
      )
  }
}
