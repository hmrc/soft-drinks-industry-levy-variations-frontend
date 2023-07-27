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
import forms.correctReturn.AddASmallProducerFormProvider
import models.{NormalMode, SmallProducer}
import models.SelectChange.CorrectReturn
import models.correctReturn.AddASmallProducer
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.{AddASmallProducerPage, SelectPage}
import play.api.data.FormError
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.correctReturn.AddASmallProducerView

import scala.concurrent.Future

class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AddASmallProducerFormProvider()
  val form = formProvider(emptyUserAnswersForCorrectReturn)

  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

  "AddASmallProducer Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddASmallProducerView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val smallProducer: AddASmallProducer = AddASmallProducer(Some("PRODUCER"), sdilNumber, 10, 20)
      val userAnswers = emptyUserAnswersForCorrectReturn.set(AddASmallProducerPage, smallProducer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(smallProducer), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.set(SelectPage, returnPeriod.head).success.value))
      .overrides(
        bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
        bind[SessionService].toInstance(mockSessionService),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      )
      .build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerRoute)
          .withFormUrlEncodedBody(
            ("producerName", "PRODUCER"),
            ("referenceNumber", "XKSDIL000000023"),
            ("lowBand", "10"),
            ("highBand", "20")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when data matching an already added small producer is submitted" in {
      val smallProducerSDILRef = "XCSDIL000456789"
      val smallProducerList: List[SmallProducer] = List(SmallProducer("MY SMALL PRODUCER", smallProducerSDILRef, (1L, 2L)))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList))).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerRoute)
          .withFormUrlEncodedBody(
            ("producerName", "PRODUCER"),
            ("referenceNumber", smallProducerSDILRef),
            ("lowBand", "10"),
            ("highBand", "20")
          )

        val boundForm = form.bind(Map(
          "producerName" -> "PRODUCER",
          "referenceNumber" -> smallProducerSDILRef,
          "lowBand" -> "10",
          "highBand" -> "20"
        )).withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.exists"))

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the Index controller when valid data without a return period is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerRoute)
          .withFormUrlEncodedBody(
            ("producerName", "PRODUCER"),
            ("referenceNumber", "XKSDIL000000023"),
            ("lowBand", "10"),
            ("highBand", "20")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when data with small producer status false is submitted" in {
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(false))
      val smallProducerList: List[SmallProducer] = List(SmallProducer("MY SMALL PRODUCER", "XCSDIL000456789", (1L, 2L)))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(smallProducerList = smallProducerList)
        .set(SelectPage, returnPeriod.head).success.value)
      ).overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerRoute)
          .withFormUrlEncodedBody(
            ("producerName", "PRODUCER"),
            ("referenceNumber", "XKSDIL000000023"),
            ("lowBand", "10"),
            ("highBand", "20")
          )

        val boundForm = form.bind(Map(
          "producerName" -> "PRODUCER",
          "referenceNumber" -> "XKSDIL000000023",
          "lowBand" -> "10",
          "highBand" -> "20"
        )).withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer"))

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, addASmallProducerRoute)
    testNoUserAnswersError(addASmallProducerRoute)
  }
}
