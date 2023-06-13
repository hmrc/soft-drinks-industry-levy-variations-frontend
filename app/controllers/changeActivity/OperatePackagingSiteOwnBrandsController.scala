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

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.changeActivity.OperatePackagingSiteOwnBrandsFormProvider
import javax.inject.Inject
import models.Mode
import pages.changeActivity.{OperatePackagingSiteOwnBrandsPage, HowManyOperatePackagingSiteOwnBrandsPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.changeActivity.OperatePackagingSiteOwnBrandsView
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}
import navigation._

class OperatePackagingSiteOwnBrandsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForChangeActivity,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: OperatePackagingSiteOwnBrandsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: OperatePackagingSiteOwnBrandsView,
                                          val genericLogger: GenericLogger,
                                          val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(OperatePackagingSiteOwnBrandsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.setAndRemoveLitresIfReq(OperatePackagingSiteOwnBrandsPage, HowManyOperatePackagingSiteOwnBrandsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, OperatePackagingSiteOwnBrandsPage, mode)
          }
      )
  }
}