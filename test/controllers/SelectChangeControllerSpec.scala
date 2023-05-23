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

package controllers

import base.SpecBase
import forms.SelectChangeFormProvider
import models.{NormalMode, SelectChange, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.SelectChangeView

import scala.concurrent.Future

class SelectChangeControllerSpec extends SpecBase with MockitoSugar {

  lazy val selectChangeRoute = routes.SelectChangeController.onPageLoad(NormalMode).url

  val formProvider = new SelectChangeFormProvider()
  val form = formProvider()

  "OwnBrands Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selectChangeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectChangeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(sdilNumber, SelectChange.values.head)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, selectChangeRoute)

        val view = application.injector.instanceOf[SelectChangeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SelectChange.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selectChangeRoute)
            .withFormUrlEncodedBody(("value", SelectChange.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, selectChangeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectChangeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
