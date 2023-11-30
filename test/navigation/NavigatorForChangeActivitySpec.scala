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
import models._
import models.changeActivity.AmountProduced
import pages._
import pages.changeActivity._
import play.api.libs.json.Json

class NavigatorForChangeActivitySpec extends SpecBase {

  val navigator = new NavigatorForChangeActivity

//  TODO: IMPLEMENT FOR ALL

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.ChangeActivity, contactAddress = contactAddress)) mustBe defaultCall
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe routes.ChangeActivityCYAController.onPageLoad
      }

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
          result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
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
          result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode)
        }
      }
    }

    "Amount produced" - {
      "In normal mode" - {

      }
    }

    "Third party packagers" - {
      def navigateFromThirdPartyPackagers(mode: Mode) =
        navigator.nextPage(ThirdPartyPackagersPage, mode,
          emptyUserAnswersForChangeActivity.set(ThirdPartyPackagersPage, true).success.value)

      "navigate to claim credits for lost/damaged in NormalMode" in {
        val result = navigateFromThirdPartyPackagers(NormalMode)
        result mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
      }

      "navigate to Check Your Answers page in CheckMode" in {
        val result = navigateFromThirdPartyPackagers(CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }

    "Operate packaging sites own brands" - {

    }

    "How many operate packaging sites own brands" - {
      def navigateFromHowManyOperatePackagingSiteOwnBrands(mode: Mode) =
        navigator.nextPage(HowManyOperatePackagingSiteOwnBrandsPage, mode,
          emptyUserAnswersForChangeActivity.set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(1, 1)).success.value)

      "navigate to claim credits for lost/damaged in NormalMode" in {
        val result = navigateFromHowManyOperatePackagingSiteOwnBrands(NormalMode)
        result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
      }

      "navigate to Check Your Answers page in CheckMode" in {
        val result = navigateFromHowManyOperatePackagingSiteOwnBrands(CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }

    "Contract packing" - {

    }

    "How many contract packing" - {
      def navigateFromHowManyContractPacking(mode: Mode) =
        navigator.nextPage(HowManyContractPackingPage, mode,
          emptyUserAnswersForChangeActivity.set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value)

      "navigate to claim credits for lost/damaged in NormalMode" in {
        val result = navigateFromHowManyContractPacking(NormalMode)
        result mustBe routes.ImportsController.onPageLoad(NormalMode)
      }

      "navigate to Check Your Answers page in CheckMode" in {
        val result = navigateFromHowManyContractPacking(CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }

    "Imports" - {

    }

    "How many imports" - {

    }

    "Remove packaging site confirm" - {
      def navigateFromRemovePackagingSiteConfirm(value: Boolean, mode: Mode) = {
        navigator.nextPage(RemovePackagingSiteDetailsPage, mode,
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packingSiteMap)
            .set(PackagingSiteDetailsPage, value).success.value
        )
      }

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to Packaging Site Details in $mode" in {
          val result = navigateFromRemovePackagingSiteConfirm(value = true, mode)
          result mustBe routes.PackagingSiteDetailsController.onPageLoad(mode)
        }

        s"select No to navigate to Packaging Site Details in $mode" in {
          val result = navigateFromRemovePackagingSiteConfirm(value = false, mode)
          result mustBe routes.PackagingSiteDetailsController.onPageLoad(mode)
        }
      })
    }

    "Remove warehouse confirm" - {
      def navigateFromRemoveWarehouseConfirm(value: Boolean, mode: Mode) = {
        navigator.nextPage(RemoveWarehouseDetailsPage, mode,
          emptyUserAnswersForChangeActivity
            .copy(warehouseList = twoWarehouses)
            .set(RemoveWarehouseDetailsPage, value).success.value
        )
      }

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to Secondary Warehouse Details in $mode" in {
          val result = navigateFromRemoveWarehouseConfirm(value = true, mode)
          result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
        }

        s"select No to navigate to Secondary Warehouse Details in $mode" in {
          val result = navigateFromRemoveWarehouseConfirm(value = false, mode)
          result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(mode)
        }
      })
    }

    "Suggest deregistration" - {
      "In normal mode" - {
        "should navigate to cancel registration reason" in {
          val result = navigator.nextPage(SuggestDeregistrationPage, NormalMode, emptyUserAnswersForChangeActivity)
          result mustBe controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode)
        }
      }
    }
  }
}