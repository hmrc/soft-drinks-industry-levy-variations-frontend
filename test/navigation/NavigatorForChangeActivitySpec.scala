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

class NavigatorForChangeActivitySpec extends SpecBase {

  val navigator = new NavigatorForChangeActivity

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
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)
        ) mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }

    "Amount produced" - {
      "select Large to navigate to Operate Packaging Site Own Brands in NormalMode" in {
        val result = navigator.nextPage(AmountProducedPage, NormalMode,
          emptyUserAnswersForChangeActivity.set(AmountProducedPage, AmountProduced.Large).success.value)
        result mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
      }

      "select Small to navigate to Third Party Packagers in NormalMode" in {
        val result = navigator.nextPage(AmountProducedPage, NormalMode,
          emptyUserAnswersForChangeActivity.set(AmountProducedPage, AmountProduced.Small).success.value)
        result mustBe routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
      }

      "select None to navigate to Contract Packing in NormalMode" in {
        val result = navigator.nextPage(AmountProducedPage, NormalMode,
          emptyUserAnswersForChangeActivity.set(AmountProducedPage, AmountProduced.None).success.value)
        result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
      }

      def navigateFromAmountProduced(amountProduced: AmountProduced, mode: Mode, previousAmountProduced: AmountProduced) =
        navigator.nextPage(AmountProducedPage, mode,
          emptyUserAnswersForChangeActivity.set(AmountProducedPage, amountProduced).success.value,
          amountProduced = Option(previousAmountProduced))

      List(NormalMode, CheckMode).foreach(mode => {
        AmountProduced.values.foreach(previousAmountProduced => {
          AmountProduced.values.foreach(newAmountProduced => {
            if (newAmountProduced == previousAmountProduced && mode == CheckMode) {
              s"select $newAmountProduced when $previousAmountProduced previously selected to navigate to CYA in CheckMode" in {
                val result = navigateFromAmountProduced(newAmountProduced, mode, previousAmountProduced)
                result mustBe routes.ChangeActivityCYAController.onPageLoad
              }
            } else {
              newAmountProduced match {
                case AmountProduced.Large =>
                  s"select $newAmountProduced when $previousAmountProduced previously selected in $mode " +
                    s"to navigate to Operate Packaging Site Own Brands in NormalMode" in {
                    val result = navigateFromAmountProduced(AmountProduced.Large, mode, previousAmountProduced)
                    result mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode)
                  }
                case AmountProduced.Small =>
                  s"select $newAmountProduced when $previousAmountProduced previously selected in $mode " +
                    s"to navigate to Third Party Packagers in NormalMode" in {
                    val result = navigateFromAmountProduced(AmountProduced.Small, mode, previousAmountProduced)
                    result mustBe routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
                  }
                case AmountProduced.None =>
                  s"select $newAmountProduced when $previousAmountProduced previously selected in $mode " +
                    s"to navigate to Contract Packing in NormalMode" in {
                    val result = navigateFromAmountProduced(AmountProduced.None, mode, previousAmountProduced)
                    result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
                  }
              }
            }
          })
        })
      })
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
      def navigateFromOperatePackagingSiteOwnBrandsPage(value: Boolean, mode: Mode) =
        navigator.nextPage(OperatePackagingSiteOwnBrandsPage, mode,
          emptyUserAnswersForChangeActivity.set(OperatePackagingSiteOwnBrandsPage, value).success.value)

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to How Many operate packaging sites own brands in $mode" in {
          val result = navigateFromOperatePackagingSiteOwnBrandsPage(value = true, mode)
          result mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(mode)
        }
      })

      "select No to navigate to contract packing page in NormalMode" in {
        val result = navigateFromOperatePackagingSiteOwnBrandsPage(value = false, NormalMode)
        result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
      }

      "Should navigate to Check Your Answers page when no is selected in CheckMode" in {
        val result = navigateFromOperatePackagingSiteOwnBrandsPage(value = false, CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }
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
      def navigateFromContractPackingPage(value: Boolean, mode: Mode) =
        navigator.nextPage(ContractPackingPage, mode,
          emptyUserAnswersForChangeActivity.set(ContractPackingPage, value).success.value
        )

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to How Many contract packing in $mode" in {
          val result = navigateFromContractPackingPage(value = true, mode)
          result mustBe routes.HowManyContractPackingController.onPageLoad(mode)
        }
      })

      "select No to navigate to imports page in NormalMode" in {
        val result = navigateFromContractPackingPage(value = false, NormalMode)
        result mustBe routes.ImportsController.onPageLoad(NormalMode)
      }

      "Should navigate to Check Your Answers page when no is selected in CheckMode and deregistration not suggested" in {
        val result = navigateFromContractPackingPage(value = false, CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }

      "should navigate to suggest registration in CheckMode when Amount Produced Small, not TPP, not Contract Packing, not Importing" in {
        val result = navigator.nextPage(ContractPackingPage, CheckMode, emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.Small).success.value
          .set(ThirdPartyPackagersPage, false).success.value
          .set(ContractPackingPage, false).success.value
          .set(ImportsPage, false).success.value
        )
        result mustBe routes.SuggestDeregistrationController.onPageLoad
      }

      "should navigate to suggest registration in CheckMode when Amount Produced None, not Contract Packing, not Importing" in {
        val result = navigator.nextPage(ContractPackingPage, CheckMode, emptyUserAnswersForChangeActivity
          .set(AmountProducedPage, AmountProduced.None).success.value
          .set(ContractPackingPage, false).success.value
          .set(ImportsPage, false).success.value
        )
        result mustBe routes.SuggestDeregistrationController.onPageLoad
      }
    }

    "How many contract packing" - {
      def navigateFromHowManyContractPacking(mode: Mode) =
        navigator.nextPage(HowManyContractPackingPage, mode,
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packingSiteMap)
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
            .set(PackagingSiteDetailsPage, false).success.value
        )

      "navigate to claim credits for lost/damaged in NormalMode" in {
        val result = navigateFromHowManyContractPacking(NormalMode)
        result mustBe routes.ImportsController.onPageLoad(NormalMode)
      }

      "navigate to Check Your Answers page in CheckMode" in {
        val result = navigateFromHowManyContractPacking(CheckMode)
        result mustBe routes.ChangeActivityCYAController.onPageLoad
      }
    }

    "Imports True" - {
      def navigateFromImportsPageTrue(mode: Mode) =
        navigator.nextPage(ImportsPage, mode,
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = packingSiteMap)
            .copy(warehouseList = twoWarehouses)
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(ImportsPage, true).success.value.set(ImportsPage, true).success.value)

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to How Many imports in $mode" in {
          val result = navigateFromImportsPageTrue(mode)
          result mustBe routes.HowManyImportsController.onPageLoad(mode)
        }
      })
    }

    "Imports false" - {
      "In CheckMode" - {
        "Should navigate to Check Your Answers page when no is selected" in {
          val result = navigator.nextPage(ImportsPage, CheckMode,
            emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.None).success.value
              .set(ContractPackingPage, true).success.value
              .set(ImportsPage, false).success.value)
          result mustBe routes.ChangeActivityCYAController.onPageLoad
        }
      }
    }

    val userAnswersWithImportsTrueAndHowMany = emptyUserAnswersForChangeActivity
      .set(ImportsPage, true).success.value
      .set(HowManyImportsPage, LitresInBands(1, 1)).success.value

    val userAnswersWithImportsFalse = emptyUserAnswersForChangeActivity
      .set(ImportsPage, false).success.value

    case class FollowingImports(prefix: String, initialUserAnswers: UserAnswers, page: Page)

    List(
      FollowingImports("How Many Imports", userAnswersWithImportsTrueAndHowMany, HowManyImportsPage),
      FollowingImports("Imports False", userAnswersWithImportsFalse, ImportsPage)
    ).foreach(followingImports => {
      s"${followingImports.prefix}" - {
        "in NormalMode" - {
          def navigateFollowingImportsPage(userAnswers: UserAnswers) =
            navigator.nextPage(followingImports.page, NormalMode, userAnswers)

          "when Amount Produced Large" - {
            val initialUserAnswersWithAmountProducedLarge = followingImports.initialUserAnswers
              .set(AmountProducedPage, AmountProduced.Large).success.value

            "navigate to Pack At Business Address if operate own brand packaging sites and not contract packing and packaging sites empty" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(ContractPackingPage, false).success.value
                .copy(packagingSiteList = Map.empty)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
            }

            "navigate to Pack At Business Address if not operate own brand packaging sites and not contract packing and packaging sites empty" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, true).success.value
                .copy(packagingSiteList = Map.empty)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
            }

            "navigate to Packaging Site Details if operate own brand packaging sites and contract packing and packaging sites not empty" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(ContractPackingPage, true).success.value
                .copy(packagingSiteList = packingSiteMap)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }

            "navigate to Packaging Site Details if operate own brand packaging sites and not contract packing and packaging sites not empty" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(ContractPackingPage, false).success.value
                .copy(packagingSiteList = packingSiteMap)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }

            "navigate to Packaging Site Details if not operate own brand packaging sites and contract packing and packaging sites not empty" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, true).success.value
                .copy(packagingSiteList = packingSiteMap)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }

            "navigate to Secondary Warehouse Details if not operate own brand packaging sites and not contract packing" in {
              val userAnswers = initialUserAnswersWithAmountProducedLarge
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, false).success.value
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode)
            }
          }

          "when Amount Produced Small" - {
            val voluntary = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value
              .set(ThirdPartyPackagersPage, true).success.value
              .set(ContractPackingPage, false).success.value
              .set(ImportsPage, false).success.value

            val initialUserAnswersWithAmountProducedSmall = followingImports.initialUserAnswers
              .set(AmountProducedPage, AmountProduced.Small).success.value

            s"And Contract Packing answered and registration is not $voluntary" - {
              s"navigate to $PackAtBusinessAddressPage if $ContractPackingPage is true and packaging site list is empty" in {
                val userAnswers = initialUserAnswersWithAmountProducedSmall
                  .set(ThirdPartyPackagersPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ImportsPage, false).success.value
                  .copy(packagingSiteList = Map.empty)
                val result = navigateFollowingImportsPage(userAnswers)
                result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
              }

              s"navigate to $PackagingSiteDetailsPage if $ContractPackingPage is true and packaging site list is not empty" in {
                val userAnswers = initialUserAnswersWithAmountProducedSmall
                  .set(ThirdPartyPackagersPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ImportsPage, false).success.value
                  .copy(packagingSiteList = packingSiteMap)
                val result = navigateFollowingImportsPage(userAnswers)
                result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
              }

              "navigate to Change Activity CYA page if not contract packing" in {
                val userAnswers = initialUserAnswersWithAmountProducedSmall
                  .set(ThirdPartyPackagersPage, true).success.value
                  .set(ContractPackingPage, false).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(ImportsPage, false).success.value
                val result = navigateFollowingImportsPage(userAnswers)
                result mustBe routes.ChangeActivityCYAController.onPageLoad
              }
            }

            if (followingImports.page == ImportsPage) {
              s"and Contract Packing answered and registration is $voluntary" - {
                "navigate to Check Your Answers" in {
                  val isVoluntaryRegUserAnswers = emptyUserAnswersForChangeActivity
                    .set(AmountProducedPage, AmountProduced.Small).success.value
                    .set(ThirdPartyPackagersPage, true).success.value
                    .set(ContractPackingPage, false).success.value
                    .set(ImportsPage, false).success.value
                  val result = navigator.nextPage(ImportsPage, NormalMode, isVoluntaryRegUserAnswers)
                  result mustBe routes.ChangeActivityCYAController.onPageLoad
                }
              }
            }

            s"navigate to $SuggestDeregistrationPage when $ThirdPartyPackagersPage is false, $ContractPackingPage is false, and $ImportsPage is false" +
              s" and $OperatePackagingSiteOwnBrandsPage is either true or false" - {
              "and there are returns pending to be filed" in {
                val userAnswers = initialUserAnswersWithAmountProducedSmall
                  .set(ThirdPartyPackagersPage, false).success.value
                  .set(ContractPackingPage, false).success.value
                  .set(ImportsPage, false).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                val result = navigateFollowingImportsPage(userAnswers)
                result mustBe routes.SuggestDeregistrationController.onPageLoad
              }
            }
          }

          "when Amount Produced None" - {
            val initialUserAnswersWithAmountProducedNone = followingImports.initialUserAnswers
              .set(AmountProducedPage, AmountProduced.None).success.value

            "navigate to PackAtBusinessAddress if contract packing" in {
              val userAnswers = initialUserAnswersWithAmountProducedNone
                .set(ContractPackingPage, true).success.value
                .set(ImportsPage, false).success.value
                .copy(packagingSiteList = Map.empty)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
            }

            "navigate to Packaging Site Details if contract packing" in {
              val userAnswers = initialUserAnswersWithAmountProducedNone
                .set(ContractPackingPage, true).success.value
                .set(ImportsPage, false).success.value
                .copy(packagingSiteList = packingSiteMap)
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
            }

            s"navigate to $SuggestDeregistrationPage when $ContractPackingPage is false, and $ImportsPage is false" in {
              val userAnswers = initialUserAnswersWithAmountProducedNone
                .set(ContractPackingPage, false).success.value
                .set(ImportsPage, false).success.value
              val result = navigateFollowingImportsPage(userAnswers)
              result mustBe routes.SuggestDeregistrationController.onPageLoad
            }
          }
        }
      }
    })

    "How many imports" - {
      "in CheckMode" - {
        def navigateFromHowManyImportsPageInCheckMode(userAnswers: UserAnswers) = navigator.nextPage(HowManyImportsPage, CheckMode, userAnswers)

        "to the checkAnswers page when the imports page has already been populated and the user is a large producer" in {
          val userAnswers = emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.Large).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, true).success.value
          val result = navigateFromHowManyImportsPageInCheckMode(userAnswers)
          result mustBe routes.ChangeActivityCYAController.onPageLoad
        }

        "to the checkAnswers page when the imports page has already been populated and the user is a small producer" in {
          val userAnswers = emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.Small).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
          val result = navigateFromHowManyImportsPageInCheckMode(userAnswers)
          result mustBe routes.ChangeActivityCYAController.onPageLoad
        }

        "to the checkAnswers page when the imports page has already been populated and the user is not a producer" in {
          val userAnswers = emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.None).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
            .set(SecondaryWarehouseDetailsPage, false).success.value
          val result = navigateFromHowManyImportsPageInCheckMode(userAnswers)
          result mustBe routes.ChangeActivityCYAController.onPageLoad
        }
      }
    }

    "Remove packaging site confirm with one packing site remaining" - {
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

    "Remove packaging site confirm with no packing site remaining" - {
      def navigateFromRemovePackagingSiteConfirm(value: Boolean, mode: Mode) = {
        navigator.nextPage(RemovePackagingSiteDetailsPage, mode,
          emptyUserAnswersForChangeActivity
            .copy(packagingSiteList = Map.empty)
            .set(PackagingSiteDetailsPage, value).success.value
        )
      }

      List(NormalMode, CheckMode).foreach(mode => {
        s"select Yes to navigate to Packaging Site Details in $mode" in {
          val result = navigateFromRemovePackagingSiteConfirm(value = true, mode)
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(mode)
        }

        s"select No to navigate to Packaging Site Details in $mode" in {
          val result = navigateFromRemovePackagingSiteConfirm(value = false, mode)
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(mode)
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
  }
}
