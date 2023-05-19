package testSupport

import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json

trait ITCoreTestDataForUpdateRegisteredDetails {

  def sdilNumber: String

  def emptyUserAnswersForUpdateRegisteredAccount = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredAccount, Json.obj())


}
