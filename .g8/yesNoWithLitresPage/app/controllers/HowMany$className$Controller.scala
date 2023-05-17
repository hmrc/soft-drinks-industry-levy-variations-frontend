package controllers

import controllers.actions._
import forms.HowManyLitresFormProvider
import javax.inject.Inject
import models.Mode
import pages.HowMany$className$Page
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.HowMany$className$View
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class HowMany$className;format="cap"$Controller @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HowManyLitresFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: HowMany$className$View,
                                         val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(HowMany$className$Page) match {
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
          val updatedAnswers = request.userAnswers.set(HowMany$className$Page, value)
          updateDatabaseAndRedirect(updatedAnswers, HowMany$className$Page, mode)
        }
      )
  }
}
