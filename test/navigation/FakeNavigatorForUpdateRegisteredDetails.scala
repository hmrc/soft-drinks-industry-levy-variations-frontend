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

package navigation

import models.backend.RetrievedSubscription
import models.changeActivity.AmountProduced
import models.{Mode, UserAnswers}
import pages.Page
import play.api.mvc.Call

class FakeNavigatorForUpdateRegisteredDetails(desiredRoute: Call) extends NavigatorForUpdateRegisteredDetails {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, amountProduced: Option[AmountProduced] = None, subscription: Option[RetrievedSubscription] = None): Call =
    desiredRoute
}
