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
import forms.correctReturn.PackagingSiteDetailsFormProvider
import models.SelectChange.CorrectReturn
import models.backend.{Site, UkAddress}
import models.{CheckMode, LitresInBands, Mode, NormalMode, UserAnswers}
import navigation._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.{HowManyBroughtIntoUKPage, PackagingSiteDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressLookupService, PackingDetails, SessionService}
import viewmodels.govuk.SummaryListFluency
import views.html.correctReturn.PackagingSiteDetailsView
import views.summary.correctReturn.PackagingSiteDetailsSummary

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar  with SummaryListFluency{

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new PackagingSiteDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val packagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  lazy val packagingSiteDetailsCheckRoute: String = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url

  def packagingSiteDetailsRouteForMode(mode: Mode): String = if (mode == CheckMode) packagingSiteDetailsCheckRoute else packagingSiteDetailsRoute

  lazy val emptyUserAnswersForCorrectReturnWithPackagingSite = emptyUserAnswersForCorrectReturn.copy(packagingSiteList = packingSiteMap)

  "PackagingSiteDetails Controller within Correct Returns" - {

    List(NormalMode, CheckMode).foreach(mode => {
      s"must return OK and the correct view for a GET in $mode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturnWithPackagingSite)).build()

        val summary = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(packingSiteMap, mode)
        )

        running(application) {
          val request = FakeRequest(GET, packagingSiteDetailsRouteForMode(mode))

          val result = route(application, request).value

          val view = application.injector.instanceOf[PackagingSiteDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form,mode, summary)(request, messages(application)).toString
        }
      }

      s"must populate the view correctly on a GET when the question has previously been answered in $mode" in {

        val summary = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(packingSiteMap, mode)
        )

        val userAnswers = emptyUserAnswersForCorrectReturnWithPackagingSite.set(PackagingSiteDetailsPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, packagingSiteDetailsRouteForMode(mode))

          val view = application.injector.instanceOf[PackagingSiteDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), mode, summary)(request, messages(application)).toString
        }
      }
    })

    "must redirect to ask secondary warehouse when user does match new importer when the data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      val PackagingSite1 = Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        None,
        Some("Wild Lemonade Group"),
        None)
      lazy val packagingSiteListWith1 = Map(("78941132", PackagingSite1))
      lazy val newImporterAnswer : UserAnswers = emptyUserAnswersForCorrectReturn.set(HowManyBroughtIntoUKPage, LitresInBands(5,6)).success.value.copy(packagingSiteList = packagingSiteListWith1)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn createSuccessVariationResult(Some(aSubscription))

      val application =
        applicationBuilder(userAnswers = Some(newImporterAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the Check your answers page when false is answered and doesn't match a new importer" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsCheckRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad.url
      }
    }

    testInvalidJourneyType(CorrectReturn, packagingSiteDetailsRoute)
    testNoUserAnswersError(packagingSiteDetailsRoute)

    List(NormalMode, CheckMode).foreach(mode => {
      s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

        val summary = SummaryListViewModel(
          rows = PackagingSiteDetailsSummary.row2(packingSiteMap, mode)
        )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturnWithPackagingSite)).build()

        running(application) {
          val request =
            FakeRequest(POST, packagingSiteDetailsRouteForMode(mode))
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PackagingSiteDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, mode, summary)(request, messages(application)).toString
        }
      }
    })

    "must redirect to the next page when valid data is submitted (true)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
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
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

  }
}
