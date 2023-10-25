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

package navigation

import controllers.correctReturn.routes
import models.{CheckMode, Mode, NormalMode, RetrievedSubscription, SdilReturn, UserAnswers}
import pages._
import pages.correctReturn._
import play.api.Logger
import play.api.mvc.Call
import utilities.UserTypeCheck

import javax.inject.{Inject, Singleton}

@Singleton
class NavigatorForCorrectReturn @Inject()() extends Navigator {

  val logger: Logger = Logger(this.getClass)

  private def navigationForBroughtIntoUkFromSmallProducers(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = BroughtIntoUkFromSmallProducersPage).contains(true)) {
      routes.HowManyBroughtIntoUkFromSmallProducersController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ClaimCreditsForExportsController.onPageLoad(mode)
    }
  }
  private def navigationForClaimCreditsForExports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ClaimCreditsForExportsPage).contains(true)) {
      routes.HowManyClaimCreditsForExportsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ClaimCreditsForLostDamagedController.onPageLoad(mode)
    }
  }

  private def navigationForBroughtIntoUK(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = BroughtIntoUKPage).contains(true)) {
      routes.HowManyBroughtIntoUKController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUkFromSmallProducersController.onPageLoad(mode)
    }
  }

  private def navigationForExemptionsForSmallProducers(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ExemptionsForSmallProducersPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(mode)
    } else if(mode == CheckMode){
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUKController.onPageLoad(mode)
    }
  }


  private def navigationForPackagedAsContractPacker(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = PackagedAsContractPackerPage).contains(true)) {
      routes.HowManyPackagedAsContractPackerController.onPageLoad(mode)
    } else if(mode == CheckMode){
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.ExemptionsForSmallProducersController.onPageLoad(mode)
    }
  }

  private def navigationForOperatePackagingSiteOwnBrands(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSiteOwnBrandsPage).contains(true)) {
      routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.PackagedAsContractPackerController.onPageLoad(mode)
    }
  }

  private def navigationForAddASmallProducer(mode: Mode): Call = {
    routes.SmallProducerDetailsController.onPageLoad(mode)
  }

  private def navigationForSmallProducerDetails(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = SmallProducerDetailsPage).contains(true)) {
      routes.AddASmallProducerController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CorrectReturnCYAController.onPageLoad
    } else {
      routes.BroughtIntoUKController.onPageLoad(mode)
    }
  }

  private def navigationForRemoveSmallProducerConfirm(userAnswers: UserAnswers, mode: Mode):Call = {
    if (userAnswers.smallProducerList.isEmpty) {
      routes.ExemptionsForSmallProducersController.onPageLoad(mode)
    } else {
      routes.SmallProducerDetailsController.onPageLoad(mode)
    }
  }

