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

package controllers.updateRegisteredDetails

import controllers.ControllerHelper
import controllers.actions._
import handlers.ErrorHandler
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.UpdateContactDetails
import navigation.NavigatorForUpdateRegisteredDetails
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.updateRegisteredDetails.ContactDetailsView
import views.summary.updateRegisteredDetails.ContactDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                        controllerActions: ControllerActions,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ContactDetailsView,
                                       val errorHandler: ErrorHandler,
                                       val navigator: NavigatorForUpdateRegisteredDetails,
                                       val sessionService: SessionService,
                                       val genericLogger: GenericLogger
                                     ) (implicit ec: ExecutionContext) extends ControllerHelper {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails).async {
    implicit request =>

      request.userAnswers.get(UpdateContactDetailsPage) match {
        case Some(contactDetails) => Future.successful(Ok(view(ContactDetailsSummary.rows(contactDetails))))
        case _ =>
          val subscriptionContact = request.subscription.contact
          val updateContactDetails =
            UpdateContactDetails(
              subscriptionContact.name.getOrElse(""),
              subscriptionContact.positionInCompany.getOrElse(""),
              subscriptionContact.phoneNumber,
              subscriptionContact.email
            )
          val updatedAnswers = request.userAnswers.set(UpdateContactDetailsPage, updateContactDetails)
          updateDatabaseWithoutRedirect(updatedAnswers, UpdateContactDetailsPage).flatMap {
            case true => Future.successful(Ok(view(ContactDetailsSummary.rows(updateContactDetails))))
            case false => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }
      }
  }

}
