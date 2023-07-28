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
import controllers.routes
import models.NormalMode
import models.SelectChange.CorrectReturn
import navigation.NavigatorForCorrectReturn
import pages.correctReturn.ReturnChangeRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.correctReturn.ReturnChangeRegistrationView

import javax.inject.Inject

class ReturnChangeRegistrationController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    controllerActions: ControllerActions,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    navigator: NavigatorForCorrectReturn,
                                                    view: ReturnChangeRegistrationView
                                                  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>
        Ok(view(routes.IndexController.onPageLoad.url))
  }

  def onSubmit(): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>
      Redirect(navigator.nextPage(ReturnChangeRegistrationPage, NormalMode, request.userAnswers))
  }

}