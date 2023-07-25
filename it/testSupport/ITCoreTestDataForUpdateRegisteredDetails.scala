package testSupport

import models.backend.Site
import models.updateRegisteredDetails.UpdateContactDetails
import models.{SelectChange, UserAnswers, Warehouse}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.updateRegisteredDetails.{PackagingSiteDetailsPage, PackingSiteDetailsRemovePage, RemoveWarehouseDetailsPage, WarehouseDetailsPage}
import play.api.libs.json.Json

trait ITCoreTestDataForUpdateRegisteredDetails extends ITSharedCoreTestData {

  def userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForUpdateRegisteredDetails
      .copy(warehouseList = Map(index -> Warehouse(None, ukAddress)))
      .set(RemoveWarehouseDetailsPage, true).success.value

    val noSelected = emptyUserAnswersForUpdateRegisteredDetails
      .copy(warehouseList = Map(index -> Warehouse(None, ukAddress)))
      .set(RemoveWarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  def userAnswersForUpdateRegisteredDetailsPackingSiteDetailsRemovePage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForUpdateRegisteredDetails
      .copy(packagingSiteList = Map(index -> Site(ukAddress, None, None, None)))
      .set(PackingSiteDetailsRemovePage, true).success.value

    val noSelected = emptyUserAnswersForUpdateRegisteredDetails
      .copy(packagingSiteList = Map(index -> Site(ukAddress, None, None, None)))
      .set(PackingSiteDetailsRemovePage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForUpdateRegisteredDetailsWarehouseDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForUpdateRegisteredDetails.set(WarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForUpdateRegisteredDetailsPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForUpdateRegisteredDetails.set(PackagingSiteDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForUpdateRegisteredDetails.set(PackagingSiteDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val updateContactDetails: UpdateContactDetails = UpdateContactDetails("Full Name", "job position", "012345678901", "email@test.com")
  val updateContactDetailsDiff: UpdateContactDetails = UpdateContactDetails("New Name", "new job position", "012345678902", "email1@test.com")

  def sdilNumber: String

  def emptyUserAnswersForUpdateRegisteredDetails = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredDetails, Json.obj(), contactAddress = ukAddress)

}
