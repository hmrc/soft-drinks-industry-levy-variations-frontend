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

package controllers.changeActivity

import base.SpecBase
import controllers.changeActivity.routes._
import models.SelectChange.ChangeActivity
import navigation._
import play.api.inject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.changeActivity.SuggestDeregistrationView

class SuggestDeregistrationControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")
  lazy val suggestDeregistrationRoute: String = SuggestDeregistrationController.onPageLoad().url

  "SuggestDeregistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity)).build()

      running(application) {
        val request = FakeRequest(GET, suggestDeregistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SuggestDeregistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }


    "must redirect to the next page when the Cancel your registration button is clicked" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity))
          .overrides(
            inject.bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, suggestDeregistrationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad().url
      }
    }

    testInvalidJourneyType(ChangeActivity, suggestDeregistrationRoute)
    testNoUserAnswersError(suggestDeregistrationRoute)
  }
}
