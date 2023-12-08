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

package errors

import models.ReturnPeriod
import play.api.data.Form

sealed trait VariationsErrors

object SessionDatabaseInsertError extends VariationsErrors

object SessionDatabaseDeleteError extends VariationsErrors

object SessionDatabaseGetError extends VariationsErrors

object ReturnsStillPending extends VariationsErrors

object FailedToAddDataToUserAnswers extends VariationsErrors

object UnexpectedResponseFromSDIL extends VariationsErrors

object NoSdilReturnForPeriod extends VariationsErrors

object NoVariableReturns extends VariationsErrors

object MissingRequiredAnswers extends VariationsErrors

case class SelectReturnFormError(formWithError: Form[String], returnPeriods: List[ReturnPeriod]) extends VariationsErrors
