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

package models.updateRegisteredDetails

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._
import models.{ Enumerable, WithName }

sealed trait ChangeRegisteredDetails

object ChangeRegisteredDetails extends Enumerable.Implicits {

  case object Sites extends WithName("sites") with ChangeRegisteredDetails
  case object ContactDetails extends WithName("contactDetails") with ChangeRegisteredDetails
  case object BusinessAddress extends WithName("businessAddress") with ChangeRegisteredDetails

  val values: Seq[ChangeRegisteredDetails] = Seq(
    Sites,
    ContactDetails,
    BusinessAddress
  )

  val voluntaryValues: Seq[ChangeRegisteredDetails] = Seq(
    ContactDetails,
    BusinessAddress
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"updateRegisteredDetails.changeRegisteredDetails.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  def voluntaryCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    voluntaryValues.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"updateRegisteredDetails.changeRegisteredDetails.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerableNonVoluntary: Enumerable[ChangeRegisteredDetails] =
    Enumerable(values.map(v => v.toString -> v)*)

  val enumerableVoluntary: Enumerable[ChangeRegisteredDetails] =
    Enumerable(voluntaryValues.map(v => v.toString -> v)*)

}
