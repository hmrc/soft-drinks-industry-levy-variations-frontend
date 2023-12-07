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
import controllers.ControllerHelper
import forms.correctReturn.RemoveSmallProducerConfirmFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
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
                                     )(implicit val ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode, sdilRef: String): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>

      val smallProducerToRemove = request.userAnswers.smallProducerList.find(smallProducer => smallProducer.sdilRef == sdilRef)
      smallProducerToRemove match {
        case None =>
          genericLogger.logger.warn(s"Small Producer sdilRef $sdilRef doesn't exist for ${request.userAnswers.id}")
          if(request.userAnswers.smallProducerList.size >= 1){
            Redirect(routes.SmallProducerDetailsController.onPageLoad(mode))
          }else{
            Redirect(routes.ExemptionsForSmallProducersController.onPageLoad(mode))
          }
        case Some(smallProducer) => Ok(view(form, mode, sdilRef, smallProducer.alias))
      }
  }

  def onSubmit(mode: Mode, sdilRef: String): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
    implicit request =>
      val smallProducerToRemove = request.userAnswers.smallProducerList.find(smallProducer => smallProducer.sdilRef == sdilRef)
      smallProducerToRemove match {
        case None =>
          genericLogger.logger.warn(s"Small Producer sdilRef $sdilRef doesn't exist for ${request.userAnswers.id}")
          Future.successful(Redirect(routes.SmallProducerDetailsController.onPageLoad(mode)))
        case Some(smallProducer) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, sdilRef, smallProducer.alias))),
            value => {
              val updatedUserAnswers = if (!value) request.userAnswers else {
                val modifiedProducerList = request.userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdilRef)
                request.userAnswers.copy(smallProducerList = modifiedProducerList)
              }
              updateDatabaseAndRedirect(updatedUserAnswers, RemoveSmallProducerConfirmPage, mode)
            })
      }
  }
}
