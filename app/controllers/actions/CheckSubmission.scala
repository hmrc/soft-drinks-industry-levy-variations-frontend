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

import models.SelectChange._
import models.requests.DataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckingSubmission @Inject() (implicit val executionContext: ExecutionContext) extends CheckingSubmissionAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    if (request.userAnswers.submitted) {
      request.userAnswers.journeyType match {
        case CancelRegistration =>
          Future(Left(Redirect(controllers.cancelRegistration.routes.CancellationRequestDoneController.onPageLoad())))
        case UpdateRegisteredDetails =>
          Future(Left(Redirect(controllers.updateRegisteredDetails.routes.UpdateDoneController.onPageLoad())))
        case CorrectReturn =>
          Future(Left(Redirect(controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad)))
        case ChangeActivity =>
          Future(Left(Redirect(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad)))
      }
    } else {
      Future.successful(Right(request))
    }
  }
}

trait CheckingSubmissionAction extends ActionRefiner[DataRequest, DataRequest]
