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
import models.LitresInBands
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import utilities.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

trait SummaryListRowLitresHelper {

  val actionUrl: String
  val bandActionIdKey: String
  val bandHiddenKey: String
  val hasZeroLevy: Boolean = false
  val isNegativeLevy: Boolean = false


  val lowBand = "lowband"
  val highBand = "highband"

  def rows(litresInBands: LitresInBands, isCheckAnswers: Boolean, includeLevyRows: Boolean = true)
          (implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    Seq(
      Option(bandRow(litresInBands.lowBand, lowBand, isCheckAnswers, includeLevyRows)),
      if (includeLevyRows) Option(bandLevyRow(litresInBands.lowBand, config.lowerBandCostPerLitre, lowBand)) else None,
      Option(bandRow(litresInBands.highBand, highBand, isCheckAnswers, includeLevyRows)),
      if (includeLevyRows) Option(bandLevyRow(litresInBands.highBand, config.higherBandCostPerLitre, highBand)) else None
    ).flatten
  }

  private def bandRow(litres: Long, band: String, isCheckAnswers: Boolean, noBorder: Boolean)(implicit messages: Messages): SummaryListRow = {
    val key = if (band == lowBand) {
      "litres.lowBand"
    } else {
      "litres.highBand"
    }
    val value = HtmlFormat.escape(java.text.NumberFormat.getInstance.format(litres)).toString
    SummaryListRow(
      key = key,
      value = ValueViewModel(HtmlContent(value)).withCssClass("sdil-right-align--desktop"),
      classes = if (noBorder) "govuk-summary-list__row--no-border" else "",
      actions = action(isCheckAnswers, band)
    )
}

private def bandLevyRow(litres: Long, bandCostPerLitre: BigDecimal, band: String)(implicit messages: Messages): SummaryListRow = {
    val key = if (band == lowBand) {
      "litres.lowBandLevy"
    } else {
      "litres.highBandLevy"
    }

    val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(levy(litres, bandCostPerLitre))).toString

    SummaryListRowViewModel(
      key = key,
      value = ValueViewModel(HtmlContent(value)).withCssClass("sdil-right-align--desktop"),
      actions = Seq()
    ).withCssClass("govuk-summary-list__row--no-actions")
  }

  private def levy(litres: BigDecimal, bandCostPerLitre: BigDecimal): BigDecimal = {
    if (hasZeroLevy) {
      0
    } else if (isNegativeLevy) {
      litres * bandCostPerLitre.toDouble * -1
    } else {
      litres * bandCostPerLitre.toDouble
    }
  }

  def action(isCheckAnswers: Boolean, band: String)(implicit messages: Messages): Option[Actions] = if (isCheckAnswers) {
    Some(Actions("",
      items =
        Seq(
          ActionItemViewModel("site.change", actionUrl)
            .withAttribute(("id", s"change-$band-litreage-$bandActionIdKey"))
            .withVisuallyHiddenText(messages(s"${bandHiddenKey}.$band.litres.hidden")))))
  } else {
    None
  }

}