//  private def navigationForCreditsForLostDamaged(userAnswers: UserAnswers,
//                                                 mode: Mode,
//                                                 subscriptionOpt: Option[RetrievedSubscription]) = {
//
//    if (userAnswers.get(page = ClaimCreditsForLostDamagedPage).contains(true)) {
//      routes.HowManyCreditsForLostDamagedController.onPageLoad(NormalMode)
//    } else {
//      packerImporterPageNavigation(userAnswers, subscriptionOpt)
//    }
//  }
//
//  private def navigationForHowManyCreditsForLostDamaged(userAnswers: UserAnswers,
//                                                        mode: Mode,
//                                                        subscriptionOpt: Option[RetrievedSubscription]) = {
//    packerImporterPageNavigation(userAnswers, subscriptionOpt)
//  }
//
//  private def packerImporterPageNavigation(userAnswers: UserAnswers,
//                                           subscriptionOpt: Option[RetrievedSubscription]) = {
//
//    val sdilReturn = SdilReturn.apply(userAnswers)
//    (sdilReturn, subscriptionOpt) match {
//      case (sdilReturn, Some(subscription)) =>
//        if (UserTypeCheck.isNewImporter(sdilReturn, subscription) || UserTypeCheck.isNewPacker(sdilReturn, subscription)) {
//          routes.ReturnChangeRegistrationController.onPageLoad()
//        } else {
//          routes.CorrectReturnCYAController.onPageLoad
//        }
//      case (_, Some(subscription)) =>
//        logger.warn(s"SDIL return not provided for ${subscription.sdilRef}")
//        controllers.routes.JourneyRecoveryController.onPageLoad()
//      case _ =>
//        logger.warn("SDIL return or subscription not provided for current unknown user")
//        controllers.routes.JourneyRecoveryController.onPageLoad()
//    }
//  }

  override val normalRoutes: Page => UserAnswers => Call = {
    case SecondaryWarehouseDetailsPage => _ => defaultCall
    case AskSecondaryWarehouseInReturnPage => _ => defaultCall
    case PackagingSiteDetailsPage => _ => defaultCall
    case ReturnChangeRegistrationPage => _ => defaultCall
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, NormalMode)
    case HowManyBroughtIntoUKPage => _ => routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, NormalMode)
    case HowManyBroughtIntoUkFromSmallProducersPage => _ => routes.ClaimCreditsForExportsController.onPageLoad(NormalMode)
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, NormalMode)
    case HowManyClaimCreditsForExportsPage => _ => routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode)
    case ExemptionsForSmallProducersPage => userAnswers => navigationForExemptionsForSmallProducers(userAnswers, NormalMode)
    case RemoveWarehouseDetailsPage => userAnswers => defaultCall
    case CorrectionReasonPage => _ => routes.RepaymentMethodController.onPageLoad(NormalMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, NormalMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => routes.PackagedAsContractPackerController.onPageLoad(NormalMode)
//    case ClaimCreditsForLostDamagedPage => userAnswers => navigationForCreditsForLostDamaged(userAnswers, NormalMode)
//    case HowManyCreditsForLostDamagedPage => userAnswers => navigationForHowManyCreditsForLostDamaged(userAnswers, NormalMode)
    case RepaymentMethodPage => userAnswers => routes.CorrectReturnCheckChangesCYAController.onPageLoad
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, NormalMode)
    case HowManyPackagedAsContractPackerPage => _ => routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(NormalMode)
    case SmallProducerDetailsPage => userAnswers => navigationForSmallProducerDetails(userAnswers, NormalMode)
    case RemoveSmallProducerConfirmPage => userAnswers => navigationForRemoveSmallProducerConfirm(userAnswers, NormalMode)
    case _ => _ => defaultCall
  }

  override val checkRouteMap: Page => UserAnswers => Call = {
    case BroughtIntoUKPage => userAnswers => navigationForBroughtIntoUK(userAnswers, CheckMode)
    case HowManyBroughtIntoUKPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case BroughtIntoUkFromSmallProducersPage => userAnswers => navigationForBroughtIntoUkFromSmallProducers(userAnswers, CheckMode)
    case HowManyBroughtIntoUkFromSmallProducersPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case ClaimCreditsForExportsPage => userAnswers => navigationForClaimCreditsForExports(userAnswers, CheckMode)
    case HowManyClaimCreditsForExportsPage => _ => routes.CorrectReturnCYAController.onPageLoad
    case ExemptionsForSmallProducersPage => _ =>  routes.CorrectReturnCYAController.onPageLoad
    case PackagedAsContractPackerPage => userAnswers => navigationForPackagedAsContractPacker(userAnswers, CheckMode)
    case OperatePackagingSiteOwnBrandsPage => userAnswers => navigationForOperatePackagingSiteOwnBrands(userAnswers, CheckMode)
    case HowManyOperatePackagingSiteOwnBrandsPage => userAnswers => routes.CorrectReturnCYAController.onPageLoad
//    case ClaimCreditsForLostDamagedPage => userAnswers => navigationForCreditsForLostDamaged(userAnswers, CheckMode)
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(CheckMode)
    case SmallProducerDetailsPage => userAnswers => navigationForSmallProducerDetails(userAnswers, CheckMode)
    case RemoveSmallProducerConfirmPage => userAnswers => navigationForRemoveSmallProducerConfirm(userAnswers, CheckMode)
    case RepaymentMethodPage => userAnswers => routes.CorrectReturnCheckChangesCYAController.onPageLoad
    case _ => _ => routes.CorrectReturnCYAController.onPageLoad
  }

  override val editRouteMap: Page => UserAnswers => Call = {
    case AddASmallProducerPage => _ => navigationForAddASmallProducer(NormalMode)
    case _ => _ => defaultCall
  }
}
