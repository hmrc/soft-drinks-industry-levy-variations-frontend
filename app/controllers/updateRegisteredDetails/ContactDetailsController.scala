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

import controllers.actions._
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.UpdateContactDetails
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.updateRegisteredDetails.ContactDetailsView
import views.summary.updateRegisteredDetails.ContactDetailsSummary

import javax.inject.Inject

class ContactDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                        controllerActions: ControllerActions,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ContactDetailsView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData(UpdateRegisteredDetails) {
    implicit request =>

      val contact =
        request.userAnswers.get(UpdateContactDetailsPage) match {
          case Some(contactDetails) => contactDetails
          case _ =>
            val retrievedContact = request.subscription.contact
            UpdateContactDetails(
              retrievedContact.name.getOrElse(""),
              retrievedContact.positionInCompany.getOrElse(""),
              retrievedContact.phoneNumber,
              retrievedContact.email
            )
        }

      val summaryList = ContactDetailsSummary.rows(contact)

      Ok(view(summaryList))
  }
}
