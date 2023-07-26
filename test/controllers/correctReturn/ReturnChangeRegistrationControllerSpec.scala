package controllers.correctReturn

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.correctReturn.ReturnChangeRegistrationView
import models.SelectChange.CorrectReturn
class ReturnChangeRegistrationControllerSpec extends SpecBase {

  lazy val returnChangeRegistrationRoute = routes.ReturnChangeRegistrationController.onPageLoad().url


  "ReturnChangeRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForCorrectReturn)).build()

      running(application) {
        val request = FakeRequest(GET, returnChangeRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnChangeRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(CorrectReturn, returnChangeRegistrationRoute)
    testNoUserAnswersError(returnChangeRegistrationRoute)
  }
}
