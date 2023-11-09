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

package models.submission

import base.SpecBase
import models.VariationsSubmissionDataHelper
import models.backend.Site

class ClosedSiteSpec extends SpecBase with VariationsSubmissionDataHelper {

  val site = Site(updatedContactAddress, Some("NAME"), Some("100"), None)

  "fromSite" - {
    "should return a closed site with the site ref" in {
      val res = ClosedSite.fromSite(site)
      res mustBe ClosedSite("", "100", "This site is no longer open.")
    }
    "should return a closed site ref 1 when no ref in site" in {
      val res = ClosedSite.fromSite(site.copy(ref = None))
      res mustBe ClosedSite("", "1", "This site is no longer open.")
    }
  }

}
