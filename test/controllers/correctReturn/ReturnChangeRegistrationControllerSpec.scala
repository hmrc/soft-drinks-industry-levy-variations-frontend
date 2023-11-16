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

package controllers.correctReturn

import base.SpecBase
import models.SelectChange.CorrectReturn
import models.{LitresInBands, NormalMode}
import pages.correctReturn.{BroughtIntoUKPage, HowManyBroughtIntoUKPage, PackagedAsContractPackerPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.correctReturn.ReturnChangeRegistrationView
class ReturnChangeRegistrationControllerSpec extends SpecBase {

  lazy val returnChangeRegistrationRoute: String = routes.ReturnChangeRegistrationController.onPageLoad(NormalMode).url

  "ReturnChangeRegistration Controller" - {

    "must return OK and the correct view for a GET when user is a new importer " in {
      val userAnswers = userAnswersForCorrectReturnWithEmptySdilReturn
        .set(PackagedAsContractPackerPage, false).success.value
        .set(BroughtIntoUKPage, true).success.value
        .set(HowManyBroughtIntoUKPage, LitresInBands(1L, 1L)).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, returnChangeRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnChangeRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode,
          "/soft-drinks-industry-levy-variations-frontend/correct-return/brought-into-uk")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when user is a new packager " in {
      val application = applicationBuilder(userAnswers = Some(completedUserAnswersForCorrectReturnNewPackerOrImporter)).build()

      running(application) {
        val request = FakeRequest(GET, returnChangeRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnChangeRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode,
          "/soft-drinks-industry-levy-variations-frontend/correct-return/packaged-as-contract-packer")(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, returnChangeRegistrationRoute)
    testNoUserAnswersError(returnChangeRegistrationRoute)
  }
}
