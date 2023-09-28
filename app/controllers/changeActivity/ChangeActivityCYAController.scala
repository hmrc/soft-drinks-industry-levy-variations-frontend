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

import com.google.inject.Inject
import controllers.actions.ControllerActions
import config.FrontendAppConfig
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeActivity.ChangeActivityCYAView
import models.SelectChange.ChangeActivity
import views.summary.changeActivity.ChangeActivitySummary

class ChangeActivityCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            implicit val config: FrontendAppConfig,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ChangeActivityCYAView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      val alias: String = request.subscription.orgName
      val sections = ChangeActivitySummary.summaryListsAndHeadings(request.userAnswers)
      Ok(view(alias, sections, routes.ChangeActivityCYAController.onSubmit))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    Redirect(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad)
  }

}
