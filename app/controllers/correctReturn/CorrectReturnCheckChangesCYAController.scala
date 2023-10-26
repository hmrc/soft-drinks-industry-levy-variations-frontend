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
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.ControllerActions
import errors.UnexpectedResponseFromSDIL
import models.{Amounts, SdilReturn}
import models.SelectChange.CorrectReturn
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReturnService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnCheckChangesCYAView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnCheckChangesCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val controllerComponents: MessagesControllerComponents,
                                            val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                            view: CorrectReturnCheckChangesCYAView,
                                            returnService: ReturnService,
                                            genericLogger: GenericLogger
                                          )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      request.userAnswers.getCorrectReturnOriginalSDILReturnData.map(originalSdilReturn => {
        val accountBalance = returnService.getBalanceBroughtForward(request.sdilEnrolment)
        val orgName: String = " " + request.subscription.orgName
        val currentSDILReturn = SdilReturn.apply(request.userAnswers)
        val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)

        def sections(accountBalance: BigDecimal): Seq[(String, SummaryList)] = {
          CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(
            request.userAnswers,
            request.subscription,
            changedPages,
            amounts = Amounts(
              originalReturnTotal = originalSdilReturn.total,
              newReturnTotal = SdilReturn(request.userAnswers).total,
              accountBalance = accountBalance * -1,
              adjustedAmount = if(accountBalance == 0){SdilReturn(request.userAnswers).total + accountBalance} else {
                (SdilReturn(request.userAnswers).total ) + (accountBalance * -1)
              }
            )
          )
        }

        accountBalance.map(accountBalance => {
        Ok(view(orgName, sections(accountBalance: BigDecimal), routes.CorrectReturnCheckChangesCYAController.onSubmit))
        }).recoverWith {
          case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
            Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
        }
        }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    Redirect(controllers.routes.IndexController.onPageLoad.url)
  }

}
