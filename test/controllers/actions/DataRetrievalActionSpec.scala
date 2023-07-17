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

import base.SpecBase
import errors.SessionDatabaseGetError
import handlers.ErrorHandler
import models.{ReturnPeriod, SelectChange, UserAnswers}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import play.api.test.FakeRequest
import play.twirl.api.Html
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionService: SessionService, errorHandler: ErrorHandler, sessionRepository: SessionRepository, sdilSessionCache: SDILSessionCache)
    extends DataRetrievalActionImpl(sessionService, errorHandler, sessionRepository, sdilSessionCache) {
    def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionService = mock[SessionService]
        val errorHandler = mock[ErrorHandler]
        val sessionRepository = mock[SessionRepository]
        val sdilSessionCache = mock[SDILSessionCache]
        when(sessionService.get("id")) thenReturn Future(Right(None))
        when(sessionRepository.get("id")) thenReturn Future(None)
//        TODO: Set return period to None
        when(sdilSessionCache.fetchEntry[ReturnPeriod]("id", SDILSessionKeys.RETURN_PERIOD)) thenReturn Future(Some(returnPeriod.head))
        val action = new Harness(sessionService, errorHandler, sessionRepository, sdilSessionCache)

        val result = action.callRefine(IdentifierRequest(FakeRequest(), "id", aSubscription)).futureValue

        result.map(_.userAnswers.isDefined) mustBe Right(false)
      }
    }

    "when there is data in the cache" - {

//      TODO: Test for both return period Some and None

      "must build a userAnswers object and add it to the request" in {

        val sessionService = mock[SessionService]
        val errorHandler = mock[ErrorHandler]
        val sessionRepository = mock[SessionRepository]
        val sdilSessionCache = mock[SDILSessionCache]
        when(sessionService.get("id")) thenReturn Future(Right(Some(UserAnswers("id", SelectChange.UpdateRegisteredDetails))))
        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id", journeyType = SelectChange.UpdateRegisteredDetails)))
        when(sdilSessionCache.fetchEntry[ReturnPeriod]("id", SDILSessionKeys.RETURN_PERIOD)) thenReturn Future(Some(returnPeriod.head))
        val action = new Harness(sessionService, errorHandler, sessionRepository, sdilSessionCache)

        val result = action.callRefine(new IdentifierRequest(FakeRequest(), "id", aSubscription)).futureValue

        result.map(_.userAnswers.isDefined) mustBe Right(true)
      }
    }

//    TODO: This no longer works - need to migrate away from use of sessionRepository
    "when a database error occurs" - {
      "must render the internal error page" in {
        val sessionService = mock[SessionService]
        val errorHandler = mock[ErrorHandler]
        val sessionRepository = mock[SessionRepository]
        val sdilSessionCache = mock[SDILSessionCache]
        when(sessionService.get("id")) thenReturn Future(Left(SessionDatabaseGetError))
        when(errorHandler.internalServerErrorTemplate(any())) thenReturn(Html("error"))
        when(sessionRepository.get("id")) thenReturn Future(None)
        when(sdilSessionCache.fetchEntry[ReturnPeriod]("id", SDILSessionKeys.RETURN_PERIOD)) thenReturn Future(Some(returnPeriod.head))
        val action = new Harness(sessionService, errorHandler, sessionRepository, sdilSessionCache)

        val result = action.callRefine(new IdentifierRequest(FakeRequest(), "id", aSubscription)).futureValue

        result mustBe Left(InternalServerError(Html("error")))
      }
    }
  }
}
