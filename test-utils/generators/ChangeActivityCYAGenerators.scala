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

  def makeKeyString(keyStrings: List[String]): String = keyStrings.filterNot(_.isEmpty).mkString(", ")

  case class UserAnswerOptions(
                                amountProducedTuple: (String, Option[AmountProduced]),
                                thirdPartyPackagingTuple: (String, Option[Boolean]),
                                ownBrandsTuple: (String, Option[Boolean]),
                                contractTuple: (String, Option[Boolean]),
                                importTuple: (String, Option[Boolean]))

//  TODO: Add packAtBusinessAddress to this and test valid answers
  val fullTestCaseOptions: List[UserAnswerOptions] = List(APAnswers.Large, APAnswers.Small, APAnswers.NoneProduced, APAnswers.Unanswered).map(apAnswer => {
    val ap = amountProducedValues(apAnswer)
    Answers.All.map(tppAnswer => {
      val tpp = thirdPartyPackagingValues(tppAnswer)
      Answers.All.map(obAnswer => {
        val ob = ownBrandsValues(obAnswer)
        Answers.All.map(contractAnswer => {
          val contract = contractValues(contractAnswer)
          Answers.All.map(importAnswer => {
            val importV = importValues(importAnswer)
            UserAnswerOptions(ap, tpp, ob, contract, importV)
          })
        })
      })
    })
  }).flatten.flatten.flatten.flatten

//  val testCaseOptions: List[UserAnswerOptions] = fullTestCaseOptions

  val testCaseOptions: List[UserAnswerOptions] = List(
//    TODO: Contract packing is always answered No here - add PackAtBusinessAddress questions
//    Large, TPP x 3, OB No, Contact No, Import x 2
    Answers.All.map(tpp => {
      Answers.Answered.map(imp => {
        UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(tpp), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(imp))
      })
    }).flatten,
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Large), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    Small, TPP x 2, OB x 2, Contract No, Import x 2
    Answers.Answered.map(tpp => {
      Answers.Answered.map(ob => {
        Answers.Answered.map(imp => {
          UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp))
        })
      })
    }).flatten.flatten,
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.Small), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    NoneProducer, TPP x 3, OB x 3, Contract No, Import x 2
    Answers.All.map(tpp => {
      Answers.All.map(ob => {
        Answers.Answered.map(imp => {
          UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(tpp), ownBrandsValues(ob), contractValues(Answers.No), importValues(imp))
        })
      })
    }).flatten.flatten
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Yes), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.No), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.Yes), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.No), contractValues(Answers.No), importValues(Answers.No)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.Yes)),
//    UserAnswerOptions(amountProducedValues(APAnswers.NoneProduced), thirdPartyPackagingValues(Answers.Unanswered), ownBrandsValues(Answers.Unanswered), contractValues(Answers.No), importValues(Answers.No)),
  ).flatten

  def getKeyStringFromUserAnswerOptions(userAnswerOptions: UserAnswerOptions): String = {
    val keyStrings: List[String] = List(
      userAnswerOptions.amountProducedTuple._1,
      userAnswerOptions.thirdPartyPackagingTuple._1,
      userAnswerOptions.ownBrandsTuple._1,
      userAnswerOptions.contractTuple._1,
      userAnswerOptions.importTuple._1
    )
    makeKeyString(keyStrings)
  }

  def getUserAnswersFromUserAnswerOptions(userAnswerOptions: UserAnswerOptions): UserAnswers = {
    getUserAnswers(
      userAnswerOptions.amountProducedTuple._2,
      userAnswerOptions.thirdPartyPackagingTuple._2,
      userAnswerOptions.ownBrandsTuple._2,
      userAnswerOptions.contractTuple._2,
      userAnswerOptions.importTuple._2
    )
  }

}
