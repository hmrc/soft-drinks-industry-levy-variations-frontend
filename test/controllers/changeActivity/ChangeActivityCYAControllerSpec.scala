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

package controllers.changeActivity

import base.SpecBase
import controllers.changeActivity.routes._
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, None => NoneProduced, Small}
import models.{LitresInBands, UserAnswers}
import pages.changeActivity._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.changeActivity.AmountProducedSummary
import views.html.changeActivity.ChangeActivityCYAView
import views.summary.changeActivity.{ContractPackingSummary, ImportsSummary, OperatePackagingSiteOwnBrandsSummary}

class ChangeActivityCYAControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

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
          .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 2)).success.value
        case Some(false) => userAnswersWithThirdPartyPackaging.set(OperatePackagingSiteOwnBrandsPage, false).success.value
        case None => userAnswersWithThirdPartyPackaging
      }
      val userAnswersWithContract = contract match {
        case Some(true) => userAnswersWithOwnBrands
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3, 4)).success.value
        case Some(false) => userAnswersWithOwnBrands.set(ContractPackingPage, false).success.value
        case None => userAnswersWithOwnBrands
      }
      val userAnswersWithImport = imports match {
        case Some(true) => userAnswersWithContract
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(5, 6)).success.value
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

//    TODO: Perhaps only need a selection of these to show behaviour
    val userAnswersMap: Map[String, UserAnswers] =
      amountProducedValues.flatMap(amountProduced => {
        thirdPartyPackagingValues.flatMap(thirdPartyPackagingValue => {
          ownBrandsValues.flatMap(ownBrandsValue => {
            contractValues.flatMap(contractValue => {
              importValues.map(importValue => {
                List(amountProduced._1, thirdPartyPackagingValue._1, ownBrandsValue._1, contractValue._1, importValue._1).filterNot(_.isEmpty).mkString(", ") ->
                  getUserAnswers(amountProduced._2, thirdPartyPackagingValue._2, ownBrandsValue._2, contractValue._2, importValue._2)
              })
            })
          })
        })
      })

    userAnswersMap.foreach { case (key, userAnswers) =>
      s"must return OK and the correct view for a GET for user answers $key" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, ChangeActivityCYAController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChangeActivityCYAView]
          val list = getAmountProducedSection(userAnswers) ++
            getThirdPartyPackagersSection(userAnswers) ++
            getOperatePackingSiteOwnBrandsSection(userAnswers) ++
            getContractPackingSection(userAnswers) ++
            getImportsSection(userAnswers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            aSubscription.orgName,
            //          TODO: Need to fix return period
            "RETURN PERIOD",
            list,
            routes.ChangeActivityCYAController.onSubmit
          )(request, messages(application)).toString
        }
      }
    }

    testInvalidJourneyType(ChangeActivity, ChangeActivityCYAController.onPageLoad.url, false)
    testNoUserAnswersError(ChangeActivityCYAController.onPageLoad.url, false)
  }
}
