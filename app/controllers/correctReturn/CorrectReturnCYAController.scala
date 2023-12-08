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
import models.{Amounts, NormalMode, SdilReturn}
import models.SelectChange.CorrectReturn
import pages.correctReturn.CorrectReturnBaseCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReturnService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnCYAView
import views.summary.correctReturn.CorrectReturnBaseCYASummary

import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnCYAController @Inject()(override
                                           val messagesApi: MessagesApi,
                                           controllerActions: ControllerActions,
                                           val controllerComponents: MessagesControllerComponents,
                                           requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                           returnService: ReturnService,
                                           view: CorrectReturnCYAView,
                                           genericLogger: GenericLogger)
                                          (implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnBaseCYAPage) {
        request.userAnswers.getCorrectReturnOriginalSDILReturnData.map(originalSdilReturn => {
          val balanceBroughtForward = returnService.getBalanceBroughtForward(request.sdilEnrolment)
          val orgName: String = " " + request.subscription.orgName

          def sections(balanceBroughtForward: BigDecimal): Seq[(String, SummaryList)] = {
            CorrectReturnBaseCYASummary.summaryListAndHeadings(
              request.userAnswers,
              request.subscription,
              amounts = Amounts(
                originalReturnTotal = originalSdilReturn.total,
                newReturnTotal = SdilReturn.generateFromUserAnswers(request.userAnswers).total,
                balanceBroughtForward = balanceBroughtForward * -1,
                adjustedAmount = SdilReturn.generateFromUserAnswers(request.userAnswers).total + (balanceBroughtForward * -1)
              )
            )
          }

          balanceBroughtForward.map(balanceBroughtForward => {
            Ok(view(orgName, sections(balanceBroughtForward: BigDecimal),routes.CorrectReturnCYAController.onSubmit))
          }).recoverWith {
            case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
              Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
          }
          }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    Redirect(routes.CorrectionReasonController.onPageLoad(NormalMode).url)
  }
}
