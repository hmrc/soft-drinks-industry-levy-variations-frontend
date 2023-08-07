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

import cats.data.EitherT
import controllers.ControllerHelper
import controllers.actions._
import errors.{NoVariableReturns, SelectReturnFormError}
import forms.correctReturn.SelectFormProvider
import handlers.ErrorHandler
import models.SelectChange.CorrectReturn
import models.requests.DataRequest
import models.{NormalMode, ReturnPeriod}
import navigation._
import orchestrators.CorrectReturnOrchestrator
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.VariationResult
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.SelectView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  val sessionService: SessionService,
                                  val navigator: NavigatorForCorrectReturn,
                                  controllerActions: ControllerActions,
                                  formProvider: SelectFormProvider,
                                  correctReturnOrchestrator: CorrectReturnOrchestrator,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: SelectView,
                                  val genericLogger: GenericLogger,
                                  val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[String] = formProvider()

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
      correctReturnOrchestrator.getReturnPeriods(request.subscription).value.map {
        case Right(returnPeriods) =>
          val returnPeriodsForYears = correctReturnOrchestrator.separateReturnPeriodsByYear(returnPeriods)
          val preparedForm = request.userAnswers.correctReturnPeriod.fold(form)(value => form.fill(value.radioValue))
          Ok(view(preparedForm, returnPeriodsForYears))
        case Left(NoVariableReturns) => Redirect(controllers.routes.SelectChangeController.onPageLoad)
        case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
      val subscription = request.subscription
      val userAnswers = request.userAnswers

      val res = for {
        returnPeriods <- correctReturnOrchestrator.getReturnPeriods(request.subscription)
        selectedReturnPeriod <- getReturnPeriodFromForm(returnPeriods)
        _ <- correctReturnOrchestrator.setupUserAnswersForCorrectReturn(subscription, userAnswers, selectedReturnPeriod)
      } yield returnPeriods

      res.value.map{
        case Right(_) if subscription.activity.smallProducer =>
          Redirect(routes.PackagedAsContractPackerController.onPageLoad(NormalMode))
        case Right(_) =>
          Redirect(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode))
        case Left(NoVariableReturns) =>
          Redirect(controllers.routes.SelectChangeController.onPageLoad)
        case Left(SelectReturnFormError(formWithError, returnPeriods)) =>
          val returnPeriodsForYears = correctReturnOrchestrator.separateReturnPeriodsByYear(returnPeriods)
          BadRequest(view(formWithError, returnPeriodsForYears))
        case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  private def getReturnPeriodFromForm(returnPeriods: List[ReturnPeriod])
                                    (implicit request: DataRequest[AnyContent]): VariationResult[ReturnPeriod] = EitherT {
    form.bindFromRequest().fold(formWithErrors =>
      Future.successful(Left(SelectReturnFormError(formWithErrors, returnPeriods))),
      returnPeriodValue => getReturnPeriodFromRadioValues(returnPeriodValue, returnPeriods) match {
        case Some(returnPeriod) => Future.successful(Right(returnPeriod))
        case None =>
          val formWithErrors = form.withError(FormError("value", "correctReturn.select.error.required"))
          Future.successful(Left(SelectReturnFormError(formWithErrors, returnPeriods)))
      }
    )
  }

  private def getReturnPeriodFromRadioValues(radioValue: String, returnPeriods: List[ReturnPeriod]): Option[ReturnPeriod] = {
    returnPeriods.collectFirst {
      case returnPeriod if returnPeriod.radioValue == radioValue => returnPeriod
    }
  }
}
