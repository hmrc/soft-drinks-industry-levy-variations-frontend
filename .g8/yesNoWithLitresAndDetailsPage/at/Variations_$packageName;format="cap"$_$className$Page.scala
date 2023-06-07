/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_$packageName;format="cap"$_$className$Page extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", $packageName$) + "/$yesNoUrl$"
  override val title = "$yesNoTitle$"
  override val header = "$yesNoHeading$"

}
