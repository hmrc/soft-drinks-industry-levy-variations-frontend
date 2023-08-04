package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json

trait ITCoreTestDataForCancelRegistration extends ITCoreTestDataForCorrectReturn {

  def sdilNumber: String
  def emptyUserAnswersForCancelRegistration = UserAnswers(sdilNumber, SelectChange.CancelRegistration, Json.obj(),
    packagingSiteList = packagingSitesFromSubscription,
    contactAddress = ukAddress)

}
