package testSupport

import models.{SelectChange, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.changeActivity.{ContractPackingPage, ImportsPage, OperatePackagingSiteOwnBrandsPage, PackagingSiteDetailsPage, PackAtBusinessAddressPage}
import play.api.libs.json.Json

trait ITCoreTestDataForChangeActivity {

  val userAnswersForChangeActivityPackAtBusinessAddressPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForChangeActivity.set(PackAtBusinessAddressPage, true).success.value
    val noSelected = emptyUserAnswersForChangeActivity.set(PackAtBusinessAddressPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForChangeActivityPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForChangeActivity.set(PackagingSiteDetailsPage, true).success.value
    val noSelected = emptyUserAnswersForChangeActivity.set(PackagingSiteDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForChangeActivityContractPackingPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForChangeActivity.set(ContractPackingPage, true).success.value
    val noSelected = emptyUserAnswersForChangeActivity.set(ContractPackingPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForChangeActivityImportsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForChangeActivity.set(ImportsPage, true).success.value
    val noSelected = emptyUserAnswersForChangeActivity.set(ImportsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForChangeActivityOperatePackagingSiteOwnBrandsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswersForChangeActivity.set(OperatePackagingSiteOwnBrandsPage, true).success.value
    val noSelected = emptyUserAnswersForChangeActivity.set(OperatePackagingSiteOwnBrandsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }
  def sdilNumber: String
  def emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.Changeactivity, Json.obj())

}
