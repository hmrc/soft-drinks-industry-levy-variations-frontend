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
import config.FrontendAppConfig
import controllers.actions.ControllerActions
import models.SelectChange.ChangeActivity
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeActivity.ChangeActivitySentView
import views.summary.changeActivity.ChangeActivitySummary

class ChangeActivitySentController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     controllerActions: ControllerActions,
                                     implicit val config: FrontendAppConfig,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: ChangeActivitySentView
                                   ) extends FrontendBaseController with I18nSupport {

 def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
  implicit request =>
    val alias: String = request.subscription.orgName
    val sections = ChangeActivitySummary.summaryListsAndHeadings(request.userAnswers, isCheckAnswers = false)
    Ok(view(alias: String, sections))
 }
}
