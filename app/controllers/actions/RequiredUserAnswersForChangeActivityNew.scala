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
import models.{CheckMode, LitresInBands, Mode, NormalMode, UserAnswers}
import org.checkerframework.checker.units.qual.A
import pages.{Page, QuestionPage}
import pages.changeActivity._
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import queries.Query

import scala.concurrent.Future

trait RequiredUserAnswersForChangeActivityNew {

  def getRedirectFromPage(page: Page, mode: Mode = NormalMode): Future[Result] = Future.successful(Redirect(page.url(mode)))

  def getResultFromRedirectConditions(redirectConditions: List[(Boolean, Page)], action: => Future[Result], mode: Mode = NormalMode): Future[Result] = {
    redirectConditions
      .filter(_._1)
      .map(_._2)
      .headOption
      .map(getRedirectFromPage(_, mode))
      .getOrElse(action)
  }

  case class RequiredPageNew(page: Page with Query, additionalPreconditions: List[Boolean] = List.empty)

  private def isSmallOrLargeProducer(userAnswers: UserAnswers): Boolean =
    userAnswers.get(AmountProducedPage).contains(AmountProduced.Large) || userAnswers.get(AmountProducedPage).contains(AmountProduced.Small)

  private def isSmallOrNoneProducer(userAnswers: UserAnswers): Boolean =
    userAnswers.get(AmountProducedPage).contains(AmountProduced.Small) || userAnswers.get(AmountProducedPage).contains(AmountProduced.None)

  private def transformRequiredPageIntoBooleanPageList(
                                                        userAnswers: UserAnswers,
                                                        requiredPageList: UserAnswers => List[RequiredPageNew]
                                                      ): List[(Boolean, Page)] = {
    val requiredPageListFromUserAnswers = requiredPageList(userAnswers)
    requiredPageListFromUserAnswers.map(requiredPage => {
      val bool = (requiredPage.additionalPreconditions :+ userAnswers.isEmpty(requiredPage.page)).forall(a => a)
      val page = requiredPage.page
      (bool, page)
    })
  }

  private def thirdPartyPackagersRedirectConditions(userAnswers: UserAnswers): List[RequiredPageNew] = List(
    RequiredPageNew(AmountProducedPage)
  )

  private[controllers] def thirdPartyPackagersPageRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, thirdPartyPackagersRedirectConditions)
    getResultFromRedirectConditions(redirectConditions, action)
  }

  private def operatePackagingSiteOwnBrandsConditions(userAnswers: UserAnswers): List[RequiredPageNew] = List(
    RequiredPageNew(AmountProducedPage),
    RequiredPageNew(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small)))
  )

  private[controllers] def operatePackagingSiteOwnBrandsPageRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, operatePackagingSiteOwnBrandsConditions)
    getResultFromRedirectConditions(redirectConditions, action)
  }

  private def contractPackagingConditions(userAnswers: UserAnswers): List[RequiredPageNew] = List(
    RequiredPageNew(AmountProducedPage),
    RequiredPageNew(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small))),
    RequiredPageNew(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(isSmallOrLargeProducer(userAnswers)))
  )

  private[controllers] def contractPackagingPageRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, contractPackagingConditions)
    getResultFromRedirectConditions(redirectConditions, action)
  }

  private def importsConditions(userAnswers: UserAnswers): List[RequiredPageNew] = List(
    RequiredPageNew(AmountProducedPage),
    RequiredPageNew(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small))),
    RequiredPageNew(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(isSmallOrLargeProducer(userAnswers))),
    RequiredPageNew(ContractPackingPage)
  )

  private[controllers] def importsPageRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, importsConditions)
    getResultFromRedirectConditions(redirectConditions, action)
  }

  private def howManyImportsConditions(userAnswers: UserAnswers): List[RequiredPageNew] = List(
    RequiredPageNew(AmountProducedPage),
    RequiredPageNew(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small))),
    RequiredPageNew(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(isSmallOrLargeProducer(userAnswers))),
    RequiredPageNew(ContractPackingPage),
    RequiredPageNew(ImportsPage)
  )

  private[controllers] def howManyImportsPageRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, howManyImportsConditions)
    getResultFromRedirectConditions(redirectConditions, action)
  }
  private def checkYourAnswersConditions(userAnswers: UserAnswers): List[RequiredPageNew] = {
    val eitherOperatePackagingSitesOrContractPacking = userAnswers.get(OperatePackagingSiteOwnBrandsPage).flatMap(ops => {
      userAnswers.get(ContractPackingPage).map(cp => {
        ops || cp
      })
    }).getOrElse(false)
    List(
      RequiredPageNew(AmountProducedPage),
      RequiredPageNew(ThirdPartyPackagersPage, additionalPreconditions = List(userAnswers.get(AmountProducedPage).contains(AmountProduced.Small))),
      RequiredPageNew(OperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(isSmallOrLargeProducer(userAnswers))),
      RequiredPageNew(HowManyOperatePackagingSiteOwnBrandsPage, additionalPreconditions = List(userAnswers.get(OperatePackagingSiteOwnBrandsPage).contains(true))),
      RequiredPageNew(ContractPackingPage),
      RequiredPageNew(HowManyContractPackingPage, additionalPreconditions = List(userAnswers.get(ContractPackingPage).contains(true))),
      RequiredPageNew(ImportsPage),
      RequiredPageNew(HowManyImportsPage, additionalPreconditions = List(userAnswers.get(ImportsPage).contains(true))),
      RequiredPageNew(PackAtBusinessAddressPage, additionalPreconditions = List(
        isSmallOrNoneProducer(userAnswers),
        userAnswers.get(ContractPackingPage).contains(true),
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPageNew(PackAtBusinessAddressPage, additionalPreconditions = List(
        userAnswers.get(AmountProducedPage).contains(AmountProduced.Large),
        eitherOperatePackagingSitesOrContractPacking,
        userAnswers.packagingSiteList.isEmpty
      )),
      RequiredPageNew(PackagingSiteDetailsPage, additionalPreconditions = List(
        isSmallOrNoneProducer(userAnswers),
        userAnswers.get(ContractPackingPage).contains(true)
      )),
      RequiredPageNew(PackagingSiteDetailsPage, additionalPreconditions = List(
        userAnswers.get(AmountProducedPage).contains(AmountProduced.Large),
        eitherOperatePackagingSitesOrContractPacking
      )),
      RequiredPageNew(SecondaryWarehouseDetailsPage, additionalPreconditions = List(
        userAnswers.get(ImportsPage).contains(true),
        !userAnswers.isEmpty(PackagingSiteDetailsPage)
      )),
    )
  }

  private[controllers] def checkYourAnswersRequiredDataNew(userAnswers: UserAnswers, action: => Future[Result]): Future[Result] = {
    val redirectConditions = transformRequiredPageIntoBooleanPageList(userAnswers, checkYourAnswersConditions)
    getResultFromRedirectConditions(redirectConditions, action, mode = CheckMode)
  }
}
