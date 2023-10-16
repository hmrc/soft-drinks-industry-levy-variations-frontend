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
import models._
import pages._
import pages.correctReturn.RepaymentMethodPage

class NavigatorForCorrectReturnSpec extends SpecBase {

  val navigator = new NavigatorForCorrectReturn

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe defaultCall
      }

      "must go from repayment method page to check changes page" in {
        navigator.nextPage(RepaymentMethodPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
          contactAddress)) mustBe controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad
      }
    }
  }
}
