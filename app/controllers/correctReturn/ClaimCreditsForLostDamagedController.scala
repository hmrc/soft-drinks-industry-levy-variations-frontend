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
import forms.correctReturn.ClaimCreditsForLostDamagedFormProvider
import handlers.ErrorHandler
import models.{Mode, SdilReturn}
import navigation._
import pages.correctReturn.{ClaimCreditsForLostDamagedPage, HowManyCreditsForLostDamagedPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.{GenericLogger, UserTypeCheck}
import views.html.correctReturn.ClaimCreditsForLostDamagedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimCreditsForLostDamagedController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorForCorrectReturn,
                                         controllerActions: ControllerActions,
                                         formProvider: ClaimCreditsForLostDamagedFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ClaimCreditsForLostDamagedView,
                                          val genericLogger: GenericLogger,
                                          val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val preparedForm = request.userAnswers.get(ClaimCreditsForLostDamagedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      val subscription = request.subscription
      println(Console.YELLOW + subscription + Console.WHITE)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.setAndRemoveLitresIfReq(ClaimCreditsForLostDamagedPage, HowManyCreditsForLostDamagedPage, value)
          updateDatabaseWithoutRedirect(updatedAnswers, ClaimCreditsForLostDamagedPage)


          if (value) {
            Future.successful(Redirect(routes.HowManyCreditsForLostDamagedController.onPageLoad(mode).url))
          } else {
            if (UserTypeCheck.isNewPacker(SdilReturn.apply(request.userAnswers), subscription) || UserTypeCheck.isNewImporter(
              SdilReturn.apply(request.userAnswers), subscription)) {
              Future.successful(Redirect(routes.ReturnChangeRegistrationController.onPageLoad().url))
            } else {
              Future.successful(Redirect(routes.CorrectReturnCYAController.onPageLoad.url))
            }
          }
        }
      )
    }
}
