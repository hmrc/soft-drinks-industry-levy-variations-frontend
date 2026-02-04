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

import forms.mappings.Mappings
import models.{ LitresInBands, UserAnswers }
import models.correctReturn.AddASmallProducer
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class AddASmallProducerFormProvider @Inject() extends Mappings {

  def apply(userAnswers: UserAnswers): Form[AddASmallProducer] =
    Form(
      mapping(
        "producerName" -> optional(
          text().verifying(maxLength(160, "correctReturn.addASmallProducer.error.producerName.maxLength"))
        ),
        "referenceNumber" -> sdilReference(
          "correctReturn.addASmallProducer.error.referenceNumber.required",
          userAnswers.id
        ),
        "litres" -> tuple[Long, Long]("lowBand" -> litres("lowBand"), "highBand" -> litres("highBand"))
          .verifying("litres.error.negative", litres => litres._1 + litres._2 != 0)
      ) { case (producerName, referenceNumber, litres) =>
        AddASmallProducer(
          producerName = producerName,
          referenceNumber = referenceNumber,
          LitresInBands(litres._1, litres._2)
        )
      }(addASmallProducer =>
        Some(
          (
            addASmallProducer.producerName,
            addASmallProducer.referenceNumber,
            (addASmallProducer.litres.lowBand, addASmallProducer.litres.highBand)
          )
        )
      )
    )
}
