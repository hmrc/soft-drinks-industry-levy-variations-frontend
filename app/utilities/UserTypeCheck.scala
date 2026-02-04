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

package utilities

import models.UserAnswers
import models.backend.RetrievedSubscription

object UserTypeCheck {
  def isNewImporter(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    val userIsNotAlreadyAnImporter = !subscription.activity.importer
    val totalImported = userAnswers.getCorrectReturnData.map(_.totalImported)
    totalImported.fold(false)(imported => (imported.lower > 0L && imported.higher > 0L) && userIsNotAlreadyAnImporter)
  }
  def isNewPacker(userAnswers: UserAnswers, subscription: RetrievedSubscription): Boolean = {
    val userIsNotAlreadyAPacker = !subscription.activity.contractPacker
    val totalPacked = userAnswers.getCorrectReturnData.map(_.totalPacked(userAnswers.smallProducerList))
    totalPacked.fold(false)(packed => (packed.lower > 0L && packed.higher > 0L) && userIsNotAlreadyAPacker)
  }
}
