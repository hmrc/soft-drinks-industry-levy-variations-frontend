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
import models.correctReturn.AddASmallProducer
import models.errors.{AlreadyExists, NotASmallProducer, SDILReferenceErrors, UnexpectedResponseFromCheckSmallProducer}
import models.{LitresInBands, Mode, ReturnPeriod, SmallProducer, UserAnswers}
import navigation._
import pages.correctReturn.AddASmallProducerPage
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import views.html.correctReturn.AddASmallProducerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
                                           )(implicit val ec: ExecutionContext) extends ControllerHelper {

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData {
    implicit request =>
      val form: Form[AddASmallProducer] = formProvider(request.userAnswers)
      val preparedForm = request.userAnswers.get(AddASmallProducerPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withCorrectReturnJourneyData.async {
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
                BadRequest(view(preparedForm.withError(FormError("referenceNumber",
                  "correctReturn.addASmallProducer.error.referenceNumber.exists")), mode))
              )
            case _ =>
              sdilConnector.checkSmallProducerStatus(value.referenceNumber, request.returnPeriod).value.flatMap {
                case Right(Some(false)) =>
                  Future.successful(
                    BadRequest(view(preparedForm.withError(FormError("referenceNumber",
                      "correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer")), mode))
                  )
                case Right(_) =>
                  val updatedAnswers: UserAnswers = request.userAnswers
                    .copy(smallProducerList = AddASmallProducer.toSmallProducer(value) :: request.userAnswers.smallProducerList)
                  updateDatabaseAndRedirect(updatedAnswers, AddASmallProducerPage, mode)
                case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
              }
          }
        }
      )
  }

  def onEditPageLoad(mode: Mode, sdilReference: String): Action[AnyContent] =
    controllerActions.withCorrectReturnJourneyData.async {
      implicit request =>

        val form = formProvider(request.userAnswers)
        val targetSmallProducer = request.userAnswers.smallProducerList.find(producer => producer.sdilRef == sdilReference)

        targetSmallProducer match {
          case Some(producer) =>
            val addASmallProducer = AddASmallProducer(Some(producer.alias), producer.sdilRef, LitresInBands(producer.litreage.lower,
              producer.litreage.higher))
            val preparedForm = form.fill(addASmallProducer)
            Future.successful(Ok(view(preparedForm, mode, Some(sdilReference))))
          case _ =>
            throw new RuntimeException("No such small producer exists")
        }
    }

  def onEditPageSubmit(mode: Mode, sdilReference: String): Action[AnyContent] =
    controllerActions.withCorrectReturnJourneyData.async {
      implicit request =>

        val form = formProvider(request.userAnswers)

        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, mode, Some(sdilReference))))
          },
          value => {
            val smallProducerList = request.userAnswers.smallProducerList
            val preparedForm = form.fill(value)
            isValidSDILRef(sdilReference, value.referenceNumber, smallProducerList, request.returnPeriod).flatMap({
              case Left(AlreadyExists) =>
                Future.successful(
                  BadRequest(view(preparedForm.withError(
                    FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.exists")), mode, Some(sdilReference)))
                )
              case Left(NotASmallProducer) =>
                Future.successful(
                  BadRequest(view(preparedForm.withError(
                    FormError("referenceNumber", "correctReturn.addASmallProducer.error.referenceNumber.notASmallProducer")), mode, Some(sdilReference)))
                )
              case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
              case Right(_) =>
                val newListWithOldSPRemoved = request.userAnswers.smallProducerList.filterNot(_.sdilRef == sdilReference)
                val updatedAnswers: UserAnswers = request.userAnswers
                  .copy(smallProducerList = AddASmallProducer.toSmallProducer(value) :: newListWithOldSPRemoved)
                updateDatabaseAndRedirect(updatedAnswers, AddASmallProducerPage, mode)
            })
          }
        )
    }

  private def isValidSDILRef(currentSDILRef: String, addASmallProducerSDILRef: String,
                             smallProducerList: Seq[SmallProducer], returnPeriod: ReturnPeriod)
                            (implicit hc: HeaderCarrier): Future[Either[SDILReferenceErrors, Unit]] = {
    if (currentSDILRef == addASmallProducerSDILRef) {
      Future.successful(Right(()))
    } else if (smallProducerList.map(_.sdilRef).contains(addASmallProducerSDILRef)) {
      Future.successful(Left(AlreadyExists))
    } else {
      sdilConnector.checkSmallProducerStatus(addASmallProducerSDILRef, returnPeriod).value.flatMap {
        case Right(Some(false)) => Future.successful(Left(NotASmallProducer))
        case Right(_) => Future.successful(Right(()))
        case Left(_) => Future.successful(Left(UnexpectedResponseFromCheckSmallProducer))
      }
    }
  }
}
