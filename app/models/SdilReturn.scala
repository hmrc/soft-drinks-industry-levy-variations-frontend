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

import cats.implicits.{catsSyntaxSemigroup, toFoldableOps}

import java.time.{LocalDate, LocalDateTime}
import cats.implicits._
import scala.collection.immutable.ListMap

case class SdilReturn(
                       ownBrand: (Long, Long),
                       packLarge: (Long, Long),
                       packSmall: List[SmallProducer],
                       importLarge: (Long, Long),
                       importSmall: (Long, Long),
                       export: (Long, Long),
                       wastage: (Long, Long),
                       submittedOn: Option[LocalDateTime] = None
                     ) {

  def smallPackTotal: (Long, Long) = packSmall.map(x => x.litreage).combineAll
  def totalPacked: (Long, Long) = packLarge |+| smallPackTotal
  def totalImported: (Long, Long) = importLarge |+| importSmall

  private def toLongs: List[(Long, Long)] =
    List(ownBrand, packLarge, smallPackTotal, importLarge, importSmall, export, wastage)
  private val keys = ReturnLiterageList.returnLiterageList
  def sumLitres(l: List[(Long, Long)]): BigDecimal = l.map(x => LitreOps(x).dueLevy).sum

  /*
   Produces a map of differing litreage fields containing the revised and original litreages as a tuple
   and keyed by the field name
   */
  def compare(other: SdilReturn): ListMap[String, ((Long, Long), (Long, Long))] = {
    val y = this.toLongs

    ListMap(
      other.toLongs.zipWithIndex
        .filter { x =>
          x._1 != y(x._2)
        }
        .map { x =>
          keys(x._2) -> ((x._1, y(x._2)))
        }: _*)
  }

  def total: BigDecimal = sumLitres(List(ownBrand, packLarge, importLarge)) - sumLitres(List(export, wastage))

  type Litres = Long
  type LitreBands = (Litres, Litres)

  implicit class LitreOps(litreBands: LitreBands) {
    lazy val lowLevy: BigDecimal = litreBands._1 * BigDecimal("0.18")
    lazy val highLevy: BigDecimal = litreBands._2 * BigDecimal("0.24")
    lazy val dueLevy: BigDecimal = lowLevy + highLevy
  }
}

case class ReturnPeriod(year: Int, quarter: Int) {
  require(quarter <= 3 && quarter >= 0)
  require(year >= 2018)
  def start: LocalDate = LocalDate.of(year, quarter * 3 + 1, if (count == 0) 5 else 1)
  def end: LocalDate = next.start.minusDays(1)
  def deadline: LocalDate = end.plusDays(30)
  def next: ReturnPeriod = ReturnPeriod(count + 1)
  def previous: ReturnPeriod = ReturnPeriod(count - 1)
  def count: Int = year * 4 + quarter - 2018 * 4 - 1
}

object ReturnPeriod {
  def apply(o: Int): ReturnPeriod = {
    val i = o + 1
    ReturnPeriod(2018 + i / 4, i % 4)
  }
  def apply(date: LocalDate): ReturnPeriod = ReturnPeriod(date.getYear, quarter(date))
  def quarter(date: LocalDate): Int = { date.getMonthValue - 1 } / 3

}

object ReturnLiterageList {
  val returnLiterageList = List(
    "own-brands-packaged-at-own-sites",
    "packaged-as-a-contract-packer",
    "exemptions-for-small-producers",
    "brought-into-uk",
    "brought-into-uk-from-small-producers",
    "claim-credits-for-exports",
    "claim-credits-for-lost-damaged"
  )
}
