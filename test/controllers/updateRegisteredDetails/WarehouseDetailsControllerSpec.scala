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

package controllers.updateRegisteredDetails

import base.SpecBase
import base.SpecBase.{twoWarehouses, userAnswerTwoWarehousesUpdateRegisteredDetails}
import errors.SessionDatabaseInsertError
import forms.updateRegisteredDetails.WarehouseDetailsFormProvider
import models.{NormalMode, Warehouse}
import models.SelectChange.UpdateRegisteredDetails
import models.backend.UkAddress
import navigation._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.WarehouseDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.updateRegisteredDetails.WarehouseDetailsView
import views.summary.updateRegisteredDetails.WarehouseDetailsSummary

import scala.collection.immutable.Map
import scala.concurrent.Future

class WarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  def onwardRoute = Call("GET", "/foo")
  def doc(result: String): Document = Jsoup.parse(result)

  val formProvider = new WarehouseDetailsFormProvider()
  val form = formProvider()

  lazy val warehouseDetailsRoute = routes.WarehouseDetailsController.onPageLoad(NormalMode).url

  "WarehouseDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehousesUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val warehouseSummaryList: List[SummaryListRow] =
          WarehouseDetailsSummary.row2(twoWarehouses)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        status(result) mustEqual OK
        val summaryActions = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
        summaryActions.size() mustEqual 2
        summaryActions.first.text() must include("Remove")
        summaryActions.last.text() must include("Remove")

        val removeLink = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
          .tagName("ul").tagName("li").last().getElementsByClass("govuk-link").last()
        removeLink.attr("href") mustEqual "/soft-drinks-industry-levy-variations-frontend/change-registered-details/warehouse-details/remove/2"

        contentAsString(result) mustEqual view(form, NormalMode, Some(summaryList))(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode,  None)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with a warehouse in the list" in {

      val summaryList = Some(SummaryListViewModel(
        rows = WarehouseDetailsSummary.row2(Map("1" -> warehouse))
      ))

      val userAnswers = warehouseAddedToUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data (warehouses in the list) is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(warehouseAddedToUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, None)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, warehouseDetailsRoute)
    testNoUserAnswersError(warehouseDetailsRoute)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure(UpdateRegisteredDetails))).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute
        )
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
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, warehouseDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on warehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
