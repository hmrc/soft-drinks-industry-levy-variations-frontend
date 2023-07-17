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

import handlers.ErrorHandler
import models.ReturnPeriod
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{ActionRefiner, Result}
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import services.SessionService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(val sessionService: SessionService,
                                        errorHandler: ErrorHandler,
                                        val sessionRepository: SessionRepository,
                                        val sdilSessionCache: SDILSessionCache
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = {
    val optionalDataRequest = for {
      userAnsOps <- sessionRepository.get(request.sdilEnrolment)
      optReturnPeriod <- sdilSessionCache.fetchEntry[ReturnPeriod](request.sdilEnrolment, SDILSessionKeys.RETURN_PERIOD)
    } yield OptionalDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnsOps, optReturnPeriod)
    val result = optionalDataRequest
      .map(Right(_))
      .recover { case e: Throwable => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request))) }
    result
//    sessionService.get(request.sdilEnrolment).map {
//      case Right(userAnsOps) => Right(OptionalDataRequest(request.request, request.sdilEnrolment, request.subscription, userAnsOps))
//      case Left(_) => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
//    }
  }
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, OptionalDataRequest]
