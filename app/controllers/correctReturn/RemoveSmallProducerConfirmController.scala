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

import controllers.actions._
import controllers.{ControllerHelper, routes}
import forms.correctReturn.RemoveSmallProducerConfirmFormProvider
import handlers.ErrorHandler
import models.Mode
import models.SelectChange.CorrectReturn
import navigation._
import pages.correctReturn.RemoveSmallProducerConfirmPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.RemoveSmallProducerConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveSmallProducerConfirmController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForCorrectReturn,
                                       controllerActions: ControllerActions,
                                       formProvider: RemoveSmallProducerConfirmFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RemoveSmallProducerConfirmView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode, sdil: String): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveSmallProducerConfirmPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val smallProducerList = request.userAnswers.smallProducerList
      val smallProducerMissing = !smallProducerList.exists(producer => producer.sdilRef == sdil)

      if(smallProducerMissing && smallProducerList.nonEmpty){
        Redirect(routes.IndexController.onPageLoad)
      }else{
        val smallProducerName = smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
        Ok(view(preparedForm, mode, sdil, smallProducerName))
     }
  }

  def onSubmit(mode: Mode, sdil: String): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>
      val smallProducerName = request.userAnswers.smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, sdil, smallProducerName))),
        formData =>{
          if(formData) {
            val updatedAnswers = request.userAnswers.set(RemoveSmallProducerConfirmPage, formData)
            val modifiedProducerList = request.userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdil)
            val updatedAnswersFinal = updatedAnswers.get.copy(smallProducerList = modifiedProducerList)
            updateDatabaseAndRedirect(updatedAnswersFinal, RemoveSmallProducerConfirmPage, mode)
          }else {
            val updatedAnswers = request.userAnswers.set(RemoveSmallProducerConfirmPage, formData)
            updateDatabaseAndRedirect(updatedAnswers, RemoveSmallProducerConfirmPage, mode)
          }
        }
      )
  }
}
