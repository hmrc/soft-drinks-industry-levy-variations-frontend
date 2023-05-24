package controllers.$packageName$

import base.SpecBase
import forms.$packageName$.$className$FormProvider
import models.NormalMode
import models.$packageName$.$className$
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.$packageName$.$className$Page
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.$packageName$.$className$View

import scala.concurrent.Future
import org.jsoup.Jsoup

class $className$ControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new $className$FormProvider()
  val form = formProvider()

  lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(NormalMode).url

  val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.copy(
    data = Json.obj(
      "$packageName$" -> Json.obj(
        $className$Page.toString -> Json.obj(
          "$field1Name$" -> "value 1",
          "$field2Name$" -> "value 2"
        )
      )
    )
  )

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$)).build()

      running(application) {
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill($className$("value 1", "value 2")), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {


      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$))
          .overrides(
            bind[NavigatorFor$packageName;format="cap"$].toInstance(new FakeNavigatorFor$packageName;format="cap"$(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$)).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recoveryCall.url
      }
    }

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;
        format = "decap" $Route
        )
        .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - soft-drinks-industry-levy - GOV.UK"
      }
    }
  }
}