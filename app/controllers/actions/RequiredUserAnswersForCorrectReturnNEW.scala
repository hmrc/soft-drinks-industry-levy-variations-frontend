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
import models.{CheckMode, UserAnswers}
import pages.Page
import play.api.mvc.Result

import javax.inject.Inject
import scala.concurrent.Future

class RequiredUserAnswersForCorrectReturnNEW @Inject() extends RequiredUserAnswersHelper {
  private[controllers] def requireData(page: Page, userAnswers: UserAnswers, subscription: RetrievedSubscription)
                                      (action: => Future[Result]): Future[Result] = {
    val missingAnswers = returnMissingAnswers(userAnswers, subscription, page.previousPagesRequired)
    getResultFromMissingAnswers(missingAnswers, action, mode = CheckMode)
  }
}