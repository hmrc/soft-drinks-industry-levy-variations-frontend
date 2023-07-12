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

object ChangeActivityCYAGenerators {

  val ownBrandsLitresLowBand = 1000
  val ownBrandsLitresHighBand = 2000
  val contractLitresLowBand = 3000
  val contractLitresHighBand = 4000
  val importLitresLowBand = 5000
  val importLitresHighBand = 6000

  val sdilNumber: String = "XKSDIL000000022"
  val emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.ChangeActivity)

  case class ChangeActivityCYAUserAnswers(userAnswers: UserAnswers) extends TryValues {
    def withAmountProduced(amountProduced: Option[AmountProduced] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithAmountProduced = amountProduced match {
        case Some(Large) => userAnswers.set(AmountProducedPage, Large).success.value
        case Some(Small) => userAnswers.set(AmountProducedPage, Small).success.value
        case Some(NoneProduced) => userAnswers.set(AmountProducedPage, NoneProduced).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithAmountProduced)
    }

    def withThirdPartyPackaging(thirdPartyPackaging: Option[Boolean] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithThirdPartyPackaging = thirdPartyPackaging match {
        case Some(true) => userAnswers.set(ThirdPartyPackagersPage, true).success.value
        case Some(false) => userAnswers.set(ThirdPartyPackagersPage, false).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithThirdPartyPackaging)
    }

    def withOwnBrands(ownBrands: Option[Boolean] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithOwnBrands = ownBrands match {
        case Some(true) => userAnswers
          .set(OperatePackagingSiteOwnBrandsPage, true).success.value
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(ownBrandsLitresLowBand, ownBrandsLitresHighBand)).success.value
        case Some(false) => userAnswers.set(OperatePackagingSiteOwnBrandsPage, false).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithOwnBrands)
    }

    def withContract(contract: Option[Boolean] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithContract = contract match {
        case Some(true) => userAnswers
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(contractLitresLowBand, contractLitresHighBand)).success.value
        case Some(false) => userAnswers.set(ContractPackingPage, false).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithContract)
    }

    def withImports(imports: Option[Boolean] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithImport = imports match {
        case Some(true) => userAnswers
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(importLitresLowBand, importLitresHighBand)).success.value
        case Some(false) => userAnswers.set(ImportsPage, false).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithImport)
    }
  }

  def getUserAnswers(
                      amountProduced: Option[AmountProduced] = None,
                      thirdPartyPackaging: Option[Boolean] = None,
                      ownBrands: Option[Boolean] = None,
                      contract: Option[Boolean] = None,
                      imports: Option[Boolean] = None
                    ): UserAnswers = {
    ChangeActivityCYAUserAnswers(emptyUserAnswersForChangeActivity)
      .withAmountProduced(amountProduced)
      .withThirdPartyPackaging(thirdPartyPackaging)
      .withOwnBrands(ownBrands)
      .withContract(contract)
      .withImports(imports)
      .userAnswers
  }

  val amountProducedValues: Map[String, Option[AmountProduced]] = Map(
    "amount produced large" -> Some(Large),
    "amount produced small" -> Some(Small),
    "amount produced none" -> Some(NoneProduced),
    "" -> None
  )

  val thirdPartyPackagingValues: Map[String, Option[Boolean]] = Map("using third party packagers" -> Some(true), "not using third party packagers" -> Some(false), "" -> None)
  val ownBrandsValues: Map[String, Option[Boolean]] = Map("producing own brands" -> Some(true), "not producing own brands" -> Some(false), "" -> None)
  val contractValues: Map[String, Option[Boolean]] = Map("contract packing" -> Some(true), "not contract packing" -> Some(false), "" -> None)
  val importValues: Map[String, Option[Boolean]] = Map("importing" -> Some(true), "not importing" -> Some(false), "" -> None)

}
