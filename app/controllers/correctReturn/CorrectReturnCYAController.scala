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
import controllers.actions._
import models.NormalMode
import models.SelectChange.CorrectReturn
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.CorrectReturnBaseCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

import scala.concurrent.ExecutionContext

class CorrectReturnCYAController @Inject()(override
                                           val messagesApi: MessagesApi,
                                           controllerActions: ControllerActions,
                                           val controllerComponents: MessagesControllerComponents,
                                           requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                           returnService: ReturnService,
                                           correctReturnOrchestrator: CorrectReturnOrchestrator,
                                           view: CorrectReturnCYAView,
                                           genericLogger: GenericLogger)
                                          (implicit config: FrontendAppConfig, ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnBaseCYAPage) {

        val calculateAmounts = correctReturnOrchestrator.calculateAmounts(request.sdilEnrolment, request.userAnswers, request.returnPeriod)

        val result = calculateAmounts.value.map {
          case Right(amounts) =>
            val orgName: String = " " + request.subscription.orgName
            val sections = CorrectReturnBaseCYASummary.summaryListAndHeadings(request.userAnswers, request.subscription, amounts)
            Ok(view(orgName, amounts, sections, controllers.correctReturn.routes.CorrectReturnCYAController.onSubmit))
          case Left(_) =>
            genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
            Redirect(controllers.routes.SelectChangeController.onPageLoad.url)
        }
        result
//        val orgName: String = " " + request.subscription.orgName
//        val sections = CorrectReturnBaseCYASummary.summaryListAndHeadings(request.userAnswers, request.subscription, amounts)
//
//        Future.successful(Ok(view(orgName, amounts, sections, controllers.correctReturn.routes.CorrectReturnCYAController.onSubmit))).recoverWith {
//          case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
//            Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
//        }
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    Redirect(controllers.correctReturn.routes.CorrectionReasonController.onPageLoad(NormalMode).url)
  }
}
