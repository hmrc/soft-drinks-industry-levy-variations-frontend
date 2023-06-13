/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.variations

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Variations_UpdateRegisteredDetails_PackingSiteDetailsRemovePage extends BasePage {

  override val url: String = TestConfiguration.url("variations-frontend", updateRegisteredDetails) + "//packaging-site-details/remove"
  override val title = "Are you sure you want to remove this warehouse?"
  override val header = "Are you sure you want to remove this warehouse?"

}
