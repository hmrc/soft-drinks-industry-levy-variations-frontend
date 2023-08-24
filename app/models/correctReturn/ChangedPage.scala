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

package models.correctReturn

import models.{SdilReturn, SmallProducer}
import pages.Page
import pages.correctReturn._

case class ChangedPage(page: Page, answerChanged: Boolean)

  object ChangedPage {
    def returnLiteragePagesThatChangedComparedToOriginalReturn(original: SdilReturn, current: SdilReturn): List[ChangedPage] = {
      List(
        ChangedPage(
          page = OperatePackagingSiteOwnBrandsPage,
          answerChanged = original.ownBrand != current.ownBrand),
        ChangedPage(
          page = HowManyOperatePackagingSiteOwnBrandsPage,
          answerChanged = original.ownBrand != current.ownBrand),
        ChangedPage(
          page = PackagedAsContractPackerPage,
          answerChanged = original.packLarge != current.packLarge
        ),
        ChangedPage(
          page = HowManyPackagedAsContractPackerPage,
          answerChanged = original.packLarge != current.packLarge
        ),
        ChangedPage(
          page = BroughtIntoUKPage,
          answerChanged = original.importLarge != current.importLarge
        ),
        ChangedPage(
          page = HowManyBroughtIntoUKPage,
          answerChanged = original.importLarge != current.importLarge
        ),
        ChangedPage(
          page = BroughtIntoUkFromSmallProducersPage,
          answerChanged = original.importSmall != current.importSmall
        ),
        ChangedPage(
          page = HowManyBroughtIntoUkFromSmallProducersPage,
          answerChanged = original.importSmall != current.importSmall
        ),
        ChangedPage(
          page = ClaimCreditsForExportsPage,
          answerChanged = original.export != current.export
        ),
        ChangedPage(
          page = HowManyClaimCreditsForExportsPage,
          answerChanged = original.export != current.export
        ),
        ChangedPage(
          page = ExemptionsForSmallProducersPage,
          answerChanged = SmallProducer.totalOfAllSmallProducers(original.packSmall) != SmallProducer.totalOfAllSmallProducers(current.packSmall)
        )
      )
    }
  }
