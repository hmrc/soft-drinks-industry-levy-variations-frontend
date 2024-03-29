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

package views.helpers

import models.ReturnPeriod
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.time.format.DateTimeFormatter

object ReturnPeriodsRadios {

  lazy val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

  def getRadioItemsForYear(returnPeriodsForYear: List[ReturnPeriod], yearIndex: Int)
                          (implicit messages: Messages): Seq[RadioItem] = {
    returnPeriodsForYear.zipWithIndex.map { case (returnPeriod, returnPeriodIndex) =>
      RadioItem(
        content = Text(messages("correctReturn.select.returnMessage",
          monthFormatter.format(returnPeriod.start),
          monthFormatter.format(returnPeriod.end),
          returnPeriod.year.toString)),
        value = Some(returnPeriod.radioValue),
        id = Some(s"value_${yearIndex}_$returnPeriodIndex")
      )
    }
  }

}
