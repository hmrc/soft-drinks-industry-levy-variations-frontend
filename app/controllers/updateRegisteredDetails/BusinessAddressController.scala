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

import controllers.actions._
import models.NormalMode
import models.SelectChange.UpdateRegisteredDetails
import navigation.NavigatorForUpdateRegisteredDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, ContactDetails}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.BusinessAddressView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BusinessAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           controllerActions: ControllerActions,
                                           val navigator: NavigatorForUpdateRegisteredDetails,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: BusinessAddressView,
                                           addressLookupService: AddressLookupService
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      Ok(view(List(request.userAnswers.contactAddress)))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    _ =>
      Redirect(controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad)
  }

  def changeAddress: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {
    implicit request =>
        addressLookupService.initJourneyAndReturnOnRampUrl(ContactDetails, mode = NormalMode).map(
          initUrl => Redirect(initUrl)
        )
  }

}
