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
      case _ => action
    }
  }

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val fullJourney = journey ++ packagingSiteChangeActivityJourney(request.userAnswers.packagingSiteList.isEmpty)
    val userAnswersMissing: List[RequiredPage[_,_,_]] = returnMissingAnswers(fullJourney)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(
        s"${request.userAnswers.id} has hit CYA and is missing $userAnswersMissing, user will be redirected to ${userAnswersMissing.head.pageRequired}")
      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_, _, _]])
                                                                         (implicit request: DataRequest[_]): List[RequiredPage[_, _, _]] = {
    list.filterNot { listItem =>
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
  }

  val implicitAmountProduced = implicitly[Reads[AmountProduced]]
  val implicitBands = implicitly[Reads[LitresInBands]]
  val implicitBoolean = implicitly[Reads[Boolean]]

  val largeProducer = AmountProduced.enumerable.withName("large").get
  val smallProducer = AmountProduced.enumerable.withName("small").get
  val noneProducer = AmountProduced.enumerable.withName("none").get
  val previousPageSmallOrNonProducer = PreviousPage(AmountProducedPage, List(smallProducer, noneProducer))(implicitly[Reads[AmountProduced]])

  private[controllers] def journey: List[RequiredPage[_,_,_]] = {

    val pagesRequiredForHowManyContractPackingPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(ContractPackingPage, List(true))(implicitBoolean)))
    val pagesRequiredForHowManyImportsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(ImportsPage, List(true))(implicitBoolean)))
    val pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitBoolean)))
    val pagesRequiredForOperatePackagingSiteOwnBrandsPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(AmountProducedPage, List(smallProducer, largeProducer))(implicitAmountProduced)))
    val pagesRequiredForThirdPartyPackagersPage: List[List[PreviousPage[_, _]]] =
      List(List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced)))
    List(
      List(RequiredPage(AmountProducedPage, List.empty)(implicitAmountProduced)),
      pagesRequiredForThirdPartyPackagersPage.map(RequiredPage(ThirdPartyPackagersPage, _)(implicitBoolean)),
      pagesRequiredForOperatePackagingSiteOwnBrandsPage.map(RequiredPage(OperatePackagingSiteOwnBrandsPage, _)(implicitBoolean)),
      pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage.map(RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, _)(implicitBands)),
      List(RequiredPage(ContractPackingPage, List.empty)(implicitBoolean)),
      pagesRequiredForHowManyContractPackingPage.map(RequiredPage(HowManyContractPackingPage, _)(implicitBands)),
      List(RequiredPage(ImportsPage, List.empty)(implicitBoolean)),
      pagesRequiredForHowManyImportsPage.map(RequiredPage(HowManyImportsPage, _)(implicitBands))
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
      pagesRequiredForPackagingSiteDetailsPage.map(RequiredPage(PackagingSiteDetailsPage, _)(implicitBoolean))
    } else {
      List.empty
    }
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPages: List[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C], C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
