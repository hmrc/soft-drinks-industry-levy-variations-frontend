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
import pages.correctReturn._
import controllers.correctReturn.routes
import play.api.libs.json.Json

class NavigatorForCorrectReturnSpec extends SpecBase {

  val navigator = new NavigatorForCorrectReturn

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe defaultCall
      }

      "must go from repayment method page to check changes page" in {
        navigator.nextPage(RepaymentMethodPage, NormalMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe
          controllers.correctReturn.routes.CorrectReturnCheckChangesCYAController.onPageLoad
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress = contactAddress)) mustBe
          controllers.correctReturn.routes.CorrectReturnCYAController.onPageLoad
      }
    }
  }

  "Brought into UK" - {
    def navigateFromBroughtIntoUkPage(value: Boolean, mode: Mode) =
      navigator.nextPage(BroughtIntoUKPage, mode, emptyUserAnswersForCorrectReturn.set(BroughtIntoUKPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How many brought into UK in $mode" in {
        val result = navigateFromBroughtIntoUkPage(value = true, mode)
        result mustBe routes.HowManyBroughtIntoUKController.onPageLoad(mode)
      }
    })

    "select No to navigate to brought into uk from small producers page in NormalMode" in {
      val result = navigateFromBroughtIntoUkPage(value = false, NormalMode)
      result mustBe routes.BroughtIntoUkFromSmallProducersController.onPageLoad(NormalMode)
    }

    "Should No to navigate to Check Your Answers page in CheckMode" in {
      val result = navigateFromBroughtIntoUkPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Packaged as a contract packer" - {
    def navigateFromPackagedAsContractPackerPage(value: Boolean, mode: Mode) =
      navigator.nextPage(PackagedAsContractPackerPage, mode, emptyUserAnswersForCorrectReturn.set(PackagedAsContractPackerPage, value).success.value)

    List(NormalMode, CheckMode).foreach(mode => {
      s"select Yes to navigate to How Many packaged as contract packer in $mode" in {
        val result = navigateFromPackagedAsContractPackerPage(value = true, mode)
        result mustBe routes.HowManyPackagedAsContractPackerController.onPageLoad(mode)
      }
    })

    "select No to navigate to exemptions for small producers page in NormalMode" in {
      val result = navigateFromPackagedAsContractPackerPage(value = false, NormalMode)
      result mustBe routes.ExemptionsForSmallProducersController.onPageLoad(NormalMode)
    }

    "Should navigate to Check Your Answers page when no is selected in CheckMode" in {
      val result = navigateFromPackagedAsContractPackerPage(value = false, CheckMode)
      result mustBe routes.CorrectReturnCYAController.onPageLoad
    }
  }

  "Add A Small Producer " - {

    "Should navigate to small producer details controller when data is entered" in {
      navigator.nextPage(AddASmallProducerPage,
        CheckMode,
        emptyUserAnswersForCorrectReturn.copy(data = Json.obj("addASmallProducer" -> Json.obj("lowBand" -> "10000", "highBand" -> "20000")))
      ) mustBe routes.SmallProducerDetailsController.onPageLoad(CheckMode)
    }
  }

  "Exemptions for small producers" - {


    "Exemptions for small producers" - {

      def navigateFromExemptionsForSmallProducers(value: Boolean, mode: Mode) =
        navigator.nextPage(ExemptionsForSmallProducersPage, mode, emptyUserAnswersForCorrectReturn.set(ExemptionsForSmallProducersPage, value).success.value)

      "select Yes to navigate to Add small producer pager in NormalMode" in {
        val result = navigateFromExemptionsForSmallProducers(value = true, NormalMode)
        result mustBe routes.AddASmallProducerController.onPageLoad(NormalMode)
      }

      "select No to navigate to brought into uk page in NormalMode" in {
        val result = navigateFromExemptionsForSmallProducers(value = false, NormalMode)
        result mustBe routes.BroughtIntoUKController.onPageLoad(NormalMode)
      }

      "Should navigate to Check Your Answers page when no is selected in CheckMode" in {
        val result = navigateFromExemptionsForSmallProducers(value = false, CheckMode)
        result mustBe routes.CorrectReturnCYAController.onPageLoad
      }

    }

    s"must go from $AddASmallProducerPage to $SmallProducerDetailsPage in EditMode" in {
      navigator.nextPage(AddASmallProducerPage, EditMode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
        contactAddress)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(NormalMode)
    }

    List(NormalMode, CheckMode).foreach(mode => {
      s"must go from $AddASmallProducerPage to $SmallProducerDetailsPage in $mode" in {
        navigator.nextPage(AddASmallProducerPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
          contactAddress)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(mode)
      }


      s"must go from $RemoveSmallProducerConfirmPage to $ExemptionsForSmallProducersPage in $mode when zero small producers left" in {
        navigator.nextPage(RemoveSmallProducerConfirmPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
          contactAddress).set(RemoveSmallProducerConfirmPage, true).success.value) mustBe controllers.correctReturn.routes.ExemptionsForSmallProducersController.onPageLoad(mode)
      }

      s"must go from $RemoveSmallProducerConfirmPage to $SmallProducerDetailsPage in $mode when one or more small producers left" in {
        navigator.nextPage(RemoveSmallProducerConfirmPage, mode, UserAnswers("id", SelectChange.CorrectReturn, contactAddress =
          contactAddress, smallProducerList = smallProducerList)) mustBe controllers.correctReturn.routes.SmallProducerDetailsController.onPageLoad(mode)
      }
    })
  }
}
