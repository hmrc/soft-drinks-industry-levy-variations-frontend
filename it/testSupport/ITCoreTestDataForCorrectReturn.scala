package testSupport

import models.backend.Site
import models.{ LitresInBands, ReturnPeriod, SelectChange, UserAnswers }
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.correctReturn._
import play.api.libs.json.Json

trait ITCoreTestDataForCorrectReturn extends ITSharedCoreTestData {

  def userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(
    index: String,
    lastPackagingSite: Boolean = true
  ): Map[String, UserAnswers] = {
    val packagingSites: Map[String, Site] =
      if (lastPackagingSite) Map(index -> Site(ukAddress, None, None, None))
      else {
        Map(index -> Site(ukAddress, None, None, None)) ++ Map("notIndex" -> Site(ukAddress, None, None, None))
      }
    val yesSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = packagingSites)
      .set(RemovePackagingSiteConfirmPage, true)
      .success
      .value

    val noSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = packagingSites)
      .set(RemovePackagingSiteConfirmPage, false)
      .success
      .value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnSecondaryWarehouseDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(SecondaryWarehouseDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(SecondaryWarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  def userAnswersForCorrectReturnRemoveWarehouseDetailsPage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn
      .copy(warehouseList = Map(index -> Site(ukAddress)))
      .set(RemoveWarehouseDetailsPage, true)
      .success
      .value

    val noSelected = emptyUserAnswersForCorrectReturn
      .copy(warehouseList = Map(index -> Site(ukAddress)))
      .set(RemoveWarehouseDetailsPage, false)
      .success
      .value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnAskSecondaryWarehouseInReturnPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(AskSecondaryWarehouseInReturnPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(AskSecondaryWarehouseInReturnPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnSmallProducerDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(SmallProducerDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(SmallProducerDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForCorrectReturnPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = packagingSitesFromSubscription)
      .set(PackagingSiteDetailsPage, true)
      .success
      .value
    val noSelected = emptyUserAnswersForCorrectReturn
      .copy(packagingSiteList = packagingSitesFromSubscription)
      .set(PackagingSiteDetailsPage, false)
      .success
      .value
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

  val userAnswersForExceptionsForSmallProducersPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForCorrectReturn.set(ExemptionsForSmallProducersPage, true).success.value
    val noSelected = emptyUserAnswersForCorrectReturn.set(ExemptionsForSmallProducersPage, false).success.value

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

  def emptyUserAnswersForCorrectReturn = UserAnswers(
    sdilNumber,
    SelectChange.CorrectReturn,
    Json.obj(),
    contactAddress = ukAddress,
    correctReturnPeriod = Some(ReturnPeriod(2023, 0))
  )

  def completedUserAnswersForCorrectReturnNewPackerOrImporter: UserAnswers = UserAnswers(
    sdilNumber,
    SelectChange.CorrectReturn,
    Json.obj(),
    packagingSiteList = Map.empty,
    warehouseList = Map.empty,
    contactAddress = ukAddress,
    correctReturnPeriod = Some(ReturnPeriod(2023, 0))
  )
    .set(OperatePackagingSiteOwnBrandsPage, true)
    .success
    .value
    .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(32432, 34839))
    .success
    .value
    .set(PackagedAsContractPackerPage, true)
    .success
    .value
    .set(HowManyPackagedAsContractPackerPage, LitresInBands(20248, 2342))
    .success
    .value
    .set(ExemptionsForSmallProducersPage, false)
    .success
    .value
    .set(BroughtIntoUKPage, true)
    .success
    .value
    .set(HowManyBroughtIntoUKPage, LitresInBands(21312, 12312))
    .success
    .value
    .set(BroughtIntoUkFromSmallProducersPage, false)
    .success
    .value
    .set(ClaimCreditsForExportsPage, false)
    .success
    .value

}
