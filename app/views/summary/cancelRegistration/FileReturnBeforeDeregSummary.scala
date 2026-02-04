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

package views.summary.cancelRegistration

import models.ReturnPeriod
import play.api.i18n.Messages
import play.twirl.api.Html

import java.time.format.DateTimeFormatter

object FileReturnBeforeDeregSummary {

  def displayMessage(returns: List[ReturnPeriod])(implicit messages: Messages): Html =
    if (returns.size == 1) {
      val returnPeriod = returns.head
      Html(
        messages(
          "cancelRegistration.fileReturnBeforeDereg.one-return",
          returnPeriod.start.format(DateTimeFormatter.ofPattern("MMMM")),
          returnPeriod.start.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        )
      )
    } else {
      Html(messages("cancelRegistration.fileReturnBeforeDereg.many-returns", returns.size))
    }
}
