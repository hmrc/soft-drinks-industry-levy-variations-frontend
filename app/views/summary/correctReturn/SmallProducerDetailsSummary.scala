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

package views.summary.correctReturn

import controllers.correctReturn.routes
import models.{CheckMode, SmallProducer}
import pages.QuestionPage
import pages.correctReturn.SmallProducerDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.summary.SummaryListRowLitresHelper

object SmallProducerDetailsSummary extends SummaryListRowLitresHelper {

  override val actionUrl: String = routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
  override val bandActionIdKey: String = "small-producers"
  override val bandHiddenKey: String = "contractPackedForRegisteredSmallProducers"
  val page: QuestionPage[Boolean] = SmallProducerDetailsPage
  val key: String = "smallProducerDetails.checkYourAnswersLabel"
  val action: String = routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
  val actionId: String = "change-small-producer-details"
  val hiddenText: String = "smallProducerDetails"
  override val hasZeroLevy: Boolean = true

  def producerList(smallProducersList: List[SmallProducer])(implicit messages: Messages): SummaryList = {
    val rows = smallProducersList.map {
      smallProducer =>
        val value = ValueViewModel(
          Text(
            smallProducer.alias
          )
        )

        SummaryListRowViewModel(
          key = smallProducer.sdilRef,
          value = value,
          actions = Seq(
            ActionItemViewModel("site.edit", controllers.routes.IndexController.onPageLoad.url)
              .withVisuallyHiddenText(messages("correctReturn.smallProducerDetails.edit.hidden", smallProducer.alias, smallProducer.sdilRef)),
            ActionItemViewModel("site.remove", controllers.routes.IndexController.onPageLoad.url)
              .withVisuallyHiddenText(messages("correctReturn.smallProducerDetails.remove.hidden", smallProducer.alias, smallProducer.sdilRef))
          )
        )
    }

    SummaryListViewModel(rows)
  }

}
