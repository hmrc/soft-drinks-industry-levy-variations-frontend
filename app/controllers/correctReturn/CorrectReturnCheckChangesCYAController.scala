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
import config.FrontendAppConfig
import controllers.actions.ControllerActions
import models.SdilReturn
import models.SelectChange.CorrectReturn
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

import scala.concurrent.ExecutionContext

class CorrectReturnCheckChangesCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val controllerComponents: MessagesControllerComponents,
                                            val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                            view: CorrectReturnCheckChangesCYAView
                                          )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>
      println(Console.YELLOW + "User answers are " + request.userAnswers + Console.WHITE)
      val orgName: String = " " + request.subscription.orgName
      val originalSDILReturn = request.userAnswers.getCorrectReturnOriginalSDILReturnData.get
      val currentSDILReturn = SdilReturn.apply(request.userAnswers)
      println(Console.BLUE + "Orig SDIL return is  " + originalSDILReturn + Console.WHITE)
      println(Console.BLUE + "Current SDIL return is  " + currentSDILReturn + Console.WHITE)
      val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSDILReturn, currentSDILReturn)
      val sections = CorrectReturnBaseCYASummary.changedSummaryListAndHeadings(request.userAnswers, request.subscription, changedPages)
       println(Console.MAGENTA + "changed pages  " + changedPages + Console.WHITE)
      Ok(view(orgName, sections, routes.CorrectReturnCheckChangesCYAController.onSubmit))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    Redirect(controllers.routes.IndexController.onPageLoad.url)
  }

}
