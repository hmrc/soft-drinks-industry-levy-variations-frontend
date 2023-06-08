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

package controllers.FileReturnBeforeDereg

import base.SpecBase
import controllers.cancelRegistration.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.cancelRegistration.FileReturnBeforeDeregView

class FileReturnBeforeDeregControllerSpec extends SpecBase {

  lazy val fileReturnBeforDeregRoute: String = routes.FileReturnBeforeDeregController.onPageLoad().url

  "FileReturnBeforeDereg Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCancelRegistration)).build()

      running(application) {
        val request = FakeRequest(GET, fileReturnBeforDeregRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReturnBeforeDeregView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Html("Before you can cancel your registration, you must send 20 returns and make any payments due."))(request, messages(application)).toString
      }
    }
  }
}
