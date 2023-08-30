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
import controllers.cancelRegistration.routes
import models._
import models.enums.VariationJourneyTypes.cancelRegistration
import pages._
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import play.api.libs.json.Json

class NavigatorForCancelRegistrationSpec extends SpecBase {

  val navigator = new NavigatorForCancelRegistration

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.CancelRegistration, contactAddress = contactAddress)) mustBe defaultCall
      }

      s"must go from $ReasonPage to $CancelRegistrationDatePage" in {
        val result = navigator.nextPage(ReasonPage, NormalMode,
          UserAnswers("id", SelectChange.CancelRegistration, Json.obj(ReasonPage.toString -> "I don't want to anymore"), contactAddress = contactAddress))
        result mustBe routes.CancelRegistrationDateController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CancelRegistration,
          contactAddress = contactAddress)) mustBe routes.CancelRegistrationCYAController.onPageLoad
      }

      s"must go from $ReasonPage to CYA page" in {
        val result = navigator.nextPage(ReasonPage, CheckMode,
          UserAnswers("id", SelectChange.CancelRegistration, Json.obj(ReasonPage.toString -> "I don't want to anymore"), contactAddress = contactAddress))
        result mustBe routes.CancelRegistrationCYAController.onPageLoad
      }
    }
  }
}
