package controllers.updateRegisteredDetails

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.updateRegisteredDetails.BusinessAddressView
import models.SelectChange.UpdateRegisteredDetails
class BusinessAddressControllerSpec extends SpecBase {

  lazy val businessAddressRoute = routes.BusinessAddressController.onPageLoad().url


  "BusinessAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUpdateRegisteredDetails)).build()

      running(application) {
        val request = FakeRequest(GET, businessAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    testInvalidJourneyType(UpdateRegisteredDetails, businessAddressRoute)
    testNoUserAnswersError(businessAddressRoute)
  }
}
