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

package controllers.actions

import models.backend.RetrievedSubscription
import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.{CheckMode, LitresInBands, UserAnswers}
import pages.correctReturn._
import pages.{Page, QuestionPage}
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.{GenericLogger, UserTypeCheck}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswersForCorrectReturn @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {

  def requireData(page: Page, userAnswers: UserAnswers, subscription: RetrievedSubscription)(action: => Future[Result]): Future[Result] = {
    page match {
      case CorrectReturnBaseCYAPage => checkYourAnswersRequiredData(userAnswers, subscription, action)
      case CorrectionReasonPage => correctionReasonRequiredData(userAnswers, subscription, action)
      case RepaymentMethodPage => repaymentMethodRequiredData(userAnswers, subscription, action)
      case CorrectReturnCheckChangesPage => checkChangesRequiredData(userAnswers, subscription, action)
      case _ => action
    }
  }

  private[controllers] def checkYourAnswersRequiredData(userAnswers: UserAnswers, subscription: RetrievedSubscription, action: => Future[Result]): Future[Result] = {
    val journey = mainRoute(userAnswers, subscription)
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(userAnswers, journey)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) if List(PackAtBusinessAddressPage, PackagingSiteDetailsPage).contains(page) =>
        genericLogger.logger.info(s"${userAnswers.id} now requires packaging sites")
        Future.successful(Redirect(PackAtBusinessAddressPage.url(CheckMode)))
      case Some(page) if page == SecondaryWarehouseDetailsPage =>
        genericLogger.logger.info(s"${userAnswers.id} now has option to add warehouse")
        Future.successful(Redirect(page.url(CheckMode)))
      case Some(page) => genericLogger.logger.warn(s"${userAnswers.id} has hit correct return base CYA and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def correctionReasonRequiredData(userAnswers: UserAnswers, subscription: RetrievedSubscription, action: => Future[Result]): Future[Result] = {
    val journey = correctionReasonJourney()
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(userAnswers, journey)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) => genericLogger.logger.warn(s"${userAnswers.id} has hit check changes CYA and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def repaymentMethodRequiredData(userAnswers: UserAnswers, subscription: RetrievedSubscription, action: => Future[Result]): Future[Result] = {
    val journey = repaymentMethodJourney()
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(userAnswers, journey)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) => genericLogger.logger.warn(s"${userAnswers.id} has hit repayment method and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def checkChangesRequiredData(userAnswers: UserAnswers, subscription: RetrievedSubscription, action: => Future[Result]): Future[Result] = {
    val journey = checkChangesJourney(userAnswers)
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(userAnswers, journey)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) => genericLogger.logger.warn(s"${userAnswers.id} has hit check changes CYA and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](
                                                                           userAnswers: UserAnswers,
                                                                           list: List[CorrectReturnRequiredPage[_, _, _]]
                                                                         ): List[CorrectReturnRequiredPage[_, _, _]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = userAnswers
        .get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnCorrectReturnPreviousPage.nonEmpty) match {
        case (false, true) =>
          val CorrectReturnPreviousPage: CorrectReturnPreviousPage[QuestionPage[B], B] = listItem.basedOnCorrectReturnPreviousPage
            .get.asInstanceOf[CorrectReturnPreviousPage[QuestionPage[B], B]]
          val CorrectReturnPreviousPageAnswer: Option[B] = userAnswers
            .get(CorrectReturnPreviousPage.page)(CorrectReturnPreviousPage.reads)
          !CorrectReturnPreviousPageAnswer.contains(CorrectReturnPreviousPage.CorrectReturnPreviousPageAnswerRequired)
        case (false, _) => false
        case _ => true
      }
    }
  }

  private[controllers] def mainRoute(userAnswers: UserAnswers, subscription: RetrievedSubscription): List[CorrectReturnRequiredPage[_, _, _]] = {
    restOfJourney(
      smallProducerCheck(subscription),
      addASmallProducerReturnChange(userAnswers),
      packingListReturnChange(userAnswers, subscription),
      warehouseListReturnChange(userAnswers, subscription)
    )
  }

  private[controllers] def smallProducerCheck(subscription: RetrievedSubscription): List[CorrectReturnRequiredPage[_, _, _]] = {
    if (subscription.activity.smallProducer) {
      List.empty
    } else {
      List(CorrectReturnRequiredPage(OperatePackagingSiteOwnBrandsPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(HowManyOperatePackagingSiteOwnBrandsPage,
          Some(CorrectReturnPreviousPage(OperatePackagingSiteOwnBrandsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]))
    }
  }

  private[controllers] def restOfJourney(
                                          smallProducerCheck: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
                                          addASmallProducerReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
                                          packingListReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty,
                                          warehouseListReturnChange: List[CorrectReturnRequiredPage[_, _, _]] = List.empty
                                        ): List[CorrectReturnRequiredPage[_, _, _]] = {
    val firstPartOfRestOfJourney = List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]])
    )
    val secondPartOfRestOfJourney = List(CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyBroughtIntoUKPage,
        Some(CorrectReturnPreviousPage(BroughtIntoUKPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(BroughtIntoUkFromSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyBroughtIntoUkFromSmallProducersPage,
        Some(CorrectReturnPreviousPage(BroughtIntoUkFromSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ClaimCreditsForExportsPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyClaimCreditsForExportsPage,
        Some(CorrectReturnPreviousPage(ClaimCreditsForExportsPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ClaimCreditsForLostDamagedPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyCreditsForLostDamagedPage,
        Some(CorrectReturnPreviousPage(ClaimCreditsForLostDamagedPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]])
    )
    smallProducerCheck ++
      firstPartOfRestOfJourney ++
      addASmallProducerReturnChange ++
      secondPartOfRestOfJourney ++
      packingListReturnChange ++
      warehouseListReturnChange
  }

  private[controllers] def correctionReasonJourney(): List[CorrectReturnRequiredPage[_, _, _]] = {
    List(CorrectReturnRequiredPage(CorrectReturnBaseCYAPage, None)(implicitly[Reads[Boolean]]))
  }

  private[controllers] def repaymentMethodJourney(): List[CorrectReturnRequiredPage[_, _, _]] = {
    List(CorrectReturnRequiredPage(CorrectReturnBaseCYAPage, None)(implicitly[Reads[Boolean]]))
  }

  private[controllers] def checkChangesJourney(userAnswers: UserAnswers): List[CorrectReturnRequiredPage[_, _, _]] = {
    val balanceRepaymentRequiredJourney = userAnswers.get(BalanceRepaymentRequired) match {
      case Some(true) => List(CorrectReturnRequiredPage(RepaymentMethodPage, None)(implicitly[Reads[RepaymentMethod]]))
      case _ => List.empty
    }
    List(
      CorrectReturnRequiredPage(CorrectReturnBaseCYAPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(CorrectionReasonPage, None)(implicitly[Reads[String]])
    ) ++ balanceRepaymentRequiredJourney
  }

  private[controllers] def addASmallProducerReturnChange(userAnswers: UserAnswers): List[CorrectReturnRequiredPage[_, _, _]] = {
    if (userAnswers.smallProducerList.isEmpty) {
      List(CorrectReturnRequiredPage(AddASmallProducerPage,
        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]))
    } else {
      List.empty
    }
  }

  private[controllers] def packingListReturnChange(
                                                    userAnswers: UserAnswers,
                                                    subscription: RetrievedSubscription
                                                  ): List[CorrectReturnRequiredPage[_, _, _]] = {
    if (UserTypeCheck.isNewPacker(userAnswers, subscription) && subscription.productionSites.isEmpty) {
      List(CorrectReturnRequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }

  private[controllers] def warehouseListReturnChange(
                                                      userAnswers: UserAnswers,
                                                      subscription: RetrievedSubscription
                                                    ): List[CorrectReturnRequiredPage[_, _, _]] = {
    if (UserTypeCheck.isNewImporter(userAnswers, subscription)) {
      List(CorrectReturnRequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }

}


case class CorrectReturnRequiredPage[+A >: QuestionPage[C], +B >: CorrectReturnPreviousPage[_, _], C]
  (pageRequired: A, basedOnCorrectReturnPreviousPage: Option[B])(val reads: Reads[C])
case class CorrectReturnPreviousPage[+B >: QuestionPage[C],C](page: B, CorrectReturnPreviousPageAnswerRequired: C)(val reads: Reads[C])



