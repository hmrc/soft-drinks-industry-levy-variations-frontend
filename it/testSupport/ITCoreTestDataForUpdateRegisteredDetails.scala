package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import org.scalatest.TryValues.convertTryToSuccessOrFailure

trait ITCoreTestDataForUpdateRegisteredDetails {

  def sdilNumber: String

  def emptyUserAnswersForUpdateRegisteredDetails = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredAccount, Json.obj())


}