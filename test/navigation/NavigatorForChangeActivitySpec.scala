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
import controllers.changeActivity.routes
import generators.ChangeActivityCYAGenerators.APAnswers.{Large, Small}
import models._
import models.changeActivity.AmountProduced
import pages._
import pages.changeActivity.{AmountProducedPage, HowManyImportsPage, RemoveWarehouseDetailsPage, SecondaryWarehouseDetailsPage}
import play.api.libs.json.Json

class NavigatorForChangeActivitySpec extends SpecBase {

  val navigator = new NavigatorForChangeActivity

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.ChangeActivity, contactAddress = contactAddress)) mustBe defaultCall
      }

      s"must navigate back to $SecondaryWarehouseDetailsPage when either choice is selected" in {
        val result = navigator.nextPage(RemoveWarehouseDetailsPage, NormalMode,
          UserAnswers("id", SelectChange.ChangeActivity, Json.obj(RemoveWarehouseDetailsPage.toString -> "true"), contactAddress = contactAddress))
        result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad
      }
    }

    "in Check mode" - {

      "must navigate from the how many imports page correctly" - {
        def navigateFromHowManyImportsPage(userAnswers: UserAnswers,mode: Mode = CheckMode) =
          navigator.nextPage(HowManyImportsPage, mode, userAnswers)

          "to the checkAnswers page when the imports page has already been populated and the user is a large producer" in {
            val result = navigateFromHowManyImportsPage(
              emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(HowManyImportsPage , LitresInBands(10L, 10L)).success.value
                .set(SecondaryWarehouseDetailsPage, true).success.value
            )
            result mustBe routes.ChangeActivityCYAController.onPageLoad
          }

          "to the checkAnswers page when the imports page has already been populated and the user is a small producer" in {
            val result = navigateFromHowManyImportsPage(
              emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Small).success.value
                .set(HowManyImportsPage , LitresInBands(10L, 10L)).success.value
                .set(SecondaryWarehouseDetailsPage, true).success.value
            )
            result mustBe routes.ChangeActivityCYAController.onPageLoad
          }

          "the secondary imports details page when the user has not answered the secondary imports page amd is a small producer" in {
            val result = navigateFromHowManyImportsPage(
              emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Small).success.value
                .set(HowManyImportsPage , LitresInBands(10L, 10L)).success.value
            )
            result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad
          }

          "to the checkAnswers page when the imports page has already been populated and the user is not a producer" in {
            val result = navigateFromHowManyImportsPage(
              emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(HowManyImportsPage , LitresInBands(10L, 10L)).success.value
                .set(SecondaryWarehouseDetailsPage, true).success.value
            )
            result mustBe routes.ChangeActivityCYAController.onPageLoad
          }

          "the secondary imports details page when the user has not answered the secondary imports page amd is not a producer" in {
            val result = navigateFromHowManyImportsPage(
              emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(HowManyImportsPage , LitresInBands(10L, 10L)).success.value
            )
            result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad
          }
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }
  }
}
