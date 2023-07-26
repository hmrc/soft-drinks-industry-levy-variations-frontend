/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_$packageName;format="cap"$_HowMany$className$Page extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", "$packageName$") + "/$litresUrl$"
  override val title = "$litresTitle$ - Soft Drinks Industry Levy - GOV.UK"
  override val header = "$litresHeading$"

}
