package controllers.$packageName$

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$packageName$.$className$View
class $className$ControllerSpec extends SpecBase {

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(emptyUserAnswersFor$packageName;format="cap"$)(request, messages(application)).toString
      }
    }
  }
}
