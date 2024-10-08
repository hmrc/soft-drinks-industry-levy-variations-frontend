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
import errors.{SessionDatabaseInsertError, UnexpectedResponseFromSDIL}
import forms.correctReturn.AddASmallProducerFormProvider
import models.SelectChange.CorrectReturn
import models.correctReturn.AddASmallProducer
import models.correctReturn.AddASmallProducer.toSmallProducer
import models.submission.Litreage
import models.{CheckMode, EditMode, LitresInBands, NormalMode, SdilReturn, SmallProducer, UserAnswers}
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.AddASmallProducerView

import scala.concurrent.Future

class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AddASmallProducerFormProvider()
  val form = formProvider(emptyUserAnswersForCorrectReturn)

  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url
  lazy val checkAddASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(CheckMode).url
  lazy val editAddASmallProducerRoute = routes.AddASmallProducerController.onEditPageLoad(EditMode, sdilReference).url

  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
    if(userAnswers.fold(false)(_.correctReturnPeriod.nonEmpty)) {
      when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
      applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
    } else {
      applicationBuilder(userAnswers)
    }
  }

  "AddASmallProducer Controller" - {

    List(NormalMode, CheckMode, EditMode).foreach(mode => {
      val path = mode match {
        case NormalMode => addASmallProducerRoute
        case CheckMode => checkAddASmallProducerRoute
        case EditMode => editAddASmallProducerRoute
      }

      s"must return OK and the correct view for a GET in $mode" in {
        val smallProducer: AddASmallProducer = AddASmallProducer(Some("PRODUCER"), sdilReference, LitresInBands(10, 20))
        val userAnswers = userAnswersForCorrectReturn(false).copy(smallProducerList = List(toSmallProducer(smallProducer)))
        val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddASmallProducerView]

          status(result) mustEqual OK
          val sdilRef = if (mode == EditMode) Some(sdilReference) else None
          val preparedForm = if (mode == EditMode) form.fill(smallProducer) else form
          contentAsString(result) mustEqual view(preparedForm, mode, sdilRef)(request, messages(application)).toString
        }
      }

      s"must redirect to Select controller when return period has not been selected in $mode" in {
        val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None))).build()

        running(application) {
          val request = FakeRequest(GET, path)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SelectController.onPageLoad.url
        }
      }

      s"must redirect to the next page when valid data is submitted in $mode" in {

        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))
        when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn createSuccessVariationResult(Some(true))

        val application =
          correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
            .overrides(
              bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, path)
            .withFormUrlEncodedBody(
              ("producerName", "PRODUCER"),
              ("referenceNumber", "XKSDIL000000023"),
              ("litres.lowBand", "10"),
              ("litres.highBand", "20")
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

        val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

        running(application) {
          val request =
            FakeRequest(POST, path)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AddASmallProducerView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          val sdilRef = if (mode == EditMode) Some(sdilReference) else None
          contentAsString(result) mustEqual view(boundForm, mode, sdilRef)(request, messages(application)).toString
        }
      }

      s"must return a Bad Request and errors when data matching an already added small producer is submitted in $mode" in {
        val smallProducerSDILRef = "XCSDIL000456789"
        val smallProducerList: List[SmallProducer] = List(SmallProducer("MY SMALL PRODUCER", smallProducerSDILRef, Litreage(1L, 2L)))
        val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList))).build()

        running(application) {
          val request = FakeRequest(POST, path)
            .withFormUrlEncodedBody(
              ("producerName", "PRODUCER"),
              ("referenceNumber", smallProducerSDILRef),
              ("litres.lowBand", "10"),
              ("litres.highBand", "20")
            )

          val boundForm = form.bind(Map(
            "producerName" -> "PRODUCER",
            "referenceNumber" -> smallProducerSDILRef,
            "litres.lowBand" -> "10",
            "litres.highBand" -> "20"
          )).withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.exists"))

          val view = application.injector.instanceOf[AddASmallProducerView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          val sdilRef = if (mode == EditMode) Some(sdilReference) else None
          contentAsString(result) mustEqual view(boundForm, mode, sdilRef)(request, messages(application)).toString
        }
      }

      s"must redirect to the select controller when valid data without a return period is submitted in $mode" in {

        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

        val application =
          correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None)))
            .overrides(
              bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, path)
            .withFormUrlEncodedBody(
              ("producerName", "PRODUCER"),
              ("referenceNumber", "XKSDIL000000023"),
              ("litres.lowBand", "10"),
              ("litres.highBand", "20")
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SelectController.onPageLoad.url
        }
      }

      s"must return a Bad Request and errors when data with small producer status false is submitted in $mode" in {
        when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn createSuccessVariationResult(Some(false))
        val smallProducerList: List[SmallProducer] = List(SmallProducer("MY SMALL PRODUCER", "XCSDIL000456789", Litreage(1L, 2L)))
        val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList)
        )).build()

        running(application) {
          val request = FakeRequest(POST, path)
            .withFormUrlEncodedBody(
              ("producerName", "PRODUCER"),
              ("referenceNumber", "XKSDIL000000023"),
              ("litres.lowBand", "10"),
              ("litres.highBand", "20")
            )

          val boundForm = form.bind(Map(
            "producerName" -> "PRODUCER",
            "referenceNumber" -> "XKSDIL000000023",
            "litres.lowBand" -> "10",
            "litres.highBand" -> "20"
          )).withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer"))

          val view = application.injector.instanceOf[AddASmallProducerView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          val sdilRef = if (mode == EditMode) Some(sdilReference) else None
          contentAsString(result) mustEqual view(boundForm, mode, sdilRef)(request, messages(application)).toString
        }
      }

      s"must render the error page when call to get small producer status fails in $mode" in {

        val mockSessionService = mock[SessionService]

        when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn createFailureVariationResult(UnexpectedResponseFromSDIL)

        val application =
          correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
            .overrides(
              bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {

          val request = FakeRequest(POST, path)
            .withFormUrlEncodedBody(
              ("producerName", "PRODUCER"),
              ("referenceNumber", "XKSDIL000000023"),
              ("litres.lowBand", "10"),
              ("litres.highBand", "20")
            )

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          val page = Jsoup.parse(contentAsString(result))
          page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }

      s"should log an error message when internal server error is returned when user answers are not set in session repository in $mode" in {
        val mockSessionService = mock[SessionService]

        val application =
          correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
            .overrides(
              bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
              bind[SessionService].toInstance(mockSessionService))
            .build()

        when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn createSuccessVariationResult(Some(true))
        when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

        running(application) {
          withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>

            val request =
              FakeRequest(POST, path)
                .withFormUrlEncodedBody(
                  ("producerName", "PRODUCER"),
                  ("referenceNumber", "XKSDIL000000023"),
                  ("litres.lowBand", "10"),
                  ("litres.highBand", "20")
                )

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on addASmallProducer"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    })

    testInvalidJourneyType(CorrectReturn, addASmallProducerRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, addASmallProducerRoute)
    testNoUserAnswersError(addASmallProducerRoute)

  }
}
