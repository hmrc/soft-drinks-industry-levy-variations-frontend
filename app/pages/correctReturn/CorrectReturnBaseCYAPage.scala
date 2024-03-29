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
import pages.{QuestionPage, RequiredPage}
import utilities.UserTypeCheck

case object CorrectReturnBaseCYAPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "correctReturn"
  override def toString: String = "checkYourAnswers"
  
  override val url: Mode => String = _ => routes.CorrectReturnCYAController.onPageLoad.url

  override val previousPagesRequired: (UserAnswers, RetrievedSubscription) => List[RequiredPage] = (userAnswers, subscription) => {
    List(
      RequiredPage(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(!subscription.activity.smallProducer)),
      RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(
        !subscription.activity.smallProducer,
        userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true))
      ),
      RequiredPage(PackagedAsContractPackerPage),
      RequiredPage(HowManyPackagedAsContractPackerPage, additionalPreconditions = List(userAnswers.get(PackagedAsContractPackerPage).contains(true))),
      RequiredPage(ExemptionsForSmallProducersPage),
      RequiredPage(AddASmallProducerPage, additionalPreconditions = List(
        userAnswers.get(ExemptionsForSmallProducersPage).contains(true),
        userAnswers.smallProducerList.isEmpty
      )),
      RequiredPage(BroughtIntoUKPage),
      RequiredPage(HowManyBroughtIntoUKPage, additionalPreconditions = List(userAnswers.get(BroughtIntoUKPage).contains(true))),
      RequiredPage(BroughtIntoUkFromSmallProducersPage),
      RequiredPage(HowManyBroughtIntoUkFromSmallProducersPage, additionalPreconditions = List(userAnswers.get(BroughtIntoUkFromSmallProducersPage).contains(true))),
      RequiredPage(ClaimCreditsForExportsPage),
      RequiredPage(HowManyClaimCreditsForExportsPage, additionalPreconditions = List(userAnswers.get(ClaimCreditsForExportsPage).contains(true))),
      RequiredPage(ClaimCreditsForLostDamagedPage),
      RequiredPage(HowManyCreditsForLostDamagedPage, additionalPreconditions = List(userAnswers.get(ClaimCreditsForLostDamagedPage).contains(true))),
      RequiredPage(PackAtBusinessAddressPage, additionalPreconditions = List(
        UserTypeCheck.isNewPacker(userAnswers, subscription),
        subscription.productionSites.isEmpty
      )),
      RequiredPage(AskSecondaryWarehouseInReturnPage, additionalPreconditions = List(UserTypeCheck.isNewImporter(userAnswers, subscription)))
    )
  }
}
