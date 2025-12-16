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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait SelectChange

object SelectChange extends Enumerable.Implicits {

  case object UpdateRegisteredDetails extends WithName("updateRegisteredDetails") with SelectChange
  case object ChangeActivity extends WithName("changeActivity") with SelectChange
  case object CancelRegistration extends WithName("cancelRegistration") with SelectChange
  case object CorrectReturn extends WithName("correctReturn") with SelectChange

  val values: Seq[SelectChange] = Seq(
    UpdateRegisteredDetails,
    ChangeActivity,
    CancelRegistration,
    CorrectReturn
  )

  val valuesWithOutCorrectReturns: Seq[SelectChange] = values.filterNot(_ == CorrectReturn)

  val valuesWithOnlyCorrectReturns: Seq[SelectChange] = values.filter(_ == CorrectReturn)

  def options(hasCorrectableReturns: Boolean, isDeregistered: Boolean)(implicit messages: Messages): Seq[RadioItem] = {
    val valuesList =
      if (isDeregistered) valuesWithOnlyCorrectReturns
      else if (hasCorrectableReturns) values
      else valuesWithOutCorrectReturns
    valuesList.zipWithIndex.map { case (value, index) =>
      RadioItem(
        content = Text(messages(s"selectChange.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
    }
  }

  implicit val enumerable: Enumerable[SelectChange] =
    Enumerable(values.map(v => v.toString -> v)*)
}
