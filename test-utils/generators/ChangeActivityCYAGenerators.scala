package generators

import base.SpecBase
import models.{LitresInBands, UserAnswers}
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, Small, None => NoneProduced}
import pages.changeActivity._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.summary.changeActivity.AmountProducedSummary
import views.summary.changeActivity._

object ChangeActivityCYAGenerators extends SpecBase {

  val ownBrandsLitresLowBand = 1000
  val ownBrandsLitresHighBand = 2000
  val contractLitresLowBand = 3000
  val contractLitresHighBand = 4000
  val importLitresLowBand = 5000
  val importLitresHighBand = 6000

  //    TODO: Refactor this if possible
  def getUserAnswers(
                      amountProduced: Option[AmountProduced] = None,
                      thirdPartyPackaging: Option[Boolean] = None,
                      ownBrands: Option[Boolean] = None,
                      contract: Option[Boolean] = None,
                      imports: Option[Boolean] = None
                    ): UserAnswers = {
    val initialUserAnswers = emptyUserAnswersForChangeActivity
    val userAnswersWithAmountProduced = amountProduced match {
      case Some(Large) => initialUserAnswers.set(AmountProducedPage, Large).success.value
      case Some(Small) => initialUserAnswers.set(AmountProducedPage, Small).success.value
      case Some(NoneProduced) => initialUserAnswers.set(AmountProducedPage, NoneProduced).success.value
      case None => initialUserAnswers
    }
    val userAnswersWithThirdPartyPackaging = userAnswersWithAmountProduced
    val userAnswersWithOwnBrands = ownBrands match {
      case Some(true) => userAnswersWithThirdPartyPackaging
        .set(OperatePackagingSiteOwnBrandsPage, true).success.value
        .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(ownBrandsLitresLowBand, ownBrandsLitresHighBand)).success.value
      case Some(false) => userAnswersWithThirdPartyPackaging.set(OperatePackagingSiteOwnBrandsPage, false).success.value
      case None => userAnswersWithThirdPartyPackaging
    }
    val userAnswersWithContract = contract match {
      case Some(true) => userAnswersWithOwnBrands
        .set(ContractPackingPage, true).success.value
        .set(HowManyContractPackingPage, LitresInBands(contractLitresLowBand, contractLitresHighBand)).success.value
      case Some(false) => userAnswersWithOwnBrands.set(ContractPackingPage, false).success.value
      case None => userAnswersWithOwnBrands
    }
    val userAnswersWithImport = imports match {
      case Some(true) => userAnswersWithContract
        .set(ImportsPage, true).success.value
        .set(HowManyImportsPage, LitresInBands(importLitresLowBand, importLitresHighBand)).success.value
      case Some(false) => userAnswersWithContract.set(ImportsPage, false).success.value
      case None => userAnswersWithContract
    }
    userAnswersWithImport
  }

  def getAmountProducedSection(userAnswers: UserAnswers): Seq[(String, SummaryList)] = Seq(
    "changeActivity.checkYourAnswers.amountProducedSection" -> SummaryList(Seq(AmountProducedSummary.row(userAnswers)).flatten)
  )

  //      TODO: Don't think 3rd party packagers is implemented
  def getThirdPartyPackagersSection(userAnswers: UserAnswers): Seq[(String, SummaryList)] = Seq.empty

  def getOperatePackingSiteOwnBrandsSection(userAnswers: UserAnswers): Seq[(String, SummaryList)] = Seq(
    "changeActivity.checkYourAnswers.operatePackingSiteOwnBrandsSection" ->
      OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = false)
  )

  def getContractPackingSection(userAnswers: UserAnswers): Seq[(String, SummaryList)] = Seq(
    "changeActivity.checkYourAnswers.contractPackingSection" ->
      ContractPackingSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = false)
  )

  def getImportsSection(userAnswers: UserAnswers): Seq[(String, SummaryList)] = Seq(
    "changeActivity.checkYourAnswers.importsSection" ->
      ImportsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = false)
  )

  val amountProducedValues: Map[String, Option[AmountProduced]] = Map(
    "amount produced large" -> Some(Large),
    "amount produced small" -> Some(Small),
    "amount produced none" -> Some(NoneProduced),
    "" -> None
  )

  val thirdPartyPackagingValues: Map[String, Option[Boolean]] = Map("" -> None)
  val ownBrandsValues: Map[String, Option[Boolean]] = Map("producing own brands" -> Some(true), "not producing own brands" -> Some(false), "" -> None)
  val contractValues: Map[String, Option[Boolean]] = Map("contract packing" -> Some(true), "not contract packing" -> Some(false), "" -> None)
  val importValues: Map[String, Option[Boolean]] = Map("importing" -> Some(true), "not importing" -> Some(false), "" -> None)

}
