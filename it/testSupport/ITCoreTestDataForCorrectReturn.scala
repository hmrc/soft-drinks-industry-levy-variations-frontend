package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json

trait ITCoreTestDataForCorrectReturn {

  def sdilNumber: String

  val emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, Json.obj())


}
