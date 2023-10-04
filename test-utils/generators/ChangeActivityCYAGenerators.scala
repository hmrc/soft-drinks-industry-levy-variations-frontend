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

import models.backend.UkAddress
import models.{LitresInBands, SelectChange, UserAnswers}
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, Small, enumerable, None => NoneProduced}
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
  lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")
  val emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.ChangeActivity, contactAddress = contactAddress)

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
        case Some(bool) => userAnswers.set(ThirdPartyPackagersPage, bool).success.value
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

    def withPackAtBusinessAddress(packAtBusinessAddress: Option[Boolean] = None): ChangeActivityCYAUserAnswers = {
      val userAnswersWithPackAtBusinessAddress = packAtBusinessAddress match {
        case Some(true) => userAnswers
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, false).success.value
        case Some(false) => userAnswers
          .set(PackAtBusinessAddressPage, false).success.value
          .set(PackagingSiteDetailsPage, false).success.value
        case None => userAnswers
      }
      ChangeActivityCYAUserAnswers(userAnswersWithPackAtBusinessAddress)
    }
  }

  def getUserAnswers(
                      amountProduced: Option[AmountProduced] = None,
                      thirdPartyPackaging: Option[Boolean] = None,
                      ownBrands: Option[Boolean] = None,
                      contract: Option[Boolean] = None,
                      imports: Option[Boolean] = None,
                      packAtBusinessAddress: Option[Boolean] = None
                    ): UserAnswers = {
    ChangeActivityCYAUserAnswers(emptyUserAnswersForChangeActivity)
      .withAmountProduced(amountProduced)
      .withThirdPartyPackaging(thirdPartyPackaging)
      .withOwnBrands(ownBrands)
      .withContract(contract)
      .withImports(imports)
      .withPackAtBusinessAddress(packAtBusinessAddress)
      .userAnswers
  }

  object Answers extends Enumeration {
    val Yes, No, Unanswered = Value
    val Answered = List(Yes, No)
    val All = List(Yes, No, Unanswered)
  }

  object APAnswers extends Enumeration {
    val Large, Small, NoneProduced, Unanswered = Value
  }

  val amountProducedValues: Map[APAnswers.Value, (String, Option[AmountProduced])] = Map(
    APAnswers.Large -> ("amount produced large", Some(Large)),
    APAnswers.Small -> ("amount produced small", Some(Small)),
    APAnswers.NoneProduced -> ("amount produced none", Some(NoneProduced)),
    APAnswers.Unanswered -> ("", None)
  )

  val thirdPartyPackagingValues: Map[Answers.Value, (String, Option[Boolean])] =
    Map(Answers.Yes -> ("using third party packagers", Some(true)), Answers.No -> ("not using third party packagers", Some(false)), Answers.Unanswered -> ("", None))
  val ownBrandsValues: Map[Answers.Value, (String, Option[Boolean])] =
    Map(Answers.Yes -> ("producing own brands", Some(true)), Answers.No -> ("not producing own brands", Some(false)), Answers.Unanswered -> ("", None))
  val contractValues: Map[Answers.Value, (String, Option[Boolean])] =
    Map(Answers.Yes -> ("contract packing", Some(true)), Answers.No -> ("not contract packing", Some(false)), Answers.Unanswered -> ("", None))
  val importValues: Map[Answers.Value, (String, Option[Boolean])] =
    Map(Answers.Yes -> ("importing", Some(true)), Answers.No -> ("not importing", Some(false)), Answers.Unanswered -> ("", None))
  val packAtBusinessAddressValues: Map[Answers.Value, (String, Option[Boolean])] =
    Map(Answers.Yes -> ("packing at business address", Some(true)), Answers.No -> ("not packing at business address", Some(false)), Answers.Unanswered -> ("", None))

  def makeKeyString(keyStrings: List[String]): String = keyStrings.filterNot(_.isEmpty).mkString(", ")

  case class UserAnswerOptions(
                                amountProducedTuple: (String, Option[AmountProduced]),
                                thirdPartyPackagingTuple: (String, Option[Boolean]),
                                ownBrandsTuple: (String, Option[Boolean]),
                                contractTuple: (String, Option[Boolean]),
                                importTuple: (String, Option[Boolean]),
                                packAtBusinessAddressTuple: (String, Option[Boolean]))

  val largeTestCaseOptions: List[UserAnswerOptions] = Answers.All.flatMap(tpp => {
    Answers.Answered.map(imp => {
      List(
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.Yes), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.Yes), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.No)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.No)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.No)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.No)),
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Unanswered))
      )
    })
  }).flatten

  val smallTestCaseOptions: List[UserAnswerOptions] = Answers.Answered.flatMap(tpp => {
    Answers.Answered.map(ob => {
      Answers.Answered.map(imp => {
        List(
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.No)),
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.No)),
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Unanswered))
        )
      })
    })
  }).flatten.flatten

  val noneTestCaseOptions: List[UserAnswerOptions] = Answers.All.flatMap(tpp => {
    Answers.All.map(ob => {
      Answers.Answered.map(imp => {
        List(
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.Yes), importValues(imp), packAtBusinessAddressValues(Answers.No)),
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Yes)),
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.No)),
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp), packAtBusinessAddressValues(Answers.Unanswered))
        )
      })
    })
  }).flatten.flatten

  val testCaseOptions: List[UserAnswerOptions] = largeTestCaseOptions ++ smallTestCaseOptions ++ noneTestCaseOptions

  def getKeyStringFromUserAnswerOptions(userAnswerOptions: UserAnswerOptions): String = {
    val keyStrings: List[String] = List(
      userAnswerOptions.amountProducedTuple._1,
      userAnswerOptions.thirdPartyPackagingTuple._1,
      userAnswerOptions.ownBrandsTuple._1,
      userAnswerOptions.contractTuple._1,
      userAnswerOptions.importTuple._1,
      userAnswerOptions.packAtBusinessAddressTuple._1
    )
    makeKeyString(keyStrings)
  }

  def getUserAnswersFromUserAnswerOptions(userAnswerOptions: UserAnswerOptions): UserAnswers = {
    getUserAnswers(
      userAnswerOptions.amountProducedTuple._2,
      userAnswerOptions.thirdPartyPackagingTuple._2,
      userAnswerOptions.ownBrandsTuple._2,
      userAnswerOptions.contractTuple._2,
      userAnswerOptions.importTuple._2,
      userAnswerOptions.packAtBusinessAddressTuple._2
    )
  }

}
