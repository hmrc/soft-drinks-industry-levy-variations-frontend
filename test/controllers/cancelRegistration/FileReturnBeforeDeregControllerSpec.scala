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

package controllers.cancelRegistration

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.SelectChange
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.cancelRegistration.FileReturnBeforeDeregView

import scala.concurrent.Future

class FileReturnBeforeDeregControllerSpec extends SpecBase {

  lazy val fileReturnBeforDeregRoute: String = routes.FileReturnBeforeDeregController.onPageLoad().url

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]

  "FileReturnBeforeDereg Controller" - {

    "must return OK and the correct view and message when there is more than one pending return for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).overrides(
        inject.bind[SoftDrinksIndustryLevyConnector].toInstance(mockConnector)
      ).build()

      when(mockConnector.returns_pending(any())(any())).thenReturn(Future.successful(Some(returnPeriods)))

      running(application) {
        val request = FakeRequest(GET, fileReturnBeforDeregRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReturnBeforeDeregView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Html(s"Before you can cancel your registration, you must send ${2} returns and make any payments due."))(request, messages(application)).toString
      }
    }

    "must display correct message when there is one pending return for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).overrides(
        inject.bind[SoftDrinksIndustryLevyConnector].toInstance(mockConnector)
      ).build()

      when(mockConnector.returns_pending(any())(any())).thenReturn(Future.successful(Some(returnPeriod)))

      running(application) {
        val request = FakeRequest(GET, fileReturnBeforDeregRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReturnBeforeDeregView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Html(s"Before you can cancel your registration, you must send a return for April to April 2018 and make any payments due."))(request, messages(application)).toString
      }
    }

    "must redirect when there are no pending returns GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).overrides(
        inject.bind[SoftDrinksIndustryLevyConnector].toInstance(mockConnector)
      ).build()

      when(mockConnector.returns_pending(any())(any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, fileReturnBeforDeregRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }
    testInvalidJourneyType(SelectChange.CancelRegistration, fileReturnBeforDeregRoute, false)
    testNoUserAnswersError(fileReturnBeforDeregRoute, false)

  }
}
