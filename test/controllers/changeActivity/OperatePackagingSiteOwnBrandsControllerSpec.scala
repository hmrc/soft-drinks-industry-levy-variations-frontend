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
import errors.SessionDatabaseInsertError
import forms.changeActivity.OperatePackagingSiteOwnBrandsFormProvider
import models.{NormalMode, UserAnswers}
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.changeActivity._
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.changeActivity.OperatePackagingSiteOwnBrandsView

import scala.concurrent.Future

class OperatePackagingSiteOwnBrandsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new OperatePackagingSiteOwnBrandsFormProvider()
  val form: Form[Boolean] = formProvider()
  val operateOwnBrandsJourneyUserAnswers: UserAnswers = emptyUserAnswersForChangeActivity
    .set(ContractPackingPage, true).success.value
    .set(AmountProducedPage, AmountProduced.Small).success.value
    .set(ThirdPartyPackagersPage, true).success.value

  lazy val operatePackagingSiteOwnBrandsRoute: String = routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url

  "OperatePackagingSiteOwnBrands Controller" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(operateOwnBrandsJourneyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, operatePackagingSiteOwnBrandsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OperatePackagingSiteOwnBrandsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = operateOwnBrandsJourneyUserAnswers.set(OperatePackagingSiteOwnBrandsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, operatePackagingSiteOwnBrandsRoute)

        val view = application.injector.instanceOf[OperatePackagingSiteOwnBrandsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect before On Page Load if a previous page has not been answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForChangeActivity))
        .overrides(
          bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity(onwardRoute))
        )
        .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, operatePackagingSiteOwnBrandsRoute)
          val result = route(application, request).value

          await(route(application, request).value)

          status(result) mustEqual 303
          redirectLocation(result).value mustEqual routes.AmountProducedController.onPageLoad(NormalMode).url
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(operateOwnBrandsJourneyUserAnswers))
          .overrides(
            bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, operatePackagingSiteOwnBrandsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(operateOwnBrandsJourneyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, operatePackagingSiteOwnBrandsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OperatePackagingSiteOwnBrandsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(ChangeActivity, operatePackagingSiteOwnBrandsRoute)
    testRedirectToPostSubmissionIfRequired(ChangeActivity, operatePackagingSiteOwnBrandsRoute)
    testNoUserAnswersError(operatePackagingSiteOwnBrandsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(ChangeActivity))).build()

      running(application) {
        val request =
          FakeRequest(POST, operatePackagingSiteOwnBrandsRoute
        )
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(operateOwnBrandsJourneyUserAnswers))
          .overrides(
            bind[NavigatorForChangeActivity].toInstance(new FakeNavigatorForChangeActivity (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, operatePackagingSiteOwnBrandsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on operatePackagingSiteOwnBrands"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
