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
import models.{Mode, UserAnswers}
import models.backend.RetrievedSubscription
import pages.{Page, RequiredPage}

case object CorrectReturnCheckChangesPage extends Page {

  def journeyType: String = "correctReturn"
  override def toString: String = "checkChanges"
  override val url: Mode => String = _ => routes.CorrectReturnCheckChangesCYAController.onPageLoad.url

  //  TODO: IMPLEMENT THIS
  override val previousPagesRequired: (UserAnswers, RetrievedSubscription) => List[RequiredPage] = (userAnswers, subscription) => {
//    private[controllers] def restOfJourney(
    //                                          smallProducerCheck: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
    //                                          addASmallProducerReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
    //                                          packingListReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
    //                                          warehouseListReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty
    //                                        ): List[CorrectReturnRequiredPage[_, _, _]] = {
    //    val firstPartOfRestOfJourney = List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
    //      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
    //        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
    //      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]])
    //    )
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
    //    smallProducerCheck ++
    //      firstPartOfRestOfJourney ++
    //      addASmallProducerReturnChange ++
    //      secondPartOfRestOfJourney ++
    //      packingListReturnChange ++
    //      warehouseListReturnChange
    //  }

//    private[controllers] def smallProducerCheck(subscription: RetrievedSubscription): List[CorrectReturnRequiredPage[_, _, _]] = {
    //    if (subscription.activity.smallProducer) {
    //      List.empty
    //    } else {
    //      List(CorrectReturnRequiredPage(OperatePackagingSiteOwnBrandsPage, None)(implicitly[Reads[Boolean]]),
    //        CorrectReturnRequiredPage(HowManyOperatePackagingSiteOwnBrandsPage,
    //          Some(CorrectReturnPreviousPage(OperatePackagingSiteOwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))
    //    }
    //  }

//    private[controllers] def addASmallProducerReturnChange(userAnswers: UserAnswers): List[CorrectReturnRequiredPage[_, _, _]] = {
    //    if (userAnswers.smallProducerList.isEmpty) {
    //      List(CorrectReturnRequiredPage(AddASmallProducerPage,
    //        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]))
    //    } else {
    //      List.empty
    //    }
    //  }
    //
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
    //
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
    List.empty
  }
}
