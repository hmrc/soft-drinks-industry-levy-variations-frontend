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

package models.correctReturn

import play.api.i18n.Messages
import models.{Enumerable, WithName}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait RepaymentMethod

object RepaymentMethod extends Enumerable.Implicits {

  case object BankAccount extends WithName("bankAccount") with RepaymentMethod
  case object SDILAccount extends WithName("sdilAccount") with RepaymentMethod

  val values: Seq[RepaymentMethod] = Seq(
    BankAccount, SDILAccount
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"correctReturn.repaymentMethod.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[RepaymentMethod] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
