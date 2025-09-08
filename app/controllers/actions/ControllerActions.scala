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

package controllers.actions

import com.google.inject.Inject
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.SelectChange._
import models.requests.{CorrectReturnDataRequest, DataRequest, OptionalDataRequest, RequiredDataRequest}
import models.{NormalMode, ReturnPeriod, SelectChange, UserAnswers}
import orchestrators.SelectChangeOrchestrator
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class ControllerActions @Inject()(identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  selectChangeOrchestrator: SelectChangeOrchestrator,
                                  errorHandler: ErrorHandler,
                                  sdilConnector: SoftDrinksIndustryLevyConnector)(implicit ec: ExecutionContext) {

  def withRequiredData[A]: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredAction
  }

  def withRequiredJourneyData[A](journeyType: SelectChange): ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen journeyDataRequiredAction(journeyType) andThen checkSubmission(onPostSubmissionPageLoad = false)
  }

  def withRequiredJourneyDataPostSubmission[A](journeyType: SelectChange): ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen journeyDataRequiredAction(journeyType) andThen checkSubmission(onPostSubmissionPageLoad = true)
  }

  def withCorrectReturnJourneyData[A]: ActionBuilder[CorrectReturnDataRequest, AnyContent] = {
    identify andThen getData andThen correctReturnDataRequiredAction andThen checkReturnSubmission(onPostSubmissionPageLoad = false)
  }

  def withCorrectReturnJourneyDataPostSubmission[A]: ActionBuilder[CorrectReturnDataRequest, AnyContent] = {
    identify andThen getData andThen correctReturnDataRequiredAction andThen checkReturnSubmission(onPostSubmissionPageLoad = true)
  }

  private def correctReturnDataRequiredAction: ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] =
    new ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, CorrectReturnDataRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.journeyType == SelectChange.CorrectReturn =>
            userAnswers.correctReturnPeriod match {
              case Some(returnPeriod) => getOriginalReturnAndCreateDataRequest(userAnswers, returnPeriod, request)
              case None => Future.successful(Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
            }
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec

      private def getOriginalReturnAndCreateDataRequest[T](userAnswers: UserAnswers,
                                                           returnPeriod: ReturnPeriod,
                                                           request: OptionalDataRequest[T])
                                   (implicit hc: HeaderCarrier): Future[Either[Result, CorrectReturnDataRequest[T]]] = {
        sdilConnector.getReturn(request.subscription.utr, returnPeriod).value.flatMap {
          case Right(Some(sdilReturn)) => Future.successful(
            Right(CorrectReturnDataRequest(request.request, request.sdilEnrolment,
            request.subscription, userAnswers, returnPeriod, sdilReturn)))
          case Right(_) => Future.successful(
            Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
          case _ =>
            errorHandler.internalServerErrorTemplate(request).map(errorView => Left(InternalServerError(errorView)))
        }
      }
    }

  private def journeyDataRequiredAction(journeyType: SelectChange): ActionRefiner[OptionalDataRequest, DataRequest] =
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.journeyType == journeyType =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers)))
          case Some(userAnswers) if journeyType == SelectChange.CancelRegistration && userAnswers.journeyType == SelectChange.ChangeActivity =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers)))
          case None if request.subscription.deregDate.nonEmpty =>
            selectChangeOrchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(request.subscription).value.flatMap {
              case Right(userAnswers) => Future.successful(
                Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers)))
              case Left(_) =>
                errorHandler.internalServerErrorTemplate(request).map(errorView => Left(InternalServerError(errorView)))
            }
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def postSubmissionResultFromJourneyType(selectChange: SelectChange): String = selectChange match {
    case CancelRegistration => controllers.cancelRegistration.routes.CancellationRequestDoneController.onPageLoad().url
    case UpdateRegisteredDetails => controllers.updateRegisteredDetails.routes.UpdateDoneController.onPageLoad().url
    case CorrectReturn => controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad().url
    case ChangeActivity => controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad().url
  }

  private def preSubmissionResultFromJourneyType(selectChange: SelectChange): String = selectChange match {
    case CancelRegistration => controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode).url
    case UpdateRegisteredDetails => controllers.updateRegisteredDetails.routes.ChangeRegisteredDetailsController.onPageLoad().url
    case CorrectReturn => controllers.correctReturn.routes.SelectController.onPageLoad.url
    case ChangeActivity => controllers.changeActivity.routes.AmountProducedController.onPageLoad(NormalMode).url
  }

  private def checkReturnSubmission(onPostSubmissionPageLoad: Boolean): ActionRefiner[CorrectReturnDataRequest, CorrectReturnDataRequest] =
    new ActionRefiner[CorrectReturnDataRequest, CorrectReturnDataRequest] {
      override protected def refine[A](request: CorrectReturnDataRequest[A]): Future[Either[Result, CorrectReturnDataRequest[A]]] = {
        (request.userAnswers.submitted, onPostSubmissionPageLoad) match {
          case (true, false) => Future(Left(Redirect(postSubmissionResultFromJourneyType(SelectChange.CorrectReturn))))
          case (false, true) => Future(Left(Redirect(preSubmissionResultFromJourneyType(SelectChange.CorrectReturn))))
          case _ => Future.successful(Right(request))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def checkSubmission(onPostSubmissionPageLoad: Boolean): ActionRefiner[DataRequest, DataRequest] =
    new ActionRefiner[DataRequest, DataRequest] {
      override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        (request.userAnswers.submitted, onPostSubmissionPageLoad) match {
          case (true, false) => Future(Left(Redirect(postSubmissionResultFromJourneyType(request.userAnswers.journeyType))))
          case (false, true) => Future(Left(Redirect(preSubmissionResultFromJourneyType(request.userAnswers.journeyType))))
          case _ => Future.successful(Right(request))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def dataRequiredAction: ActionRefiner[OptionalDataRequest, DataRequest] =
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[T](request: OptionalDataRequest[T]): Future[Either[Result, DataRequest[T]]] = {
        request.userAnswers match {
          case Some(data) =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, data)))
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

}
