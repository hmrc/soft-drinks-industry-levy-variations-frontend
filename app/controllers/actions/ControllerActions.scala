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
import controllers.routes
import models.SelectChange
import models.requests.{CorrectReturnDataRequest, DataRequest, OptionalDataRequest, RequiredDataRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}

class ControllerActions @Inject()(identify: IdentifierAction,
                                  getData: DataRetrievalAction)(implicit ec: ExecutionContext) {

  def withRequiredData[A]: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredAction
  }

  def withRequiredJourneyData[A](journeyType: SelectChange): ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen journeyDataRequiredAction(journeyType)
  }

  def withCorrectReturnJourneyData[A]: ActionBuilder[CorrectReturnDataRequest, AnyContent] = {
    identify andThen getData andThen correctReturnDataRequiredAction
  }

  private def correctReturnDataRequiredAction: ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] =
    new ActionRefiner[OptionalDataRequest, CorrectReturnDataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, CorrectReturnDataRequest[A]]] = {


        request.userAnswers match {
          case Some(userAnswers) if userAnswers.journeyType == SelectChange.CorrectReturn =>
            (userAnswers.correctReturnPeriod) match {
              case Some(returnPeriod) => Future.successful(
                Right(CorrectReturnDataRequest(request.request, request.sdilEnrolment,
                request.subscription, userAnswers, returnPeriod)))
              case None => Future.successful(Left(Redirect(controllers.correctReturn.routes.SelectController.onPageLoad)))
            }
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def journeyDataRequiredAction(journeyType: SelectChange): ActionRefiner[OptionalDataRequest, DataRequest] =
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        request.userAnswers match {
          case Some(data) if data.journeyType == journeyType =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, data)))
          case Some(data) if journeyType == SelectChange.CancelRegistration && data.journeyType == SelectChange.ChangeActivity =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, data)))
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def dataRequiredAction: ActionRefiner[OptionalDataRequest, DataRequest] =
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        request.userAnswers match {
          case Some(data) =>
            Future.successful(Right(RequiredDataRequest(request.request, request.sdilEnrolment, request.subscription, data)))
          case _ => Future.successful(Left(Redirect(routes.SelectChangeController.onPageLoad)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

}
