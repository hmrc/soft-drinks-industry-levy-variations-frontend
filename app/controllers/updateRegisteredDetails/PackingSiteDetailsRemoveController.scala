package controllers.updateRegisteredDetails

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.updateRegisteredDetails.PackingSiteDetailsRemoveFormProvider
import javax.inject.Inject
import models.Mode
import pages.updateRegisteredDetails.PackingSiteDetailsRemovePage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.updateRegisteredDetails.PackingSiteDetailsRemoveView
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}
import navigation._

class PackingSiteDetailsRemoveController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorForUpdateRegisteredDetails,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PackingSiteDetailsRemoveFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackingSiteDetailsRemoveView,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PackingSiteDetailsRemovePage) match {
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
          val updatedAnswers = request.userAnswers.set(PackingSiteDetailsRemovePage, value)
          updateDatabaseAndRedirect(updatedAnswers, PackingSiteDetailsRemovePage, mode)
        }
      )
  }
}
