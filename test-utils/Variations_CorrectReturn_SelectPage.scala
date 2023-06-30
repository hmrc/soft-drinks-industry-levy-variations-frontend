/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_CorrectReturn_SelectPage extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", correctReturn) + "/select"
  override val title = "which return do you need to correct"
  override val header = "which return do you need to correct"

}
