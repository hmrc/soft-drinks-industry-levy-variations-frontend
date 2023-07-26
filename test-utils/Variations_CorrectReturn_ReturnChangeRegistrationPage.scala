/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_CorrectReturn_ReturnChangeRegistrationPage extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", "correctReturn") + "/return-change-registration"
  override val title = "You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"
  override val header = "You changed your soft drinks business activity"

}
