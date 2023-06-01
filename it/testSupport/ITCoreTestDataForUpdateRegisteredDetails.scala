package testSupport

import models.updateRegisteredDetails.UpdateContactDetails
import models.{SelectChange, UserAnswers}
import play.api.libs.json.Json
import org.scalatest.TryValues.convertTryToSuccessOrFailure

trait ITCoreTestDataForUpdateRegisteredDetails {

  val updateContactDetails: UpdateContactDetails = UpdateContactDetails("Full Name", "job position", "012345678901", "email@test.com")
  val updateContactDetailsDiff: UpdateContactDetails = UpdateContactDetails("New Name", "new job position", "012345678902", "email1@test.com")

  def sdilNumber: String

  def emptyUserAnswersForUpdateRegisteredDetails = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredAccount, Json.obj())


}
