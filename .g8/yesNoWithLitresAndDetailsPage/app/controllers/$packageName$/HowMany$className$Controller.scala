package controllers.$packageName$

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.HowManyLitresFormProvider
import javax.inject.Inject
import models.Mode
import models.SelectChange.$packageName;format="cap"$
import pages.$packageName$.HowMany$className$Page
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.$packageName$.HowMany$className$View
import handlers.ErrorHandler
import navigation._

import scala.concurrent.{ExecutionContext, Future}

class HowMany$className$Controller @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionService: SessionService,
                                         val navigator: NavigatorFor$packageName;format="cap"$,
                                         controllerActions: ControllerActions,
                                         formProvider: HowManyLitresFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: HowMany$className$View,
                                         val genericLogger: GenericLogger,
                                         val errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData($packageName;format="cap"$) {
    implicit request =>

      val preparedForm = request.userAnswers.get(HowMany$className$Page) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData($packageName;format="cap"$).async {
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
