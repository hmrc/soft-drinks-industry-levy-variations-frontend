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
import forms.correctReturn.PackAtBusinessAddressFormProvider
import models.SelectChange.CorrectReturn
import models.backend.{RetrievedSubscription, UkAddress}
import models.{NormalMode, SdilReturn, UserAnswers}
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString, eq => matching}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.PackAtBusinessAddressPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.correctReturn.PackAtBusinessAddressView

import scala.concurrent.Future

class PackAtBusinessAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PackAtBusinessAddressFormProvider()
  val form = formProvider()
  var usersRetrievedSubscription: RetrievedSubscription = aSubscription
  val businessName: String = usersRetrievedSubscription.orgName
  val businessAddress: UkAddress = usersRetrievedSubscription.address

  lazy val packAtBusinessAddressRoute = routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(userAnswers: Option[UserAnswers], optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }

  "PackAtBusinessAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)
        when(mockSdilConnector.retrieveSubscription(matching("XCSDIL000000002"), anyString())(any())).thenReturn {
          createSuccessVariationResult(Some(aSubscription))
        }
        val result = route(application, request).value

        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        status(result) mustEqual OK
        val address = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))
        contentAsString(result) mustEqual view(form, NormalMode, address)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn.set(PackAtBusinessAddressPage, true).success.value

      val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)
        when(mockSdilConnector.retrieveSubscription(matching("XCSDIL000000002"), anyString())(any())).thenReturn {
          createSuccessVariationResult(Some(aSubscription))
        }
        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        val address = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, address)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted - true" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))

        status(result) mustEqual BAD_REQUEST
        val address = AddressFormattingHelper.addressFormatting(businessAddress, Option(businessName))
        contentAsString(result) mustEqual view(boundForm, NormalMode, address)(request, messages(application)).toString

        //noinspection ComparingUnrelatedTypes
        page.getElementsContainingText(usersRetrievedSubscription.orgName).isEmpty mustBe false
        //noinspection ComparingUnrelatedTypes
        usersRetrievedSubscription.address.lines.foreach { line =>
          page.getElementsContainingText(line).isEmpty mustBe false
        }
        page.getElementsContainingText(usersRetrievedSubscription.address.postCode).isEmpty mustBe false

        page.getElementsByTag("a").text() must include(Messages("correctReturn.packAtBusinessAddress.error.required"))
      }
    }

    testInvalidJourneyType(CorrectReturn, packAtBusinessAddressRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, packAtBusinessAddressRoute)
    testNoUserAnswersError(packAtBusinessAddressRoute)

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        correctReturnAction(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          ).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packAtBusinessAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packAtBusinessAddress"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
