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

package models

import generators.ChangeActivityCYAGenerators.contactAddress
import generators.ModelGenerators
import org.scalacheck.{Gen, Shrink}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

class UserAnswersIsEmptySpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  val nonEmptyAlphaStr: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

  "UserAnswers" - {
    "isEmpty" - {
      "must return true or false correctly depending on whether data is empty at that path" in {
//        TODO: FINISH THIS TEST
        val gen = for {
          keys <- Gen.nonEmptyListOf(nonEmptyAlphaStr)
          value <- nonEmptyAlphaStr
        } yield (keys, value)

        forAll(gen) {
          case (keys: List[String], value: String) =>
            val change = SelectChange.CancelRegistration
            val pathNodes = (List(change.toString) ++ keys).map(KeyPathNode)
            val jsPath = JsPath(pathNodes)
            val data = Json.obj().set(jsPath, JsString(value)).asOpt.value.asInstanceOf[JsObject]
            val userAnswers = UserAnswers("sdilId", change, contactAddress = contactAddress, data = data)
            val emptyJsPaths = keys.init.foldLeft(List(JsPath(List(KeyPathNode(change.toString)))))((acc, d) => {
              val last = acc.last
              val newPath = acc.last.path :+ KeyPathNode(d)
              acc :+ JsPath(newPath)
            })
            emptyJsPaths.foreach(path => {
              userAnswers.isEmpty(path) mustEqual true
            })
            userAnswers.isEmpty(jsPath) mustEqual false
        }
      }
    }
  }
}
