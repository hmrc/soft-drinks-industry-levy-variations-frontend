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

//   TODO: RENAME ignoreSubmitted
  def withRequiredJourneyData[A](journeyType: SelectChange, ignoreSubmitted: Boolean = false): ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen journeyDataRequiredAction(journeyType, ignoreSubmitted)
  }

  def withCorrectReturnJourneyData[A]: ActionBuilder[CorrectReturnDataRequest, AnyContent] = {
    identify andThen getData andThen correctReturnDataRequiredAction(ignoreSubmitted = false)
  }

  def withCorrectReturnJourneyDataNew[A](ignoreSubmitted: Boolean = false): ActionBuilder[CorrectReturnDataRequest, AnyContent] = {
    identify andThen getData andThen correctReturnDataRequiredAction(ignoreSubmitted)
  }

//TODO: CLEAN UP THIS ONE
  private def correctReturnDataRequiredAction(ignoreSubmitted: Boolean): ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] =
    new ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, CorrectReturnDataRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        if (request.userAnswers.get.submitted && !ignoreSubmitted) {
//          UPDATE DONE PAGES
          request.userAnswers.get.journeyType match {
            case CancelRegistration =>
              Future(Left(Redirect(controllers.cancelRegistration.routes.CancellationRequestDoneController.onPageLoad())))
            case UpdateRegisteredDetails =>
              Future(Left(Redirect(controllers.updateRegisteredDetails.routes.UpdateDoneController.onPageLoad())))
            case CorrectReturn =>
              Future(Left(Redirect(controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad)))
            case ChangeActivity =>
              Future(Left(Redirect(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad)))
          }
        } else if (!request.userAnswers.get.submitted && ignoreSubmitted) {
//          ACCESSING UPDATE DONE PAGES INCORRECTLY
          request.userAnswers.get.journeyType match {
            case CancelRegistration =>
              Future(Left(Redirect(controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode))))
            case UpdateRegisteredDetails =>
              Future(Left(Redirect(controllers.updateRegisteredDetails.routes.ChangeRegisteredDetailsController.onPageLoad())))
            case CorrectReturn =>
              Future(Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
            case ChangeActivity =>
              Future(Left(Redirect(controllers.changeActivity.routes.AmountProducedController.onPageLoad(NormalMode))))
          }
        } else {
          request.userAnswers match {
            case Some(userAnswers) if userAnswers.journeyType == SelectChange.CorrectReturn =>
              userAnswers.correctReturnPeriod match {
                case Some(returnPeriod) => getOriginalReturnAndCreateDataRequest(userAnswers, returnPeriod, request)
                case None => Future.successful(Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
              }
            case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
          }
        }
      }

      override protected def executionContext: ExecutionContext = ec

      private def getOriginalReturnAndCreateDataRequest[T](userAnswers: UserAnswers,
                                                           returnPeriod: ReturnPeriod,
                                                           request: OptionalDataRequest[T])
                                   (implicit hc: HeaderCarrier): Future[Either[Result, CorrectReturnDataRequest[T]]] = {
        sdilConnector.getReturn(request.subscription.utr, returnPeriod).value.map{
          case Right(Some(sdilReturn)) => Right(CorrectReturnDataRequest(request.request, request.sdilEnrolment,
            request.subscription, userAnswers, returnPeriod, sdilReturn))
          case Right(_) => Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad))
          case _ => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
        }
      }
    }

//  TODO: BRING OUT DIFFERENT OPTIONS AS SEPARATE FUNCTION
  private def journeyDataRequiredAction(journeyType: SelectChange, ignoreSubmitted: Boolean): ActionRefiner[OptionalDataRequest, DataRequest] =
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        if (request.userAnswers.get.submitted && !ignoreSubmitted) {
//          UPDATE DONE PAGES
          request.userAnswers.get.journeyType match {
            case CancelRegistration =>
              Future(Left(Redirect(controllers.cancelRegistration.routes.CancellationRequestDoneController.onPageLoad())))
            case UpdateRegisteredDetails =>
              Future(Left(Redirect(controllers.updateRegisteredDetails.routes.UpdateDoneController.onPageLoad())))
            case CorrectReturn =>
              Future(Left(Redirect(controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad)))
            case ChangeActivity =>
              Future(Left(Redirect(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad)))
          }
        } else if (!request.userAnswers.get.submitted && ignoreSubmitted) {
//          ACCESSING UPDATE DONE PAGES INCORRECTLY
          request.userAnswers.get.journeyType match {
            case CancelRegistration =>
              Future(Left(Redirect(controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode))))
            case UpdateRegisteredDetails =>
              Future(Left(Redirect(controllers.updateRegisteredDetails.routes.ChangeRegisteredDetailsController.onPageLoad())))
            case CorrectReturn =>
              Future(Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
            case ChangeActivity =>
              Future(Left(Redirect(controllers.changeActivity.routes.AmountProducedController.onPageLoad(NormalMode))))
          }
        }else {
          request.userAnswers match {
            case Some(userAnswers) if userAnswers.journeyType == journeyType =>
              Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers)))
            case Some(userAnswers) if journeyType == SelectChange.CancelRegistration && userAnswers.journeyType == SelectChange.ChangeActivity =>
              Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers)))
            case None if request.subscription.deregDate.nonEmpty =>
              selectChangeOrchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(request.subscription).value.map{
                case Right(userAnswers) => Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnswers))
                case Left(_) => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
              }
            case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
          }
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
