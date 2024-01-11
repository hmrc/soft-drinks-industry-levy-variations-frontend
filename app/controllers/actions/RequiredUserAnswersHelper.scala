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
import models.requests.DataRequest
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.changeActivity._
import pages.{Page, RequiredPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class RequiredUserAnswersHelper @Inject() extends ActionHelpers {

  private def getRedirectFromPage(page: Page, mode: Mode = NormalMode): Future[Result] = Future.successful(Redirect(page.url(mode)))

  private def transformRequiredPageIntoBooleanPageList(
                                                        userAnswers: UserAnswers,
                                                        subscription: RetrievedSubscription,
                                                        requiredPageList: (UserAnswers, RetrievedSubscription) => List[RequiredPage]
                                                      ): List[(Boolean, Page)] = {
    val requiredPageListFromUserAnswers = requiredPageList(userAnswers, subscription)
    requiredPageListFromUserAnswers.map(requiredPage => {
      val isMissing = (requiredPage.additionalPreconditions :+ userAnswers.isEmpty(requiredPage.page)).forall(bool => bool)
      (isMissing, requiredPage.page)
    })
  }

  private[controllers] def getResultFromMissingAnswers(missingAnswers: List[Page], action: => Future[Result], mode: Mode = NormalMode): Future[Result] = {
    missingAnswers
      .headOption
      .map(getRedirectFromPage(_, mode))
      .getOrElse(action)
  }

  private[controllers] def returnMissingAnswers(
                            userAnswers: UserAnswers,
                            subscription: RetrievedSubscription,
                            requiredPageList: (UserAnswers, RetrievedSubscription) => List[RequiredPage]
                          ): List[Page] = {
    val previousPagesRequired = transformRequiredPageIntoBooleanPageList(userAnswers, subscription, requiredPageList)
    previousPagesRequired
      .filter(_._1)
      .map(_._2)
  }

  private[controllers] def requireData(page: Page, userAnswers: UserAnswers, subscription: RetrievedSubscription)
                                      (action: => Future[Result]): Future[Result]
}
