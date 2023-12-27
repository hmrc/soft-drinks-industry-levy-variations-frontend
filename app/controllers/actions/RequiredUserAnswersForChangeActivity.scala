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

import models.changeActivity.AmountProduced
import models.requests.DataRequest
import models.{CheckMode, LitresInBands}
import pages.changeActivity._
import pages.{Page, QuestionPage}
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswersForChangeActivity @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {
  def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    page match {
      case ChangeActivityCYAPage => checkYourAnswersRequiredData(action)
      case ThirdPartyPackagersPage => thirdPartyPackagersPageRequiredData(action)
      case OperatePackagingSiteOwnBrandsPage => operatePackagingSiteOwnBrandsPageRequiredData(action)
      case ContractPackingPage => contractPackagingPageRequiredData(action)
      case ImportsPage => importsPageRequiredData(action)
      case _ => action
    }
  }

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val fullJourney = baseJourney ++ packagingSiteChangeActivityJourney(request.userAnswers.packagingSiteList.isEmpty)
    val userAnswersMissing: List[RequiredPage[_,_,_]] = returnMissingAnswers(fullJourney)
    println(Console.YELLOW + "Missing UAs" + userAnswersMissing + Console.WHITE)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit CYA and is missing $userAnswersMissing, user will be redirected to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def thirdPartyPackagersPageRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val upToThirdPartyPackagersJourney = thirdPartyPackagersPageJourney
    val userAnswersMissing: List[RequiredPage[_, _, _]] = returnMissingAnswers(upToThirdPartyPackagersJourney)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit $ThirdPartyPackagersPage and is missing $userAnswersMissing, user will be redirected" +
          s" to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def operatePackagingSiteOwnBrandsPageRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val upToOperatePackagingSiteOwnBrandsJourney = thirdPartyPackagersPageJourney ++ operatePackagingSiteOwnBrandsPageJourney
    val userAnswersMissing: List[RequiredPage[_, _, _]] = returnMissingAnswers(upToOperatePackagingSiteOwnBrandsJourney)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit $OperatePackagingSiteOwnBrandsPage and is missing $userAnswersMissing," +
          s" user will be redirected to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def contractPackagingPageRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val upToContractPackagingPageJourney = thirdPartyPackagersPageJourney ++ operatePackagingSiteOwnBrandsPageJourney ++ contractPackagingPageJourney
    val userAnswersMissing: List[RequiredPage[_, _, _]] = returnMissingAnswers(upToContractPackagingPageJourney)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit $ContractPackingPage and is missing $userAnswersMissing, redirected to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def importsPageRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val upToImportsPageJourney = thirdPartyPackagersPageJourney ++ operatePackagingSiteOwnBrandsPageJourney ++ contractPackagingPageJourney ++ importsPageJourney
    val userAnswersMissing: List[RequiredPage[_, _, _]] = returnMissingAnswers(upToImportsPageJourney)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit $ImportsPage and is missing $userAnswersMissing, user redirected to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_, _, _]])
                                                                         (implicit request: DataRequest[_]): List[RequiredPage[_, _, _]] = {
    val missingList = list.filterNot { listItem =>
      val currentPageFromUserAnswers: Option[A] = request.userAnswers.get(listItem.pageRequired
        .asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPageFromUserAnswers.isDefined, listItem.basedOnPreviousPages.nonEmpty) match {
        case (false, true) =>
          val previousUserAnswersMatchedToResultInCurrentPageRequired: List[Boolean] = listItem.basedOnPreviousPages.map { previousListItem =>
            val previousPageRequired: PreviousPage[QuestionPage[B], B] = previousListItem.asInstanceOf[PreviousPage[QuestionPage[B], B]]
            val previousPageAnswer: Option[B] = request.userAnswers.get(previousPageRequired.page)(previousPageRequired.reads)
            previousPageRequired.previousPageAnswerRequired match {
              case Nil => previousPageAnswer.isEmpty
              case _ => !previousPageAnswer.exists(i => previousPageRequired.previousPageAnswerRequired.contains(i))
            }
          }
          if (previousUserAnswersMatchedToResultInCurrentPageRequired.contains(true)) {
            true
          } else {
            false
          }
        case (false, _) => false
        case _ => true
      }
    }
    println(Console.YELLOW + "missing list " + missingList + Console.WHITE)
    missingList
  }

  private val implicitAmountProduced = implicitly[Reads[AmountProduced]]
  private val implicitBands = implicitly[Reads[LitresInBands]]
  private val implicitBoolean = implicitly[Reads[Boolean]]

  val largeProducer: AmountProduced = AmountProduced.enumerable.withName("large").get
  val smallProducer: AmountProduced = AmountProduced.enumerable.withName("small").get
  private val noneProducer: AmountProduced = AmountProduced.enumerable.withName("none").get
  private val previousPageSmallOrNonProducer = PreviousPage(AmountProducedPage, List(smallProducer, noneProducer))(implicitly[Reads[AmountProduced]])

  private[controllers] def baseJourney: List[RequiredPage[_,_,_]] = {
    val pagesRequiredForHowManyContractPackingPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(ContractPackingPage, List(true))(implicitBoolean)))
    val pagesRequiredForHowManyImportsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(ImportsPage, List(true))(implicitBoolean)))
    val pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitBoolean)))
    val pagesRequiredForSecondaryWarehouseDetailsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(ImportsPage, List(true))(implicitBoolean)))
    List(
      importsPageJourney,
      pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage.map(RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, _)(implicitBands)),
      pagesRequiredForHowManyContractPackingPage.map(RequiredPage(HowManyContractPackingPage, _)(implicitBands)),
      pagesRequiredForHowManyImportsPage.map(RequiredPage(HowManyImportsPage, _)(implicitBands)),
      pagesRequiredForSecondaryWarehouseDetailsPage.map(RequiredPage(SecondaryWarehouseDetailsPage, _)(implicitBoolean)),
    ).flatten
  }

  private[controllers] def thirdPartyPackagersPageJourney: List[RequiredPage[_, _, _]] = {
    List(
      List(RequiredPage(ThirdPartyPackagersPage, List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced)))(implicitBoolean))
      ).flatten
    }

  private[controllers] def operatePackagingSiteOwnBrandsPageJourney: List[RequiredPage[_, _, _]] = {
    List(
      List(RequiredPage(AmountProducedPage, List(smallProducer, largeProducer))(implicitAmountProduced)),
      List(RequiredPage(ThirdPartyPackagersPage, List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced)))(implicitBoolean)),
      List(RequiredPage(OperatePackagingSiteOwnBrandsPage, List(PreviousPage(AmountProducedPage,
        List(smallProducer, largeProducer))(implicitAmountProduced)))(implicitBoolean))
    ).flatten
 }

  private[controllers] def contractPackagingPageJourney: List[RequiredPage[_, _, _]] = {
      List(
        List(RequiredPage(AmountProducedPage, List.empty)(implicitAmountProduced)),
        List(RequiredPage(ThirdPartyPackagersPage, List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced)))(implicitBoolean)),
        List(RequiredPage(OperatePackagingSiteOwnBrandsPage, List(PreviousPage(AmountProducedPage,
          List(smallProducer, largeProducer))(implicitAmountProduced)))(implicitBoolean)),
        List(RequiredPage(ContractPackingPage, List.empty)(implicitBoolean))
      ).flatten
  }

  private[controllers] def importsPageJourney: List[RequiredPage[_, _, _]] = {
    List(
      List(RequiredPage(AmountProducedPage, List.empty)(implicitAmountProduced)),
      List(RequiredPage(ThirdPartyPackagersPage, List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced)))(implicitBoolean)),
      List(RequiredPage(OperatePackagingSiteOwnBrandsPage, List(PreviousPage(AmountProducedPage,
        List(smallProducer, largeProducer))(implicitAmountProduced)))(implicitBoolean)),
      List(RequiredPage(ContractPackingPage, List.empty)(implicitBoolean)),
      List(RequiredPage(ImportsPage, List.empty)(implicitBoolean))
    ).flatten
  }

  def packagingSiteChangeActivityJourney(emptyPackagingSites: Boolean): List[RequiredPage[_, _, _]] = {
    val pagesRequiredForPackagingSiteDetailsPage: List[List[PreviousPage[_, _]]] = List(
      List(previousPageSmallOrNonProducer, PreviousPage(ContractPackingPage, List(true))(implicitBoolean)),
      List(PreviousPage(AmountProducedPage, List(largeProducer))(implicitAmountProduced),
        PreviousPage(OperatePackagingSiteOwnBrandsPage, List(false))(implicitBoolean), PreviousPage(ContractPackingPage, List(true))(implicitBoolean)),
      List(PreviousPage(AmountProducedPage, List(largeProducer))(implicitAmountProduced),
        PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitBoolean), PreviousPage(ContractPackingPage, List(true, false))(implicitBoolean))
    )
    if (emptyPackagingSites) {
      List(pagesRequiredForPackagingSiteDetailsPage.map(RequiredPage(PackAtBusinessAddressPage, _)(implicitBoolean)),
        pagesRequiredForPackagingSiteDetailsPage.map(RequiredPage(PackagingSiteDetailsPage, _)(implicitBoolean))).flatten
    } else {
      List(pagesRequiredForPackagingSiteDetailsPage.map(RequiredPage(PackagingSiteDetailsPage, _)(implicitBoolean))).flatten
    }
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPages: List[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C], C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
