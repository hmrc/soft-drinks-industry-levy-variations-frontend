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
import errors.SessionDatabaseInsertError
import forms.correctReturn.SecondaryWarehouseDetailsFormProvider
import models.NormalMode
import models.SelectChange.CorrectReturn
import models.backend.{Site, UkAddress}
import models.updateRegisteredDetails.ChangeRegisteredDetails
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.SecondaryWarehouseDetailsPage
import pages.updateRegisteredDetails.ChangeRegisteredDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressLookupService, SessionService, WarehouseDetails}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.correctReturn.SecondaryWarehouseDetailsSummary
import views.html.correctReturn.SecondaryWarehouseDetailsView

import scala.concurrent.Future

class SecondaryWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form = formProvider()

  lazy val secondaryWarehouseDetailsRoute = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url

  "SecondaryWarehouseDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn.copy(warehouseList = twoWarehouses))).build()

      running(application) {

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(twoWarehouses, NormalMode)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn.set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers.copy(warehouseList = twoWarehouses))).build()

      running(application) {

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(twoWarehouses, NormalMode)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val userAnswers = emptyUserAnswersForCorrectReturn

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehouses)).build()

      running(application) {

        val WarhouseMap: Map[String,Site] =
          Map("1"-> Site(UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX"), Some("ABC Ltd")),
            "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE"), Some("Super Cola Ltd")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(WarhouseMap, NormalMode)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, secondaryWarehouseDetailsRoute)
    testNoUserAnswersError(secondaryWarehouseDetailsRoute)

    "must fail if the setting of userAnswers fails" in {
      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(CorrectReturn))).build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
        .withFormUrlEncodedBody(("value", "true"))

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
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, secondaryWarehouseDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on secondaryWarehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
