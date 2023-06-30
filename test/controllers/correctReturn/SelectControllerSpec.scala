package controllers.correctReturn

import base.SpecBase
import forms.correctReturn.SelectFormProvider
import models.NormalMode
import models.correctReturn.Select
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.correctReturn.SelectPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.correctReturn.SelectView
import errors.SessionDatabaseInsertError
import org.jsoup.Jsoup
import scala.concurrent.Future
import utilities.GenericLogger
class SelectControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectRoute: String = routes.SelectController.onPageLoad(NormalMode).url

  val formProvider = new SelectFormProvider()
  val form = formProvider()

  "Select Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) ).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForCorrectReturn
      .set(SelectPage, Select.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute
        )

        val view = application.injector.instanceOf[SelectView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Select.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) )
      .overrides(
        bind[NavigatorForCorrectReturn
      ].toInstance(new FakeNavigatorForCorrectReturn (onwardRoute)
      ),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn
      ) ).build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selectRoute
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, selectRoute
        )
        .withFormUrlEncodedBody(("value", Select.values.head.toString))

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
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, selectRoute
            )
            .withFormUrlEncodedBody(("value", Select.values.head.toString))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on select"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}