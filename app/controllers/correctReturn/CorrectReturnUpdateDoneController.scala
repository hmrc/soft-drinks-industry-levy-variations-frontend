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
import controllers.actions.{ControllerActions, RequiredUserAnswersForCorrectReturn}
import models.{Amounts, SdilReturn}
import models.correctReturn.ChangedPage
import orchestrators.CorrectReturnOrchestrator
import pages.correctReturn.CorrectReturnUpdateDonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReturnService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.correctReturn.CorrectReturnUpdateDoneView
import views.summary.correctReturn.CorrectReturnCheckChangesSummary

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class CorrectReturnUpdateDoneController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            val requiredUserAnswers: RequiredUserAnswersForCorrectReturn,
                                            val controllerComponents: MessagesControllerComponents,
                                            val correctReturnOrchestrator: CorrectReturnOrchestrator,
                                            returnService: ReturnService,
                                            genericLogger: GenericLogger,
                                            view: CorrectReturnUpdateDoneView
                                          )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      requiredUserAnswers.requireData(CorrectReturnUpdateDonePage) {
        request.userAnswers.getCorrectReturnOriginalSDILReturnData.map(originalSdilReturn => {
          request.userAnswers.correctReturnPeriod.map(returnPeriod => {
            returnService.getBalanceBroughtForward(request.sdilEnrolment).map(balanceBroughtForward => {
              val orgName: String = " " + request.subscription.orgName
              val currentSDILReturn = SdilReturn.apply(request.userAnswers)
              val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)
              val amounts: Amounts = Amounts(
                originalReturnTotal = originalSdilReturn.total,
                newReturnTotal = SdilReturn(request.userAnswers).total,
                balanceBroughtForward = balanceBroughtForward * -1,
                adjustedAmount = SdilReturn(request.userAnswers).total + (balanceBroughtForward * -1)
              )
              val sections: Seq[(String, SummaryList)] = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(
                request.userAnswers,
                request.subscription,
                changedPages,
                amounts,
                isCheckAnswers = false
              )

              val getSentDateTime = LocalDateTime.now(ZoneId.of("UTC")) //LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("UTC"))
              val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
              val timeFormatter = DateTimeFormatter.ofPattern("H:MMa")
              val formattedDate = getSentDateTime.format(dateFormatter)
              val formattedTime = getSentDateTime.format(timeFormatter)

              val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
              val returnPeriodStart = returnPeriod.start.format(returnPeriodFormat)
              val returnPeriodEnd = returnPeriod.end.format(returnPeriodFormat)
              Ok(view(orgName, sections, formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd, config.sdilHomeUrl))
            }).recoverWith {
              case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
                Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
            }
          }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
        }).getOrElse(Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))
      }
  }

  //        (for {
  //          originalSdilReturn <- request.userAnswers.getCorrectReturnOriginalSDILReturnData
  //          returnPeriod <- request.userAnswers.correctReturnPeriod
  //        } yield {
  //          returnService.getBalanceBroughtForward(request.sdilEnrolment).map(balanceBroughtForward => {
  //            val orgName: String = " " + request.subscription.orgName
  //            val currentSDILReturn = SdilReturn.apply(request.userAnswers)
  //            val changedPages = ChangedPage.returnLiteragePagesThatChangedComparedToOriginalReturn(originalSdilReturn, currentSDILReturn)
  //            val amounts: Amounts = Amounts(
  //              originalReturnTotal = originalSdilReturn.total,
  //              newReturnTotal = SdilReturn(request.userAnswers).total,
  //              balanceBroughtForward = balanceBroughtForward * -1,
  //              adjustedAmount = SdilReturn(request.userAnswers).total + (balanceBroughtForward * -1)
  //            )
  //            val sections = CorrectReturnCheckChangesSummary.changeSpecificSummaryListAndHeadings(
  //              request.userAnswers, request.subscription, changedPages, amounts, isCheckAnswers = false)
  //
  //            val getSentDateTime = LocalDateTime.now(ZoneId.of("UTC")) //LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("UTC"))
  //            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  //            val timeFormatter = DateTimeFormatter.ofPattern("H:MMa")
  //            val formattedDate = getSentDateTime.format(dateFormatter)
  //            val formattedTime = getSentDateTime.format(timeFormatter)
  //
  //            val returnPeriodFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
  //            val returnPeriodStart = returnPeriod.start.format(returnPeriodFormat)
  //            val returnPeriodEnd = returnPeriod.end.format(returnPeriodFormat)
  //
  //            Future.successful(Ok(view(orgName, sections, formattedDate, formattedTime, returnPeriodStart, returnPeriodEnd, config.sdilHomeUrl)))
  //          }).recoverWith {
  //            case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][Balance] - unexpected response for ${request.sdilEnrolment}")
  //              Future.successful(Redirect(controllers.routes.SelectChangeController.onPageLoad.url))
  //          }
  //        }).getOrElse(Future(Redirect(controllers.routes.SelectChangeController.onPageLoad.url)))

}
