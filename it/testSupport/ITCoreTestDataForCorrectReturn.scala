package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import org.scalatest.TryValues.convertTryToSuccessOrFailure

trait ITCoreTestDataForCorrectReturn {

  def sdilNumber: String

  def emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, Json.obj())


}
