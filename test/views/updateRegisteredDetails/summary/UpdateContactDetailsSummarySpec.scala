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

package views.updateRegisteredDetails.summary

import base.SpecBase
import controllers.updateRegisteredDetails.routes
import models.updateRegisteredDetails.ContactDetails
import models.{CheckMode, SelectChange, UserAnswers}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.govuk.all.FluentActionItem
import viewmodels.implicits._
import views.summary.updateRegisteredDetails.UpdateContactDetailsSummary

class UpdateContactDetailsSummarySpec extends SpecBase {

  "rows" - {
    "should return nothing when user answers is empty" in {
      UpdateContactDetailsSummary.rows(UserAnswers("", SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress)) mustBe None
    }

    s"should return correct rows when $UpdateContactDetailsPage is present and isCheckAnswers is true" in {
      val contactDetails = ContactDetails("foo","bar", "wizz", "bang")
      val res = UpdateContactDetailsSummary.rows(
        UserAnswers("", SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress).set(UpdateContactDetailsPage, contactDetails).success.value).get
      res._1 mustBe "Soft Drinks Industry Levy contact"

      res._2 mustBe SummaryList(
        Seq(SummaryListRow(
<<<<<<< HEAD
          key = s"updateRegisteredDetails.updateContactDetails.fullName",
          value = Value(Text(contactDetails.fullName)),
            actions = Some(Actions(
            items = Seq(ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden")))
          ))
        ),
=======
        key = s"updateRegisteredDetails.updateContactDetails.fullName",
        value = Value(Text(contactDetails.fullName)),
          actions = Some(Actions(
          items = Seq(ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
            .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden"))
            .withAttribute("id", "change-contactDetailsAdd"))
        )
      )),
>>>>>>> 0f8398ab (DLS-9358 add id attribute to view spec tests)
        SummaryListRow(
          key = s"updateRegisteredDetails.updateContactDetails.position",
          value = Value(Text(contactDetails.position)),
          actions = Some(Actions(
            items = Seq(ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
<<<<<<< HEAD
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden")))
          ))
=======
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden"))
              .withAttribute("id", "change-contactDetailsAdd")))
          )
>>>>>>> 0f8398ab (DLS-9358 add id attribute to view spec tests)
        ),
        SummaryListRow(
          key = s"updateRegisteredDetails.updateContactDetails.phoneNumber",
          value = Value(Text(contactDetails.phoneNumber)),
          actions = Some(Actions(
            items = Seq(ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
<<<<<<< HEAD
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden")))
          ))
=======
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden"))
              .withAttribute("id", "change-contactDetailsAdd")))
          )
>>>>>>> 0f8398ab (DLS-9358 add id attribute to view spec tests)
        ),
        SummaryListRow(
          key = s"updateRegisteredDetails.updateContactDetails.email",
          value = Value(Text(contactDetails.email)),
          actions = Some(Actions(
            items = Seq(ActionItem(routes.UpdateContactDetailsController.onPageLoad(CheckMode).url, "site.change")
              .withVisuallyHiddenText(messages(application)("updateRegisteredDetails.updateContactDetails.change.hidden"))
              .withAttribute("id", "change-contactDetailsAdd"))
            )
          ))
        )
      )
    }

    s"should return correct rows when $UpdateContactDetailsPage is present and isCheckAnswers is false" in {
      val contactDetails = ContactDetails("foo", "bar", "wizz", "bang")
      val res = UpdateContactDetailsSummary.rows(
        UserAnswers("", SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress).set(UpdateContactDetailsPage, contactDetails).success.value,
        isCheckAnswers = false
      ).get
      res._1 mustBe "Soft Drinks Industry Levy contact"

      res._2 mustBe SummaryList(
        Seq(
          SummaryListRow(
            key = s"updateRegisteredDetails.updateContactDetails.fullName",
            value = Value(Text(contactDetails.fullName)),
            actions = Some(Actions("", Seq.empty))
          ),
          SummaryListRow(
            key = s"updateRegisteredDetails.updateContactDetails.position",
            value = Value(Text(contactDetails.position)),
            actions = Some(Actions("", Seq.empty))
          ),
          SummaryListRow(
            key = s"updateRegisteredDetails.updateContactDetails.phoneNumber",
            value = Value(Text(contactDetails.phoneNumber)),
            actions = Some(Actions("", Seq.empty))
          ),
          SummaryListRow(
            key = s"updateRegisteredDetails.updateContactDetails.email",
            value = Value(Text(contactDetails.email)),
            actions = Some(Actions("", Seq.empty))
          )
        ))
    }
  }

}
