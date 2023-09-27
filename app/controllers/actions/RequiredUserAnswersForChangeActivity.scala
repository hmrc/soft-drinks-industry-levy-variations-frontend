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

import controllers.changeActivity.routes
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, Mode}
import models.requests.DataRequest
import pages.{Page, QuestionPage}
import pages.changeActivity._
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

  private val pageRouteMap: Map[Page, Mode => String] = Map(
    AmountProducedPage -> (_ => routes.AmountProducedController.onPageLoad(CheckMode).url),
    ChangeActivityCYAPage -> (_ => routes.ChangeActivityCYAController.onPageLoad.url),
    ContractPackingPage -> (_ => routes.ContractPackingController.onPageLoad(CheckMode).url),
    HowManyContractPackingPage -> (_ => routes.HowManyContractPackingController.onPageLoad(CheckMode).url),
    HowManyImportsPage -> (_ => routes.HowManyImportsController.onPageLoad(CheckMode).url),
    HowManyOperatePackagingSiteOwnBrandsPage -> (_ => routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url),
    ImportsPage -> (_ => routes.ImportsController.onPageLoad(CheckMode).url),
    OperatePackagingSiteOwnBrandsPage -> (_ => routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url),
    PackagingSiteDetailsPage -> (_ => routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url),
    PackAtBusinessAddressPage -> (_ => routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url),
//    NOTE: Do not think below two are needed
//    RemovePackagingSiteDetailsPage -> (_ => routes.RemovePackagingSiteDetailsController.onPageLoad(CheckMode).url),
//    RemoveWarehouseDetailsPage -> (_ => routes.RemoveWarehouseDetailsController.onPageLoad(CheckMode).url),
//    TODO: Need to add check mode to below in theory - can be a later ticket
    SecondaryWarehouseDetailsPage -> (_ => routes.SecondaryWarehouseDetailsController.onPageLoad.url),
//    NOTE: Do not think below is needed
//    SuggestDeregistrationPage -> (_ => routes.SuggestDeregistrationController.onPageLoad.url),
    ThirdPartyPackagersPage -> (_ => routes.ThirdPartyPackagersController.onPageLoad(CheckMode).url)
  )

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val userAnswersMissing: List[RequiredPage[_,_,_]] = returnMissingAnswers(journey)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(s"${request.userAnswers.id} has hit CYA and is missing $userAnswersMissing, user will be redirected to ${userAnswersMissing.head.pageRequired}")
      val missingPage: Page = userAnswersMissing.head.pageRequired.asInstanceOf[Page]
      Future.successful(Redirect(pageRouteMap(missingPage)(CheckMode)))
