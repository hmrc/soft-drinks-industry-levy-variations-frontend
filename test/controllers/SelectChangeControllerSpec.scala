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
import errors.UnexpectedResponseFromSDIL
import forms.SelectChangeFormProvider
import models.backend.{ RetrievedActivity, RetrievedSubscription, UkAddress }
import models.{ Contact, NormalMode, SelectChange, UserAnswers }
import orchestrators.SelectChangeOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SelectChangeView

import java.time.LocalDate

class SelectChangeControllerSpec extends SpecBase with MockitoSugar {

  lazy val selectChangeRoute = routes.SelectChangeController.onPageLoad.url

  val mockOrchestrator = mock[SelectChangeOrchestrator]

  val formProvider = new SelectChangeFormProvider()
  val form = formProvider()

  "OwnBrands Controller" - {
    List(true, false).foreach { hasVariableReturns =>
      if (hasVariableReturns) {
        "when there are pending returns"
      } else
        {
          "when no pending returns"
        } - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = None)
              .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
              .build()

            running(application) {
              when(mockOrchestrator.hasReturnsToCorrect(any())(using any(), any())).thenReturn(
                createSuccessVariationResult(
                  hasVariableReturns
                )
              )

              val request = FakeRequest(GET, selectChangeRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SelectChangeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, hasVariableReturns)(using
                request,
                messages(application)
              ).toString
            }
          }

          "must return OK and display the view correctly when the user is deregistered and has submitted returns" in {
            val userAnswers = UserAnswers(sdilNumber, SelectChange.values.head, contactAddress = contactAddress)
            val retrievedSubscription = RetrievedSubscription(
              "foo",
              "bar",
              "wizz",
              UkAddress(List.empty, ""),
              RetrievedActivity(
                smallProducer = false,
                largeProducer = false,
                contractPacker = false,
                importer = false,
                voluntaryRegistration = false
              ),
              LocalDate.now(),
              productionSites = List.empty,
              warehouseSites = List.empty,
              contact = Contact(None, None, "", ""),
              deregDate = Some(LocalDate.of(2023, 1, 1))
            )

            val application =
              applicationBuilder(userAnswers = Some(userAnswers), subscription = Some(retrievedSubscription))
                .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
                .build()

            running(application) {
              when(mockOrchestrator.hasReturnsToCorrect(any())(using any(), any())).thenReturn(
                createSuccessVariationResult(
                  hasVariableReturns
                )
              )
              val request = FakeRequest(GET, selectChangeRoute)

              val view = application.injector.instanceOf[SelectChangeView]

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(
                form.fill(SelectChange.values.head),
                hasVariableReturns,
                isDeregistered = true
              )(using request, messages(application)).toString
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered and submitted is false" in {

            val userAnswers =
              UserAnswers(sdilNumber, SelectChange.values.head, contactAddress = contactAddress, submitted = false)

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
              .build()

            running(application) {
              when(mockOrchestrator.hasReturnsToCorrect(any())(using any(), any())).thenReturn(
                createSuccessVariationResult(
                  hasVariableReturns
                )
              )
              val request = FakeRequest(GET, selectChangeRoute)

              val view = application.injector.instanceOf[SelectChangeView]

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(SelectChange.values.head), hasVariableReturns)(using
                request,
                messages(application)
              ).toString
            }
          }

          "must return OK and the correct view for a GET when the question has previously been answered and submitted is true" in {

            val userAnswers =
              UserAnswers(sdilNumber, SelectChange.values.head, contactAddress = contactAddress, submitted = true)

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
              .build()

            running(application) {
              when(mockOrchestrator.hasReturnsToCorrect(any())(using any(), any())).thenReturn(
                createSuccessVariationResult(
                  hasVariableReturns
                )
              )
              val request = FakeRequest(GET, selectChangeRoute)

              val view = application.injector.instanceOf[SelectChangeView]

              val result = route(application, request).value

              status(result) mustEqual OK

              contentAsString(result) mustEqual view(form, hasVariableReturns)(using
                request,
                messages(application)
              ).toString
            }
          }
        }
    }

    "when an error occurs when calling the backend" - {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        when(mockOrchestrator.hasReturnsToCorrect(any())(using any(), any())).thenReturn(
          createFailureVariationResult(
            UnexpectedResponseFromSDIL
          )
        )

        val request = FakeRequest(GET, selectChangeRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    SelectChange.values.foreach { selectChangeValue =>
      s"must redirect to the next page when ${selectChangeValue.toString} selected" in {

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
            .build()

        running(application) {
          when(
            mockOrchestrator.createUserAnswersAndSaveToDatabase(any(), any())(using any(), any())
          ).thenReturn(createSuccessVariationResult((): Unit))

          val request =
            FakeRequest(POST, selectChangeRoute)
              .withFormUrlEncodedBody(("value", selectChangeValue.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          val expectedNextPage = selectChangeValue match {
            case SelectChange.UpdateRegisteredDetails =>
              updateRegisteredDetails.routes.ChangeRegisteredDetailsController.onPageLoad()
            case SelectChange.ChangeActivity => changeActivity.routes.AmountProducedController.onPageLoad(NormalMode)
            case SelectChange.CorrectReturn  => correctReturn.routes.SelectController.onPageLoad
            case _                           => cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)
          }
          redirectLocation(result).value mustEqual expectedNextPage.url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
        .overrides(bind[SelectChangeOrchestrator].toInstance(mockOrchestrator))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, selectChangeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectChangeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(using request, messages(application)).toString
      }
    }
  }
}
