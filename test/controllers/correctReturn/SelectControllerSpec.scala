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
import connectors.SoftDrinksIndustryLevyConnector
import errors.SessionDatabaseInsertError
import forms.correctReturn.SelectFormProvider
import models.NormalMode
import models.correctReturn.Select
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.SelectPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.SelectView

import scala.concurrent.Future
class SelectControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectRoute: String = routes.SelectController.onPageLoad(NormalMode).url

  val formProvider = new SelectFormProvider()
  val form = formProvider()
  val returnsList = List(returnPeriodList)
  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val controller = application.injector.instanceOf[SelectController]

  "Select Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute)
        when(mockSdilConnector.retrieveSubscription(any, anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }

        when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
          Future.successful(Some(returnPeriodList))
        }

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, controller.seperateReturnYears(returnPeriodList))(request, messages(application)).toString
      }
    }

    "must redirect When returns is empty for GET" in {


      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {

        when(mockSdilConnector.retrieveSubscription(any, anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }

        when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
          Future.successful(None)
        }

        val request = FakeRequest(GET, selectRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]

        status(result) mustEqual SEE_OTHER

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn
      .set(SelectPage, Select.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute)

        when(mockSdilConnector.retrieveSubscription(any, anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }

        when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
          Future.successful(Some(returnPeriodList))
        }

        val view = application.injector.instanceOf[SelectView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Select.values.head), NormalMode, controller.seperateReturnYears(returnPeriodList))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
        Future.successful(Some(returnPeriodList))
      }

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) )
      .overrides(
        bind[NavigatorForCorrectReturn
      ].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)
      ),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the index page when no returns are found when submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(POST, selectRoute).withFormUrlEncodedBody(("value", "invalid value"))

        when(mockSdilConnector.retrieveSubscription(any, anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }

        when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
          Future.successful(None)
        }

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(POST, selectRoute).withFormUrlEncodedBody(("value", "invalid value"))

        when(mockSdilConnector.retrieveSubscription(any, anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }

        when(mockSdilConnector.returns_variable(any())(any())).thenReturn {
          Future.successful(Some(returnPeriodList))
        }

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, controller.seperateReturnYears(returnPeriodList))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }
    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, selectRoute
            )
            .withFormUrlEncodedBody(("value", Select.values.head.toString))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on select"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}