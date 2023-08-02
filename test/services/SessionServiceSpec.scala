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

package services

import base.SpecBase
import errors.{SessionDatabaseDeleteError, SessionDatabaseGetError, SessionDatabaseInsertError}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository

import scala.concurrent.Future

class SessionServiceSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  val sessionService = new SessionService(mockSessionRepository)

  "set" - {

    "must return true" - {
      "when no mongo errors occur" in {

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val res = sessionService.set(emptyUserAnswersForUpdateRegisteredDetails)

        whenReady(res) {result =>
          result mustBe Right(true)
        }
      }
    }

    "must return SessionDatabaseInsertError" - {
      "when a mongo errors occurs" in {

        when(mockSessionRepository.set(any())) thenReturn Future.failed(new Exception("error"))

        val res = sessionService.set(emptyUserAnswersForUpdateRegisteredDetails)

        whenReady(res) { result =>
          result mustBe Left(SessionDatabaseInsertError)
        }
      }
    }
  }

  "get" - {

    "must return useranswers" - {
      "when no mongo errors occur and a record exists" in {
        val record = emptyUserAnswersForUpdateRegisteredDetails

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(record))

        val res = sessionService.get("id")

        whenReady(res) { result =>
          result mustBe Right(Some(record))
        }
      }
    }


    "must return None" - {
      "when no mongo errors occur and a record doesn't exists" in {

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

        val res = sessionService.get("id")

        whenReady(res) { result =>
          result mustBe Right(None)
        }
      }
    }

    "must return SessionDatabaseGetError" - {
      "when a mongo errors occurs" in {

        when(mockSessionRepository.get(any())) thenReturn Future.failed(new Exception("error"))

        val res = sessionService.get("id")

        whenReady(res) { result =>
          result mustBe Left(SessionDatabaseGetError)
        }
      }
    }
  }

  "clear" - {

    "must return true" - {
      "when no mongo errors occur" in {

        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val res = sessionService.clear("id")

        whenReady(res) { result =>
          result mustBe Right(true)
        }
      }
    }

    "must return SessionDatabaseDeleteError" - {
      "when a mongo errors occurs" in {

        when(mockSessionRepository.clear(any())) thenReturn Future.failed(new Exception("error"))

        val res = sessionService.clear("id")

        whenReady(res) { result =>
          result mustBe Left(SessionDatabaseDeleteError)
        }
      }
    }
  }

  "keepAlive" - {

    "must return true" - {
      "when no mongo errors occur" in {

        when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

        val res = sessionService.keepAlive("id")

        whenReady(res) { result =>
          result mustBe Right(true)
        }
      }
    }

    "must return SessionDatabaseInsertError" - {
      "when a mongo errors occurs" in {

        when(mockSessionRepository.keepAlive(any())) thenReturn Future.failed(new Exception("error"))

        val res = sessionService.keepAlive("id")

        whenReady(res) { result =>
          result mustBe Left(SessionDatabaseInsertError)
        }
      }
    }
  }
}
