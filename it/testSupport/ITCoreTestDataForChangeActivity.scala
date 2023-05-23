package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import org.scalatest.TryValues.convertTryToSuccessOrFailure

trait ITCoreTestDataForChangeActivity {
  def sdilNumber: String
  def emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.Changeactivity, Json.obj())


}
