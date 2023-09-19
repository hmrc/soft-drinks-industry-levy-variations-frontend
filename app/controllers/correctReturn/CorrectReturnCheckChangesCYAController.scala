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

import com.google.inject.Inject
import controllers.actions.ControllerActions
<<<<<<< HEAD
import controllers.correctReturn.routes
=======
>>>>>>> 49c8c91 (DLS-7741 add check changes page and tests)
import models.SelectChange.CorrectReturn
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.correctReturn.CorrectReturnCheckChangesCYAView

class CorrectReturnCheckChangesCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CorrectReturnCheckChangesCYAView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val orgName: String = " " + request.subscription.orgName
      val list: Seq[(String, SummaryList)] = Seq.empty

<<<<<<< HEAD
      Ok(view(orgName, list, routes.CorrectReturnCheckChangesCYAController.onSubmit))
=======
      Ok(view(orgName, list))
>>>>>>> 49c8c91 (DLS-7741 add check changes page and tests)
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    Redirect(controllers.routes.IndexController.onPageLoad.url)
  }
}
