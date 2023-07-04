package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json

trait ITCoreTestDataForCorrectReturn extends ITSharedCoreTestData  {

  def sdilNumber: String

  def emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, Json.obj())


}
