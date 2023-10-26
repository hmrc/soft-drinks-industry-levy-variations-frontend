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

import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.requests.DataRequest
import models.{CheckMode, LitresInBands, NormalMode, RetrievedSubscription, SdilReturn}
import pages.{Page, QuestionPage}
import pages.correctReturn._
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.{GenericLogger, UserTypeCheck}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswersForCorrectReturn @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {

  def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    page match {
      case CorrectReturnBaseCYAPage => checkYourAnswersRequiredData(action)
      case CorrectReturnCheckChangesPage => checkChangesRequiredData(action)
      case _ => action
    }
  }

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(mainRoute)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) if List(PackAtBusinessAddressPage, PackagingSiteDetailsPage).contains(page) =>
        genericLogger.logger.info(s"${request.userAnswers.id} now requires packaging sites")
        Future.successful(Redirect(PackAtBusinessAddressPage.url(CheckMode)))
      case Some(page) if page == SecondaryWarehouseDetailsPage =>
        genericLogger.logger.info(s"${request.userAnswers.id} now has option to add warehouse")
        Future.successful(Redirect(page.url(CheckMode)))
      case Some(page) => genericLogger.logger.warn(s"${request.userAnswers.id} has hit correct return base CYA and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def checkChangesRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val userAnswersMissing: List[CorrectReturnRequiredPage[_, _, _]] = returnMissingAnswers(correctChangesJourney)
    userAnswersMissing.headOption.map(_.pageRequired.asInstanceOf[Page]) match {
      case Some(page) => genericLogger.logger.warn(s"${request.userAnswers.id} has hit check changes CYA and is missing $userAnswersMissing")
        Future.successful(Redirect(page.url(CheckMode)))
      case None => action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[CorrectReturnRequiredPage[_, _, _]])
                                                                         (implicit request: DataRequest[_]): List[CorrectReturnRequiredPage[_, _, _]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = request.userAnswers
        .get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnCorrectReturnPreviousPage.nonEmpty) match {
        case (false, true) =>
          val CorrectReturnPreviousPage: CorrectReturnPreviousPage[QuestionPage[B], B] = listItem.basedOnCorrectReturnPreviousPage
            .get.asInstanceOf[CorrectReturnPreviousPage[QuestionPage[B], B]]
          val CorrectReturnPreviousPageAnswer: Option[B] = request.userAnswers
            .get(CorrectReturnPreviousPage.page)(CorrectReturnPreviousPage.reads)
          !CorrectReturnPreviousPageAnswer.contains(CorrectReturnPreviousPage.CorrectReturnPreviousPageAnswerRequired)
        case (false, _) => false
        case _ => true
      }
    }
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

  private[controllers] def mainRoute(implicit dataRequest: DataRequest[_]): List[CorrectReturnRequiredPage[_, _, _]] = {
    smallProducerCheck(dataRequest.subscription) ++ restOfJourney ++ packingListReturnChange(dataRequest) ++ warehouseListReturnChange(dataRequest)
  }

  private[controllers] def restOfJourney: List[CorrectReturnRequiredPage[_, _, _]] = {
    List(CorrectReturnRequiredPage(PackagedAsContractPackerPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(HowManyPackagedAsContractPackerPage,
        Some(CorrectReturnPreviousPage(PackagedAsContractPackerPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      CorrectReturnRequiredPage(ExemptionsForSmallProducersPage, None)(implicitly[Reads[Boolean]]),
      CorrectReturnRequiredPage(AddASmallProducerPage,
        Some(CorrectReturnPreviousPage(ExemptionsForSmallProducersPage, true)(implicitly[Reads[Boolean]])))(implicitly[Reads[AddASmallProducer]]),
      CorrectReturnRequiredPage(BroughtIntoUKPage, None)(implicitly[Reads[Boolean]]),
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
  }

  private[controllers] def correctChangesJourney: List[CorrectReturnRequiredPage[_, _, _]] = {
    List(
      CorrectReturnRequiredPage(CorrectionReasonPage, None)(implicitly[Reads[String]]),
      CorrectReturnRequiredPage(RepaymentMethodPage, None)(implicitly[Reads[RepaymentMethod]])
    )
  }

  private[controllers] val packingListReturnChange: DataRequest[_] => List[CorrectReturnRequiredPage[_, _, _]] = { (request: DataRequest[_]) =>
    if (UserTypeCheck.isNewPacker(SdilReturn.apply(request.userAnswers), request.subscription) && request.subscription.productionSites.isEmpty) {
      List(CorrectReturnRequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]),
        CorrectReturnRequiredPage(PackagingSiteDetailsPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }

  private[controllers] val warehouseListReturnChange: DataRequest[_] => List[CorrectReturnRequiredPage[_, _, _]] = { (request: DataRequest[_]) =>
    if (UserTypeCheck.isNewImporter(SdilReturn.apply(request.userAnswers), request.subscription)) {
      List(CorrectReturnRequiredPage(AskSecondaryWarehouseInReturnPage, None)(implicitly[Reads[Boolean]]))
    } else {
      List.empty
    }
  }

}


case class CorrectReturnRequiredPage[+A >: QuestionPage[C], +B >: CorrectReturnPreviousPage[_, _], C]
  (pageRequired: A, basedOnCorrectReturnPreviousPage: Option[B])(val reads: Reads[C])
case class CorrectReturnPreviousPage[+B >: QuestionPage[C],C](page: B, CorrectReturnPreviousPageAnswerRequired: C)(val reads: Reads[C])



