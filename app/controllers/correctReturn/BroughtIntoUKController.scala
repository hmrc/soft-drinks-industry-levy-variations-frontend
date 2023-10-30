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

import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.BroughtIntoUKFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation._
import pages.correctReturn.{BroughtIntoUKPage, HowManyBroughtIntoUKPage, IsImporterPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.BroughtIntoUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BroughtIntoUKController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForCorrectReturn,
                                         controllerActions: ControllerActions,
                                         formProvider: BroughtIntoUKFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: BroughtIntoUKView,
                                          val genericLogger: GenericLogger,
                                          val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>
//      TODO: Remove need for this - pass subscription into Navigator in way that does not affect too many other files
      val setIsImporterFromSubscription = request.userAnswers.set(IsImporterPage, request.subscription.activity.importer)
      updateDatabaseWithoutRedirect(setIsImporterFromSubscription, IsImporterPage)
      val preparedForm = request.userAnswers.get(BroughtIntoUKPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        //        TODO: Pass in request.subscription for navigation
        value => {
          val updatedAnswers = request.userAnswers.setAndRemoveLitresIfReq(BroughtIntoUKPage, HowManyBroughtIntoUKPage, value)
          updateDatabaseAndRedirect(updatedAnswers, BroughtIntoUKPage, mode)
          }
      )
  }
}
