/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import models.{LitresInBands, SelectChange, UserAnswers}
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, Small, None => NoneProduced}
import org.scalatest.TryValues
import pages.changeActivity._

object ChangeActivityCYAGenerators extends TryValues {

  val ownBrandsLitresLowBand = 1000
  val ownBrandsLitresHighBand = 2000
  val contractLitresLowBand = 3000
  val contractLitresHighBand = 4000
  val importLitresLowBand = 5000
  val importLitresHighBand = 6000

  val sdilNumber: String = "XKSDIL000000022"
  val emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.Changeactivity)

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
