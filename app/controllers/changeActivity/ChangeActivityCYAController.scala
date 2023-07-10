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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.summary.changeActivity.AmountProducedSummary
import views.html.changeActivity.ChangeActivityCYAView
import models.SelectChange.ChangeActivity
import views.summary.changeActivity.{ContractPackingSummary, ImportsSummary, OperatePackagingSiteOwnBrandsSummary}

class ChangeActivityCYAController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            controllerActions: ControllerActions,
                                            implicit val config: FrontendAppConfig,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ChangeActivityCYAView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    implicit request =>
      val amountProducedSection: Seq[(String, SummaryList)] = Seq(
        "changeActivity.checkYourAnswers.amountProducedSection" -> SummaryList(Seq(AmountProducedSummary.row(request.userAnswers)).flatten)
      )
      val thirdPartyPackagersSection: Seq[(String, SummaryList)] = Seq.empty
      val operatePackingSiteOwnBrandsSection: Seq[(String, SummaryList)] = Seq(
        "changeActivity.checkYourAnswers.operatePackingSiteOwnBrandsSection" ->
          OperatePackagingSiteOwnBrandsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      )
      val contractPackingSection: Seq[(String, SummaryList)] = Seq(
        "changeActivity.checkYourAnswers.contractPackingSection" ->
          ContractPackingSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      )
      val importsSection: Seq[(String, SummaryList)] = Seq(
        "changeActivity.checkYourAnswers.importsSection" ->
          ImportsSummary.summaryList(request.userAnswers, isCheckAnswers = true, includeLevyRows = false)
      )

      val alias: String = request.subscription.orgName
//      TODO: Implement Return Period in DLS-8346
      val returnPeriod: String = "RETURN PERIOD"
      val sections = amountProducedSection ++ thirdPartyPackagersSection ++ operatePackingSiteOwnBrandsSection ++ contractPackingSection ++ importsSection

      Ok(view(alias, returnPeriod, sections, routes.ChangeActivityCYAController.onSubmit))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(ChangeActivity) {
    Redirect(controllers.routes.IndexController.onPageLoad.url)
  }

}
