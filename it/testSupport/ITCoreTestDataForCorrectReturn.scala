package testSupport

import models.{SelectChange, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.correctReturn._
import play.api.libs.json.Json

trait ITCoreTestDataForCorrectReturn extends ITSharedCoreTestData  {

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

  def sdilNumber: String

  def emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, Json.obj(), contactAddress = ukAddress)


}
