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
import models.{CheckMode, LitresInBands, Mode, NormalMode, UserAnswers}
import pages.changeActivity._
import pages.{Page, QuestionPage, RequiredPageNew}
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswersForChangeActivity @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {

  def getRedirectFromPage(page: Page, mode: Mode = NormalMode): Future[Result] = Future.successful(Redirect(page.url(mode)))

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

  def getResultFromRedirectConditions(redirectConditions: List[(Boolean, Page)], action: => Future[Result], mode: Mode = NormalMode): Future[Result] = {
    redirectConditions
      .filter(_._1)
      .map(_._2)
      .headOption
      .map(getRedirectFromPage(_, mode))
      .getOrElse(action)
  }
  def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val mode = if (page == ChangeActivityCYAPage) CheckMode else NormalMode
    getResultFromRedirectConditions(transformRequiredPageIntoBooleanPageList(request.userAnswers, page.redirectConditions), action, mode = mode)
  }
}
