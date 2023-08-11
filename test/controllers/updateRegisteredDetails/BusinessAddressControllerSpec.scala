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
import models.SelectChange.UpdateRegisteredDetails
import models.backend.UkAddress
import navigation._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.inject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AddressLookupService, ContactDetails}
import views.html.updateRegisteredDetails.BusinessAddressView

import scala.concurrent.Future

class BusinessAddressControllerSpec extends SpecBase {

  lazy val businessAddressRoute = routes.BusinessAddressController.onPageLoad().url
  lazy val businessAddress: UkAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")
  def onwardRoute: Call = Call("GET", "/soft-drinks-industry-levy-variations-frontend")

  "BusinessAddress Controller" - {

    "must redirect to ALF" in {
      val mockAddressLookupService: AddressLookupService = mock[AddressLookupService]
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(inject.bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      when(mockAddressLookupService
        .initJourneyAndReturnOnRampUrl(ArgumentMatchers.eq(ContactDetails), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful("woohooherewegoo!"))

      running(application) {
        val request =
          FakeRequest(GET, controllers.updateRegisteredDetails.routes.BusinessAddressController.changeAddress().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "woohooherewegoo!"
      }
    }

    "must return error when attempting to navigate to ALF but ALF returns error" in {
      val mockAddressLookupService: AddressLookupService = mock[AddressLookupService]
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(inject.bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      when(mockAddressLookupService
        .initJourneyAndReturnOnRampUrl(ArgumentMatchers.eq(ContactDetails), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.failed(new Exception("uh oh spagetio")))

      running(application) {
        val request =
          FakeRequest(GET, controllers.updateRegisteredDetails.routes.BusinessAddressController.changeAddress().url)
        val result = route(application, request).value

        intercept[Exception](await(result))
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(GET, businessAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(List(contactAddress))(request, messages(application)).toString
      }
    }

    "must redirect to the CYA page when the Save and continue button is clicked" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails))
          .overrides(
            inject.bind[NavigatorForUpdateRegisteredDetails].toInstance(new FakeNavigatorForUpdateRegisteredDetails(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessAddressRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url
      }
    }

    "test invalid scenarios for main business address route" - {
      testInvalidJourneyType(UpdateRegisteredDetails, businessAddressRoute)
      testNoUserAnswersError(businessAddressRoute)
    }
  }
}
