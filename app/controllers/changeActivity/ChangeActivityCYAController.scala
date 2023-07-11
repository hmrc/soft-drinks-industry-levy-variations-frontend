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

package controllers.changeActivity

import com.google.inject.Inject
import controllers.actions.ControllerActions
import config.FrontendAppConfig
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.summary.changeActivity.AmountProducedSummary
import views.html.changeActivity.ChangeActivityCYAView
import models.SelectChange.ChangeActivity
import views.summary.changeActivity.{ContractPackingSummary, ImportsSummary, OperatePackagingSiteOwnBrandsSummary, ThirdPartyPackagersSummary}

class ChangeActivityCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            implicit val config: FrontendAppConfig,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ChangeActivityCYAView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      val amountProducedSummary: Option[SummaryListRow] = AmountProducedSummary.row(request.userAnswers)
      val thirdPartyPackagersSummary: Option[SummaryListRow] = ThirdPartyPackagersSummary.row(request.userAnswers)
      val ownBrandsSummary: SummaryList = OperatePackagingSiteOwnBrandsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      val contractSummary: SummaryList = ContractPackingSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      val importsSummary: SummaryList = ImportsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      val amountProducedSection1: Option[(String, SummaryList)] = amountProducedSummary.map(summary => {
        "changeActivity.checkYourAnswers.amountProducedSection" -> SummaryList(Seq(summary))
      })
      val thirdPartyPackagersSection1: Option[(String, SummaryList)] = thirdPartyPackagersSummary.map(summary => {
        "changeActivity.checkYourAnswers.thirdPartyPackagersSection" -> SummaryList(Seq(summary))
      })
      val ownBrandsSection1: Option[(String, SummaryList)] = if (ownBrandsSummary.rows.nonEmpty) {
        Option(
          "changeActivity.checkYourAnswers.operatePackingSiteOwnBrandsSection" ->
            OperatePackagingSiteOwnBrandsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
        )
      } else None
      val contractSection1: Option[(String, SummaryList)] = if (contractSummary.rows.nonEmpty) {
        Option(
          "changeActivity.checkYourAnswers.contractPackingSection" ->
            ContractPackingSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
        )
      } else None
      val importsSection1: Option[(String, SummaryList)] = if (importsSummary.rows.nonEmpty) {
        Option(
          "changeActivity.checkYourAnswers.importsSection" ->
            ImportsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
        )
      } else None
      val sections1: Seq[(String, SummaryList)] = (amountProducedSection1 ++ thirdPartyPackagersSection1 ++ ownBrandsSection1 ++ contractSection1 ++ importsSection1).toSeq

      val alias: String = request.subscription.orgName
//      TODO: Implement Return Period in DLS-8346
      val returnPeriod: String = "RETURN PERIOD"
//      val sections = amountProducedSection ++ thirdPartyPackagersSection ++ operatePackingSiteOwnBrandsSection ++ contractPackingSection ++ importsSection

      Ok(view(alias, returnPeriod, sections1, routes.ChangeActivityCYAController.onSubmit))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    Redirect(controllers.routes.IndexController.onPageLoad.url)
  }

}
