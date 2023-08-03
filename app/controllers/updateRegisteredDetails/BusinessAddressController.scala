package controllers.updateRegisteredDetails

import controllers.actions._
import models.{Mode, NormalMode}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.BusinessAddressView
import models.SelectChange.UpdateRegisteredDetails
import navigation.NavigatorForUpdateRegisteredDetails
import pages.updateRegisteredDetails.BusinessAddressPage
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.helper.form
import views.summary.updateRegisteredDetails.BusinessAddressSummary

class BusinessAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           controllerActions: ControllerActions,
                                           val navigator: NavigatorForUpdateRegisteredDetails,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: BusinessAddressView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>

      val summaryList: Option[SummaryList] = request.userAnswers.warehouseList match {
        case warehouseList if warehouseList.nonEmpty => Some(SummaryListViewModel(
          rows =  BusinessAddressSummary.row2(warehouseList))
        )
        case _ => None
      }

//      Ok(view(preparedForm, mode, summaryList))
//      val businessAddress: SummaryList = SummaryListViewModel(
//        rows = BusinessAddressSummary.row2(request.userAnswers.)
//      )

      Ok(view(mode, summaryList))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>
      Redirect(navigator.nextPage(BusinessAddressPage, NormalMode, request.userAnswers))
  }

}
