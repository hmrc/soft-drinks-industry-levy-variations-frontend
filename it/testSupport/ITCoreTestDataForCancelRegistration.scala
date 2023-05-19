package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import org.scalatest.TryValues.convertTryToSuccessOrFailure

trait ITCoreTestDataForCancelRegistration {

  def sdilNumber: String
  def emptyUserAnswersForCancelRegistration = UserAnswers(sdilNumber, SelectChange.CancelRegistration, Json.obj())

}
