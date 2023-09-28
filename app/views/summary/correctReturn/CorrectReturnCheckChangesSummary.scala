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

import config.FrontendAppConfig
import models.correctReturn.ChangedPage
import models.{RetrievedSubscription, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.summary.correctReturn.ExemptionsForSmallProducersSummary
import views.summary.UKSitesSummary
import views.summary.changeActivity.{AmountProducedSummary, OperatePackagingSiteOwnBrandsSummary}

object CorrectReturnCheckChangesSummary {

//  def summaryListAndHeadings(userAnswers: UserAnswers, subscription: RetrievedSubscription)
//                            (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
//changedSummaryListAndHeadings(CorrectReturnBaseCYASummary.changedSummaryListAndHeadings(userAnswers, subscription, changedPage = true))
//  })))
//  }
//  }



  private def correctionReasonSummarySection(userAnswers: UserAnswers)
                                     (implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Seq[(String, SummaryList)] = {
    //    val correctionReasonSummary: Option[SummaryListRow] = CorrectionReasonSummary.row(userAnswers)
    //    val correctionReasonList: Option[(String, SummaryList)] = if (correctionReasonSummary.isEmpty) None else {
    //      Option(
    //        "correctReturn.correctionReason.checkYourAnswersLabel" ->
    //          correctionReasonSummary
    //      )
    //    }
    //    correctionReasonList
    //  }
    val correctionReasonSummary: Option[SummaryListRow] = CorrectionReasonSummary.row(userAnswers)
    val correctionReasonList: Option[(String, SummaryList)] = correctionReasonSummary.map(summary => {
        "correctReturn.correctionReason.checkYourAnswersLabel" ->
          SummaryList(Seq(summary))
})
    }
    val amountProducedSummary: Option[SummaryListRow] = AmountProducedSummary.row(userAnswers)
    val amountProducedSection: Option[(String, SummaryList)] = amountProducedSummary.map(summary => {
      "changeActivity.checkYourAnswers.amountProducedSection" -> SummaryList(Seq(summary))
    })
    val ownBrandsSummary: SummaryList = OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = false)
    val ownBrandsSection: Option[(String, SummaryList)] = if (ownBrandsSummary.rows.isEmpty) None else {
      Option(
        "changeActivity.checkYourAnswers.operatePackingSiteOwnBrandsSection" ->
          OperatePackagingSiteOwnBrandsSummary.summaryList(userAnswers, isCheckAnswers = true, includeLevyRows = false)
      )
    }
  }

}
