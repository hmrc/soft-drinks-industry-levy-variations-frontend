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

import base.SpecBase
import models.ReturnPeriod
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, RadioItem}

class SelectSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with SpecBase{

  "Select" - {

    "when i put invalid data in, i get error out" in {

          SelectReturn.options(returnPeriodList) mustEqual List(RadioItem(HtmlContent("January to March 2020"), Some("value_0"), Some("""{"year":2020,"quarter":0}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("April to June 2020"), Some("value_1"), Some("""{"year":2020,"quarter":1}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("July to September 2020"), Some("value_2"), Some("""{"year":2020,"quarter":2}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("October to December 2020"), Some("value_3"), Some("""{"year":2020,"quarter":3}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("January to March 2022"), Some("value_4"), Some("""{"year":2022,"quarter":0}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("April to June 2022"), Some("value_5"), Some("""{"year":2022,"quarter":1}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("July to September 2022"), Some("value_6"), Some("""{"year":2022,"quarter":2}"""), None, None, None, false, None, false, Map()),
                                                                RadioItem(HtmlContent("October to December 2022"), Some("value_7"), Some("""{"year":2022,"quarter":3}"""), None, None, None, false, None, false, Map()))
      }
    }

    "When i put no data in i go no data out" in {
      SelectReturn.options(List()) mustEqual List()
    }
}
