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

import models.requests.DataRequest
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.changeActivity._
import pages.{Page, RequiredPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequiredUserAnswersForChangeActivity @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {

  private def getRedirectFromPage(page: Page, mode: Mode = NormalMode): Future[Result] = Future.successful(Redirect(page.url(mode)))

  private def transformRequiredPageIntoBooleanPageList(
                                                        userAnswers: UserAnswers,
                                                        requiredPageList: UserAnswers => List[RequiredPage]
                                                      ): List[(Boolean, Page)] = {
    val requiredPageListFromUserAnswers = requiredPageList(userAnswers)
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
                            requiredPageList: UserAnswers => List[RequiredPage]
                          ): List[Page] = {
    val previousPagesRequired = transformRequiredPageIntoBooleanPageList(userAnswers, requiredPageList)
    previousPagesRequired
      .filter(_._1)
      .map(_._2)
  }
  private[controllers] def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val mode = if (page == ChangeActivityCYAPage) CheckMode else NormalMode
    val missingAnswers = returnMissingAnswers(request.userAnswers, page.previousPagesRequired)
    getResultFromMissingAnswers(missingAnswers, action, mode = mode)
  }
}
