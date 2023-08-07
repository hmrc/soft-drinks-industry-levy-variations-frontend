package testSupport

import models.backend.Site
import models.{SelectChange, UserAnswers, Warehouse}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.correctReturn._
import play.api.libs.json.Json

trait ITCoreTestDataForCorrectReturn extends ITSharedCoreTestData  {

  def userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = Map(index -> Site(ukAddress, None, None, None)))
      .set(RemovePackagingSiteConfirmPage, true).success.value

    val noSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = Map(index -> Site(ukAddress, None, None, None)))
      .set(RemovePackagingSiteConfirmPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnSecondaryWarehouseDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(SecondaryWarehouseDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(SecondaryWarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  def userAnswersForCorrectReturnRemoveWarehouseDetailsPage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn
      .copy(warehouseList = Map(index -> Warehouse(None, ukAddress)))
      .set(RemoveWarehouseDetailsPage, true).success.value

    val noSelected = emptyUserAnswersForCorrectReturn
      .copy(warehouseList = Map(index -> Warehouse(None, ukAddress)))
      .set(RemoveWarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnSmallProducerDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(SmallProducerDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(SmallProducerDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(PackagingSiteDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(PackagingSiteDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForCorrectReturnPackAtBusinessAddressPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(PackAtBusinessAddressPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(PackAtBusinessAddressPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnRemoveSmallProducerConfirmPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(RemoveSmallProducerConfirmPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(RemoveSmallProducerConfirmPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForCorrectReturnBroughtIntoUkFromSmallProducersPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(BroughtIntoUkFromSmallProducersPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(BroughtIntoUkFromSmallProducersPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnClaimCreditsForExportsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(ClaimCreditsForExportsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(ClaimCreditsForExportsPage, false).success.value

    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForCorrectReturnBroughtIntoUKPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(BroughtIntoUKPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(BroughtIntoUKPage, false).success.value

    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForCorrectReturnPackagedAsContractPackerPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(PackagedAsContractPackerPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(PackagedAsContractPackerPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForCorrectReturnOperatePackagingSiteOwnBrandsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(OperatePackagingSiteOwnBrandsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(OperatePackagingSiteOwnBrandsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnClaimCreditsForLostDamagedPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(ClaimCreditsForLostDamagedPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(ClaimCreditsForLostDamagedPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  def sdilNumber: String

  def emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, Json.obj(),
    packagingSiteList = packagingSitesFromSubscription,
    contactAddress = ukAddress)

}
