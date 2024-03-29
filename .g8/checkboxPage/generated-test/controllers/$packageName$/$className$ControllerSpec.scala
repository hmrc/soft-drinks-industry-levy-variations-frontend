package controllers.$packageName$

import base.SpecBase
import forms.$packageName$.$className$FormProvider
import models.NormalMode
import models.$packageName$.$className$
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import models.SelectChange.$packageName;format="cap"$
import pages.$packageName$.$className$Page
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.$packageName$.$className$View
import errors.SessionDatabaseInsertError
import org.jsoup.Jsoup
import scala.concurrent.Future
import utilities.GenericLogger
class $className$ControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val $className;format ="decap"$Route = routes.$className$Controller.onPageLoad(NormalMode).url

  val formProvider = new $className$FormProvider()
  val form = formProvider()

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;
      format = "cap" $
      ) ).build()

      running(application) {
        val request = FakeRequest(GET, $className;
        format = "decap" $Route
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersFor$packageName;
      format = "cap" $
      .set($className$Page, $className$.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, $className;
        format = "decap" $Route
        )

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill($className$.values.toSet), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;
      format = "cap" $
      ) )
      .overrides(
        bind[NavigatorFor$packageName;
      format = "cap" $
      ].toInstance(new FakeNavigatorFor$packageName;
      format = "cap" $ (onwardRoute)
      ),
      bind[SessionService].toInstance(mockSessionService)
      )
      .build()

      running(application) {
        val request =
          FakeRequest(POST, $className;
        format = "decap" $Route
        )
        .withFormUrlEncodedBody(("value[0]", $className$.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;
      format = "cap" $
      ) ).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;
        format = "decap" $Route
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    testInvalidJourneyType($packageName;format="cap"$, $className;format="decap"$Route)
    testRedirectToPostSubmissionIfRequired($packageName;format="cap"$, $className;format="decap"$Route)
    testNoUserAnswersError($className;format="decap"$Route)

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure($packageName;format="cap"$))).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;
        format = "decap" $Route
        )
        .withFormUrlEncodedBody(("value[0]", $className$.values.head.toString))

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
        applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$))
          .overrides(
            bind[NavigatorFor$packageName;format="cap"$].toInstance(new FakeNavigatorFor$packageName;format="cap"$(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          running(application) {
            val request = FakeRequest(POST, $className;
            format = "decap" $Route
            )
            .withFormUrlEncodedBody(("value[0]", $className$.values.head.toString))

            await(route(application, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustBe "ERROR"
                event.getMessage mustEqual "Failed to set value in session repository while attempting set on $className;format="
                decap"$"
            }.getOrElse(fail("No logging captured"))
          }
        }
      }
    }
  }
}
