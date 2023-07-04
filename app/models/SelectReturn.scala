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

package models

import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.time.format.DateTimeFormatter

object SelectReturn {


  def options(returns: List[ReturnPeriod])(implicit messages: Messages): Seq[RadioItem] = {
    returns.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = HtmlContent(messages("correctReturn.select.returnMessage",
            value.start.format(DateTimeFormatter.ofPattern("MMMM")),
            value.end.format(DateTimeFormatter.ofPattern("MMMM")),
            value.start.getYear.toString)),
          value   = Some(Json.toJson(value).toString),
          id      = Some(s"value_$index")
        )
    }
  }
}