//      NOTE: pageRouteMap removes need to put url on Page trait
//      Future.successful(Redirect(userAnswersMissing.head.pageRequired.asInstanceOf[Page].url(CheckMode)))
    } else {
      action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_, _, _]])
                                                                         (implicit request: DataRequest[_]): List[RequiredPage[_, _, _]] = {
    list.filterNot { listItem =>
      val currentPageFromUserAnswers: Option[A] = request.userAnswers.get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
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
          if (previousUserAnswersMatchedToResultInCurrentPageRequired.contains(true) && previousUserAnswersMatchedToResultInCurrentPageRequired.contains(false)) {
            true
          } else {
            !previousUserAnswersMatchedToResultInCurrentPageRequired.contains(false)
          }
        case (false, _) => false
        case _ => true
      }
    }
  }

  private val implicitAmountProduced = implicitly[Reads[AmountProduced]]
  private val implicitBands = implicitly[Reads[LitresInBands]]
  private val implicitBoolean = implicitly[Reads[Boolean]]
  private val implicitString = implicitly[Reads[String]]

  private val largeProducer = AmountProduced.enumerable.withName("large").get
  private val smallProducer = AmountProduced.enumerable.withName("small").get
  private val noneProducer = AmountProduced.enumerable.withName("none").get
  private val previousPageSmallOrNonProducer = PreviousPage(AmountProducedPage, List(smallProducer, noneProducer))(implicitly[Reads[AmountProduced]])
  //      TODO: RequiredPage for each page in pageRouteMap. Implicitly reads, use List.empty initially
  //      TODO: Then build up list of previous pages
  private val pagesRequiredForAmountProducedPage: List[List[PreviousPage[_, _]]] = List.empty
  private val pagesRequiredForContractPackingPage: List[List[PreviousPage[_, _]]] = List.empty
  private val pagesRequiredForHowManyContractPackingPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(ContractPackingPage, List(true))(implicitBoolean))
  )
  private val pagesRequiredForHowManyImportsPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(ImportsPage, List(true))(implicitBoolean))
  )
  private val pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitBoolean))
  )
  private val pagesRequiredForImportsPage: List[List[PreviousPage[_, _]]] = List.empty
  private val pagesRequiredForOperatePackagingSiteOwnBrandsPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(AmountProducedPage, List(smallProducer, largeProducer))(implicitAmountProduced))
  )
  private val pagesRequiredForPackagingSiteDetailsPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(PackAtBusinessAddressPage, List(true, false))(implicitBoolean))
  )
  private val pagesRequiredForPackAtBusinessAddressPage: List[List[PreviousPage[_, _]]] = List(
    List(previousPageSmallOrNonProducer, PreviousPage(ContractPackingPage, List(true))(implicitBoolean)),
    List(PreviousPage(AmountProducedPage, List(largeProducer))(implicitAmountProduced), PreviousPage(OperatePackagingSiteOwnBrandsPage, List(false))(implicitBoolean), PreviousPage(ContractPackingPage, List(true))(implicitBoolean)),
    List(PreviousPage(AmountProducedPage, List(largeProducer))(implicitAmountProduced), PreviousPage(OperatePackagingSiteOwnBrandsPage, List(true))(implicitBoolean), PreviousPage(ContractPackingPage, List(true, false))(implicitBoolean))
  )
//  TODO: Is below correct?
  private val pagesRequiredForSecondaryWarehouseDetailsPage: List[List[PreviousPage[_, _]]] = List.empty
  private val pagesRequiredForThirdPartyPackagersPage: List[List[PreviousPage[_, _]]] = List(
    List(PreviousPage(AmountProducedPage, List(smallProducer))(implicitAmountProduced))
  )

  private[controllers] def journey: List[RequiredPage[_,_,_]] = {
    List(
      pagesRequiredForAmountProducedPage.map(RequiredPage(AmountProducedPage, _)(implicitAmountProduced)),
      ////      NOTE: Do not think below are needed
      ////      RequiredPage(ChangeActivityCYAPage, List.empty)(implicitBoolean),
      pagesRequiredForContractPackingPage.map(RequiredPage(ContractPackingPage, _)(implicitBoolean)),
      pagesRequiredForHowManyContractPackingPage.map(RequiredPage(HowManyContractPackingPage, _)(implicitBands)),
      pagesRequiredForHowManyImportsPage.map(RequiredPage(HowManyImportsPage, _)(implicitBands)),
      pagesRequiredForHowManyOperatePackagingSiteOwnBrandsPage.map(RequiredPage(HowManyOperatePackagingSiteOwnBrandsPage, _)(implicitBands)),
      pagesRequiredForImportsPage.map(RequiredPage(ImportsPage, _)(implicitBoolean)),
      pagesRequiredForOperatePackagingSiteOwnBrandsPage.map(RequiredPage(OperatePackagingSiteOwnBrandsPage, _)(implicitBoolean)),
      pagesRequiredForPackagingSiteDetailsPage.map(RequiredPage(PackagingSiteDetailsPage, _)(implicitBoolean)),
      pagesRequiredForPackAtBusinessAddressPage.map(RequiredPage(PackAtBusinessAddressPage, _)(implicitBoolean)),
      ////      NOTE: Do not think below two are needed
      ////      RequiredPage(RemovePackagingSiteDetailsPage, List.empty)(implicitBoolean),
      ////      RequiredPage(RemoveWarehouseDetailsPage, List.empty)(implicitBoolean),
      pagesRequiredForSecondaryWarehouseDetailsPage.map(RequiredPage(SecondaryWarehouseDetailsPage, _)(implicitBoolean)),
      ////      RequiredPage(SuggestDeregistrationPage, List.empty)(implicitBoolean),
      pagesRequiredForThirdPartyPackagersPage.map(RequiredPage(ThirdPartyPackagersPage, _)(implicitBoolean))
    ).flatten
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPages: List[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C], C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
