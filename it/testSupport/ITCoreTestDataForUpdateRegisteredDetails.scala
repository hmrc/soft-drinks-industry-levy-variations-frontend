package testSupport

import models.updateRegisteredDetails.UpdateContactDetails
import models.{SelectChange, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.updateRegisteredDetails.WarehouseDetailsPage
import play.api.libs.json.Json

trait ITCoreTestDataForUpdateRegisteredDetails {

  val userAnswersForUpdateRegisteredDetailsWarehouseDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val updateContactDetails: UpdateContactDetails = UpdateContactDetails("Full Name", "job position", "012345678901", "email@test.com")
  val updateContactDetailsDiff: UpdateContactDetails = UpdateContactDetails("New Name", "new job position", "012345678902", "email1@test.com")

  def sdilNumber: String

  def emptyUserAnswersForUpdateRegisteredDetails = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredAccount, Json.obj())

}
