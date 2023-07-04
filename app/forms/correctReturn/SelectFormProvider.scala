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

package forms.correctReturn

import javax.inject.Inject
import forms.mappings.Mappings
import models.ReturnPeriod
import play.api.data.Form
import models.correctReturn.Select
import play.api.libs.json.{JsValue, Json}

class SelectFormProvider @Inject() extends Mappings {

  def apply(): Form[ReturnPeriod] =
    Form(
      "value" -> text("correctReturn.select.error.required").transform[ReturnPeriod](s => Json.fromJson[ReturnPeriod](Json.parse(s)).get, j => Json.toJson(j).toString())
    )
}
