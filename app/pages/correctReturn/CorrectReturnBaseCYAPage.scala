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

package pages.correctReturn

import controllers.correctReturn.routes
import play.api.libs.json.JsPath
import models.{Mode, UserAnswers}
import models.backend.RetrievedSubscription
import pages.{Page, QuestionPage, RequiredPage}
import utilities.UserTypeCheck

case object CorrectReturnBaseCYAPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "correctReturn"
  override def toString: String = "checkYourAnswers"
  
  override val url: Mode => String = _ => routes.CorrectReturnCYAController.onPageLoad.url

  //    private[controllers] def smallProducerCheck(subscription: RetrievedSubscription): List[CorrectReturnRequiredPage[_, _, _]] = {
  //    if (subscription.activity.smallProducer) {
  //      List.empty
  //    } else {
  //      List(CorrectReturnRequiredPage(OperatePackagingSiteOwnBrandsPage, None)(implicitly[Reads[Boolean]]),
  //        CorrectReturnRequiredPage(HowManyOperatePackagingSiteOwnBrandsPage,
  //          Some(CorrectReturnPreviousPage(OperatePackagingSiteOwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))
  //    }
  //  }
  private def smallProducerRequiredPages(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[RequiredPage] = {
    List(
      RequiredPage(OperatePackagingSiteOwnBrandsPage),
      RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true)))
    )
  }

  //    private[controllers] def addASmallProducerReturnChange(userAnswers: UserAnswers): List[CorrectReturnRequiredPage[_, _, _]] = {
  //    if (userAnswers.smallProducerList.isEmpty) {
  //      List(CorrectReturnRequiredPage(AddASmallProducerPage,
  //        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]))
  //    } else {
  //      List.empty
  //    }
  //  }
  private def addASmallProducerRequiredPages(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[RequiredPage] = {
    List(RequiredPage(AddASmallProducerPage, additionalPreconditions = List(
      userAnswers.get(ExemptionsForSmallProducersPage).contains(true),
      userAnswers.smallProducerList.isEmpty
    )))
  }

  //  private[controllers] def packingListReturnChange(
  //                                                    userAnswers: UserAnswers,
  //                                                    subscription: RetrievedSubscription
  //                                                  ): List[CorrectReturnRequiredPage[_, _, _]] = {
  //    if (UserTypeCheck.isNewPacker(userAnswers, subscription) && subscription.productionSites.isEmpty) {
  //      List(CorrectReturnRequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]))
  //    } else {
  //      List.empty
  //    }
  //  }
  private def packagingRequiredPages(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[RequiredPage] = {
    List(RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
      UserTypeCheck.isNewImporter(userAnswers, subscription),
      subscription.productionSites.isEmpty
    )))
  }

  //  private[controllers] def warehouseListReturnChange(
  //                                                      userAnswers: UserAnswers,
  //                                                      subscription: RetrievedSubscription
  //                                                    ): List[CorrectReturnRequiredPage[_, _, _]] = {
  //    if (UserTypeCheck.isNewImporter(userAnswers, subscription)) {
  //      List(CorrectReturnRequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))
  //    } else {
  //      List.empty
  //    }
  //  }
  private def warehouseRequiredPages(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[RequiredPage] = {
    List(RequiredPage(AskSecondaryWarehouseInReturnPage, additionalPreconditions = List(UserTypeCheck.isNewImporter(userAnswers, subscription))))
  }

  //  TODO: IMPLEMENT THIS
  override val previousPagesRequired: (UserAnswers, RetrievedSubscription) => List[RequiredPage] = (userAnswers, subscription) => {
    //    val firstPartOfRestOfJourney = List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
    //        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    //      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]])
    //    )
    val firstPartOfJourney = List(
      RequiredPage(PackagedAsContractPackerPage),
      RequiredPage(HowManyPackagedAsContractPackerPage, additionalPreconditions = List(userAnswers.get(PackagedAsContractPackerPage).contains(true))),
      RequiredPage(ExemptionsForSmallProducersPage)
    )
    //    val secondPartOfRestOfJourney = List(CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyBroughtIntoUKPage,
    //        Some(CorrectReturnPreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    //      CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyBroughtIntoUkFromSmallProducersPage,
    //        Some(CorrectReturnPreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    //      CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyClaimCreditsForExportsPage,
    //        Some(CorrectReturnPreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    //      CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyCreditsForLostDamagedPage,
    //        Some(CorrectReturnPreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
    //    )
    val secondPartOfJourney = List(
      RequiredPage(BroughtIntoUKPage),
      RequiredPage(HowManyBroughtIntoUKPage, additionalPreconditions = List(userAnswers.get(BroughtIntoUKPage).contains(true))),
      RequiredPage(BroughtIntoUkFromSmallProducersPage),
      RequiredPage(HowManyBroughtIntoUkFromSmallProducersPage, additionalPreconditions = List(userAnswers.get(BroughtIntoUkFromSmallProducersPage).contains(true))),
      RequiredPage(ClaimCreditsForExportsPage),
      RequiredPage(HowManyClaimCreditsForExportsPage, additionalPreconditions = List(userAnswers.get(ClaimCreditsForExportsPage).contains(true))),
      RequiredPage(ClaimCreditsForLostDamagedPage),
      RequiredPage(HowManyCreditsForLostDamagedPage, additionalPreconditions = List(userAnswers.get(ClaimCreditsForLostDamagedPage).contains(true))),
    )
    smallProducerRequiredPages(userAnswers, subscription) ++
      firstPartOfJourney ++
      addASmallProducerRequiredPages(userAnswers, subscription) ++
      secondPartOfJourney ++
      packagingRequiredPages(userAnswers, subscription) ++
      warehouseRequiredPages(userAnswers, subscription)
  }
}
