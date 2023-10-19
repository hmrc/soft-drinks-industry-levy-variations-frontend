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

package navigation

import base.SpecBase
import pages._
import models._
import controllers.updateRegisteredDetails.routes
import models.updateRegisteredDetails.ChangeRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails.Sites
import pages.updateRegisteredDetails.ChangeRegisteredDetailsPage

class NavigatorForUpdateRegisteredDetailsSpec extends SpecBase {

  val navigator = new NavigatorForUpdateRegisteredDetails

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress)) mustBe defaultCall
      }

      "must go from ChangeRegisteredDetailsPage to WarehouseDetailsPage " - {
        "when user selects Sites on ChangeRegisteredDetailsPage and packaging site list is empty" in {
          val values: Seq[ChangeRegisteredDetails] = Seq(Sites)
          val changeSiteDetails: Seq[ChangeRegisteredDetails] = ChangeRegisteredDetails.values
          val userAnswersWithSitesSelectedToChange: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
            .set(ChangeRegisteredDetailsPage, changeSiteDetails).success.value
            .copy(packagingSiteList = Map.empty)

          navigator.nextPage(ChangeRegisteredDetailsPage, NormalMode,
            userAnswersWithSitesSelectedToChange
          ) mustBe routes.WarehouseDetailsController.onPageLoad(NormalMode)
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswersForUpdateRegisteredDetails) mustBe routes.UpdateRegisteredDetailsCYAController.onPageLoad
      }
    }
  }
}
