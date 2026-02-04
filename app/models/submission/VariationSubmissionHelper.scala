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

import models.backend.Site
import scala.util.Try

trait VariationSubmissionHelper {

  implicit class RichA[A](first: A) {

    /** if the first value is the same as the second then return None - otherwise return Some(first)
      */
    def ifDifferentTo(other: A): Option[A] =
      if (first == other) None else Some(first)
  }

  def getHighestRefNumber(sites: List[Site]): Int = sites
    .flatMap(site =>
      site.ref
        .fold[Option[Int]](None)(ref => Try(ref.toInt).toOption)
    )
    .maxOption
    .getOrElse(0)

}
