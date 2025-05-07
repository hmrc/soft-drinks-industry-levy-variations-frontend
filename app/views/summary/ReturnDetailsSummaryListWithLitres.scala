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

package views.summary

import config.FrontendAppConfig
import models.{LitresInBands, UserAnswers}
import pages.QuestionPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

trait ReturnDetailsSummaryListWithLitres extends ReturnDetailsSummaryRowHelper {

  val page: QuestionPage[Boolean]
  val optLitresPage: Option[QuestionPage[LitresInBands]]
  val summaryLitres: SummaryListRowLitresHelper
  val key: String
  val action: String
  val actionId: String
  val hiddenText: String
  val isSmallProducerLitres: Boolean = false

//  TODO: Separate into changeActivity and correctReturn with and without bandLevyRows - and alter unit tests using these
  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean, includeLevyRows: Boolean = true)
                 (implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litresDetails: Seq[SummaryListRow] = optLitresPage match {
      case Some(litresPage) => getLitresDetails(userAnswers, isCheckAnswers, litresPage, includeLevyRows)
      case None if isSmallProducerLitres => getLitresForSmallProducer(userAnswers, isCheckAnswers)
      case None => Seq.empty
    }
    SummaryListViewModel(rows =
      row(userAnswers, isCheckAnswers) ++ litresDetails
    )
  }

  private def getLitresDetails(userAnswers: UserAnswers, isCheckAnswers: Boolean, litresPage: QuestionPage[LitresInBands], includeLevyRows: Boolean)
                              (implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    (userAnswers.get(page), userAnswers.get(litresPage)) match {
      case (Some(true), Some(litresInBands)) => summaryLitres.rows(litresInBands, isCheckAnswers, userAnswers.correctReturnPeriod, includeLevyRows)
      case _ => Seq.empty
    }
  }

  private def getLitresForSmallProducer(userAnswers: UserAnswers, isCheckAnswers: Boolean, includeLevyRows: Boolean = true)
                                       (implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    val smallProducerList = userAnswers.smallProducerList
    if(userAnswers.get(page).getOrElse(false) && smallProducerList.nonEmpty) {
      val lowBandLitres = smallProducerList.map(_.litreage.lower).sum
      val highBandLitres = smallProducerList.map(_.litreage.higher).sum
      val litresInBands = LitresInBands(lowBandLitres, highBandLitres)
      summaryLitres.rows(litresInBands, isCheckAnswers, userAnswers.correctReturnPeriod, includeLevyRows)
    } else {
      Seq.empty
    }
  }

}
