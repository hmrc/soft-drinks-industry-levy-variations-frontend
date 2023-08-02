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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.ControllerHelper
import controllers.actions._
import forms.correctReturn.AddASmallProducerFormProvider
import handlers.ErrorHandler
import models.{Mode, SmallProducer, UserAnswers}
import models.SelectChange.CorrectReturn
import models.correctReturn.AddASmallProducer
import navigation._
import pages.correctReturn.{AddASmallProducerPage, SelectPage}
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.correctReturn.AddASmallProducerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddASmallProducerController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             val sessionService: SessionService,
                                             val navigator: NavigatorForCorrectReturn,
                                             controllerActions: ControllerActions,
                                             sdilConnector: SoftDrinksIndustryLevyConnector,
                                             formProvider: AddASmallProducerFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: AddASmallProducerView,
                                             val genericLogger: GenericLogger,
                                             val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn) {
    implicit request =>

      request.userAnswers.get(SelectPage) match {
        case Some(_) =>
          val form: Form[AddASmallProducer] = formProvider(request.userAnswers)
          val preparedForm = request.userAnswers.get(AddASmallProducerPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode))
        case _ =>
          genericLogger.logger.warn(s"Return period has not been set for correct return journey for ${request.userAnswers.id}")
          Redirect(controllers.routes.IndexController.onPageLoad.url)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(CorrectReturn).async {
    implicit request =>

      val form: Form[AddASmallProducer] = formProvider(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val smallProducerList: List[SmallProducer] = request.userAnswers.smallProducerList
          val smallProducerOpt: Option[SmallProducer] = smallProducerList.find(smallProducer => smallProducer.sdilRef == value.referenceNumber)
          val preparedForm = form.fill(value)
          smallProducerOpt match {
            case Some(_) =>
              Future.successful(
                BadRequest(view(preparedForm.withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.exists")), mode))
              )
            case _ =>
              request.userAnswers.get(SelectPage) match {
                case Some(returnPeriod) =>
                  sdilConnector.checkSmallProducerStatus(value.referenceNumber, returnPeriod).flatMap {
                    case Some(false) =>
                      Future.successful(
                        BadRequest(view(preparedForm.withError(FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer")), mode))
                      )
                    case _ =>
                      val userAnswersSetPage: Try[UserAnswers] = request.userAnswers.set(AddASmallProducerPage, value)
                      val updatedAnswers: Try[UserAnswers] = userAnswersSetPage
                        .map(userAnswers => userAnswers.copy(smallProducerList = AddASmallProducer.toSmallProducer(value) :: userAnswers.smallProducerList))
                      updateDatabaseAndRedirect(updatedAnswers, AddASmallProducerPage, mode)
                  }
                case _ =>
                  genericLogger.logger.warn(s"Return period has not been set for correct return journey for ${request.userAnswers.id}")
                  Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))
              }
          }
        }
      )
  }
}