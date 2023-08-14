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
import errors.{NoSdilReturnForPeriod, NoVariableReturns, UnexpectedResponseFromSDIL}
import forms.correctReturn.SelectFormProvider
import models.SelectChange.CorrectReturn
import models.{NormalMode, ReturnPeriod}
import orchestrators.CorrectReturnOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.correctReturn.SelectView
class SelectControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectRoute: String = routes.SelectController.onPageLoad.url

  val formProvider = new SelectFormProvider()
  val form = formProvider()
  val returnsList = List(returnPeriodList)
  val mockOrchestrator: CorrectReturnOrchestrator = mock[CorrectReturnOrchestrator]
  val controller = application.injector.instanceOf[SelectController]

  "Select Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None))).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute)
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createSuccessVariationResult(returnPeriodList)
        }
        val separatedByYearAndSortedReturnPeriods = Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)

        when(mockOrchestrator.separateReturnPeriodsByYear(returnPeriodList)).thenReturn(separatedByYearAndSortedReturnPeriods)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, separatedByYearAndSortedReturnPeriods)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute)
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createSuccessVariationResult(returnPeriodList)
        }

        val separatedByYearAndSortedReturnPeriods = Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)

        when(mockOrchestrator.separateReturnPeriodsByYear(returnPeriodList)).thenReturn(separatedByYearAndSortedReturnPeriods)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(returnPeriodsFor2022.head.radioValue), separatedByYearAndSortedReturnPeriods)(request, messages(application)).toString
      }
    }


    "must redirect to SelectChange page when returns is empty for GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {

        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createFailureVariationResult(NoVariableReturns)
        }

        val request = FakeRequest(GET, selectRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SelectChangeController.onPageLoad.url

      }
    }

    "must render the error page when the backend call get variable returns fails for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createFailureVariationResult(UnexpectedResponseFromSDIL)
        }

        val request =
          FakeRequest(GET, selectRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "on submit POST" - {
      "when a valid return period selected and no errors occur" - {
        "must redirect to own brands for a user who is not a small producer" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
            bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
          ).build()

          running(application) {
            when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
              createSuccessVariationResult(returnPeriodList)
            }
            when(mockOrchestrator.setupUserAnswersForCorrectReturn(any(), any(),
              any())(any(), any())
            ).thenReturn {
              createSuccessVariationResult((): Unit)
            }

            val request =
              FakeRequest(POST, selectRoute
              )
                .withFormUrlEncodedBody(("value", returnPeriodList.head.radioValue))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url
          }
        }

        "must redirect to copacker page for a user who is a small producer" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn), Some(subscriptionSmallProducer)).overrides(
            bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
          ).build()

          running(application) {
            when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
              createSuccessVariationResult(returnPeriodList)
            }
            when(mockOrchestrator.setupUserAnswersForCorrectReturn(any(), any(),
              any())(any(), any())
            ).thenReturn {
              createSuccessVariationResult((): Unit)
            }

            val request =
              FakeRequest(POST, selectRoute
              )
                .withFormUrlEncodedBody(("value", returnPeriodList.head.radioValue))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url
          }
        }
      }
    }

    "must return a Bad Request and errors when no return period selected" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn), Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createSuccessVariationResult(returnPeriodList)
        }

        val separatedByYearAndSortedReturnPeriods = Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)

        when(mockOrchestrator.separateReturnPeriodsByYear(returnPeriodList)).thenReturn(separatedByYearAndSortedReturnPeriods)

        val request =
          FakeRequest(POST, selectRoute
          )
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]
        val formWithError = form.withError(FormError("value", "correctReturn.select.error.required"))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithError, separatedByYearAndSortedReturnPeriods)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when return period selected is not in variableReturnList" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn), Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createSuccessVariationResult(returnPeriodList)
        }

        val separatedByYearAndSortedReturnPeriods = Map(2022 -> returnPeriodsFor2022, 2020 -> returnPeriodsFor2020)

        when(mockOrchestrator.separateReturnPeriodsByYear(returnPeriodList)).thenReturn(separatedByYearAndSortedReturnPeriods)

        val request =
          FakeRequest(POST, selectRoute
          )
            .withFormUrlEncodedBody(("value", ReturnPeriod(2023, 0).radioValue))

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]
        val formWithError = form.withError(FormError("value", "correctReturn.select.error.required"))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithError, separatedByYearAndSortedReturnPeriods)(request, messages(application)).toString
      }
    }

    "must redirect to selectChange when there are no variable returns" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn), Some(subscriptionSmallProducer)).overrides(
        bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
      ).build()

      running(application) {
        when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
          createFailureVariationResult(NoVariableReturns)
        }

        val request =
          FakeRequest(POST, selectRoute
          )
            .withFormUrlEncodedBody(("value", returnPeriodList.head.radioValue))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SelectChangeController.onPageLoad.url
      }
    }

    "must render the error page" - {
      "when the call to get return periods fails" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
            bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
          ).build()

          running(application) {
            when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
              createFailureVariationResult(UnexpectedResponseFromSDIL)
            }

            val request =
              FakeRequest(POST, selectRoute
              )
                .withFormUrlEncodedBody(("value", returnPeriodList.head.radioValue))

            val result = route(application, request).value

            status(result) mustEqual INTERNAL_SERVER_ERROR
            val page = Jsoup.parse(contentAsString(result))
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }

      "when no sdilReturn was found for the selected variable return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).overrides(
          bind[CorrectReturnOrchestrator].toInstance(mockOrchestrator)
        ).build()

        running(application) {
          when(mockOrchestrator.getReturnPeriods(any())(any(), any())).thenReturn {
            createSuccessVariationResult(returnPeriodList)
          }
          when(mockOrchestrator.setupUserAnswersForCorrectReturn(any(), any(),
            any())(any(), any())
          ).thenReturn {
            createFailureVariationResult(NoSdilReturnForPeriod)
          }

          val request =
            FakeRequest(POST, selectRoute
            )
              .withFormUrlEncodedBody(("value", returnPeriodList.head.radioValue))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          val page = Jsoup.parse(contentAsString(result))
          page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }

    testInvalidJourneyType(CorrectReturn, selectRoute)
    testNoUserAnswersError(selectRoute)
  }
}