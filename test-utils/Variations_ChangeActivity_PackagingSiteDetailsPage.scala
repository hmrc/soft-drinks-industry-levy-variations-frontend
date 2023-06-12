/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_ChangeActivity_PackagingSiteDetailsPage extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", changeActivity) + "/packaging-site-details"
  override val title = "UK packaging site details"
  override val header = "UK packaging site details"

}
