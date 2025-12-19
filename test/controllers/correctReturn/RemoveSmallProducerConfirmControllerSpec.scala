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
import forms.correctReturn.RemoveSmallProducerConfirmFormProvider
import models.SelectChange.CorrectReturn
import models.submission.Litreage
import models.{ CheckMode, NormalMode, SdilReturn, SmallProducer, UserAnswers }
import navigation._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.RemoveSmallProducerConfirmPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.RemoveSmallProducerConfirmView

import scala.concurrent.Future

class RemoveSmallProducerConfirmControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveSmallProducerConfirmFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val removeSmallProducerConfirmRoute =
    routes.RemoveSmallProducerConfirmController.onPageLoad(NormalMode, s"$sdilReferenceParty").url
  lazy val removeSmallProducerConfirmCheckRoute =
    routes.RemoveSmallProducerConfirmController.onPageLoad(CheckMode, s"$sdilReferenceParty").url
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  def correctReturnAction(
    userAnswers: Option[UserAnswers],
    optOriginalReturn: Option[SdilReturn] = Some(emptySdilReturn)
  ): GuiceApplicationBuilder = {
    when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(optOriginalReturn))
    applicationBuilder(userAnswers = userAnswers)
      .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
  }
  "RemoveSmallProducerConfirm Controller" - {

    List((NormalMode, removeSmallProducerConfirmRoute), (CheckMode, removeSmallProducerConfirmCheckRoute))
      .foreach { case (mode, controllerRoute) =>
        s"must return OK and the correct view for a GET in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false)

          val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, controllerRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

            status(result) mustEqual OK
            val page = Jsoup.parse(contentAsString(result))
            page.title() must include(
              Messages("Are you sure you want to remove this small producer? - Soft Drinks Industry Levy - GOV.UK")
            )
            page.getElementsByTag("h1").text() mustEqual Messages(
              "Are you sure you want to remove this small producer?"
            )
            contentAsString(result) mustEqual view(form, mode, sdilReferenceParty, producerNameParty)(
              request,
              messages(application)
            ).toString
          }
        }

        s"must redirect to Exemptions Page when small producer not found for a GET in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false).copy(smallProducerList = List.empty)

          val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, controllerRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.ExemptionsForSmallProducersController.onPageLoad(mode).url
          }
        }

        s"must redirect to Small Producer Details user answers contains at least one small producer for a GET in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false).copy(smallProducerList =
            List(SmallProducer("", "XZSDIL000000234", Litreage(2000, 4000)))
          )

          val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, controllerRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.SmallProducerDetailsController.onPageLoad(mode).url
          }
        }

        s"must not populate the view on a GET when the question has previously been answered in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false)

          val application = correctReturnAction(userAnswers =
            Some(userAnswers.set(RemoveSmallProducerConfirmPage, true).success.value)
          ).build()

          running(application) {
            val request = FakeRequest(GET, controllerRoute)

            val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, mode, sdilReferenceParty, producerNameParty)(
              request,
              messages(application)
            ).toString
          }
        }

        s"must redirect to the next page when valid data is submitted in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false)

          val mockSessionService = mock[SessionService]

          when(mockSessionService.set(any())).thenReturn(Future.successful(Right(true)))

          val application =
            correctReturnAction(userAnswers = Some(userAnswers.set(RemoveSmallProducerConfirmPage, true).success.value))
              .overrides(
                bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
                bind[SessionService].toInstance(mockSessionService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, controllerRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        s"must redirect to Small Producer Details when small producer not found for a POST in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false).copy(smallProducerList = List.empty)

          val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, controllerRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.SmallProducerDetailsController.onPageLoad(mode).url
          }
        }

        s"must return a Bad Request and errors when invalid data is submitted in $mode" in {

          val userAnswers: UserAnswers = userAnswersForCorrectReturn(false)

          val application = correctReturnAction(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, controllerRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, mode, sdilReferenceParty, producerNameParty)(
              request,
              messages(application)
            ).toString
          }
        }

        s"should log an error message when internal server error is returned when user answers are not set in session repository in $mode" in {
          val mockSessionService = mock[SessionService]

          when(mockSessionService.set(any())).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

          val application =
            correctReturnAction(userAnswers = Some(userAnswersForCorrectReturn(false)))
              .overrides(
                bind[NavigatorForCorrectReturn].toInstance(new FakeNavigatorForCorrectReturn(onwardRoute)),
                bind[SessionService].toInstance(mockSessionService)
              )
              .build()

          running(application) {
            withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
              val request =
                FakeRequest(POST, controllerRoute)
                  .withFormUrlEncodedBody(("value", "false"))

              await(route(application, request).value)
              events
                .collectFirst { case event =>
                  event.getLevel.levelStr mustBe "ERROR"
                  event.getMessage mustEqual "Failed to set value in session repository while attempting set on removeSmallProducerConfirm"
                }
                .getOrElse(fail("No logging captured"))
            }
          }
        }
      }
    testInvalidJourneyType(CorrectReturn, removeSmallProducerConfirmRoute)
    testRedirectToPostSubmissionIfRequired(CorrectReturn, removeSmallProducerConfirmRoute)
    testNoUserAnswersError(removeSmallProducerConfirmRoute)
  }
}
