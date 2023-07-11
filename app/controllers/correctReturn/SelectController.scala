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

package controllers.correctReturn

import cats.implicits.catsSyntaxPartialOrder
import connectors.SoftDrinksIndustryLevyConnector
import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.SelectFormProvider
import handlers.ErrorHandler
import models.SelectChange.CorrectReturn
import models.{Mode, ReturnPeriod}
import navigation._
import pages.correctReturn.SelectPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.SelectView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  val sessionService: SessionService,
                                  val navigator: NavigatorForCorrectReturn,
                                  controllerActions: ControllerActions,
                                  formProvider: SelectFormProvider,
                                  connector: SoftDrinksIndustryLevyConnector,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: SelectView,
                                  val genericLogger: GenericLogger,
                                  val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def seperateReturnYears(returns:List[ReturnPeriod]): List[List[ReturnPeriod]] ={
    returns.map(aReturn => aReturn.year match {
      case year => returns.filter(_.year == year)
    }).distinct
      .sortWith(_.map(returnPeriod => returnPeriod.year) > _.map(returnPeriod => returnPeriod.year))
      .map(returnPeriod => returnPeriod.sortWith(_.quarter > _.quarter).reverse)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(SelectPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      connector.returns_variable(request.subscription.utr).flatMap {
        case Some(returns) if returns.nonEmpty =>
          Future.successful(Ok(view(preparedForm, mode, seperateReturnYears(returns: List[ReturnPeriod]))))
        case _ =>
          Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>

     connector.returns_variable(request.subscription.utr).flatMap{
       case Some(returns) if returns.nonEmpty =>
         Future.successful(BadRequest(view(formWithErrors, mode, seperateReturnYears(returns): List[List[ReturnPeriod]])))
       case Some(returns) if returns.isEmpty =>
         Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
       case _ =>
         Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
     },
        value => {
          val updatedAnswers = request.userAnswers.set(SelectPage, value)
          updateDatabaseAndRedirect(updatedAnswers, SelectPage, mode)
        }
      )
  }
}
