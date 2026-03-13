package models

import org.scalacheck.Gen

object TaxRateUtil {
  val janToMarInt: Gen[Int] = Gen.choose(1, 3)
  val aprToDecInt: Gen[Int] = Gen.choose(4, 12)

  val lowerBandCostPerLitre: BigDecimal = BigDecimal("0.18")
  val higherBandCostPerLitre: BigDecimal = BigDecimal("0.24")

  val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
  val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

  case class LevyBands(low: String, high: String)
  def levyValues(returnPeriod: ReturnPeriod): LevyBands = returnPeriod match {
      case ReturnPeriod(2025, 0) => LevyBands("£180.00", "£480.00")
      case ReturnPeriod(2026, 0) => LevyBands("£194.00", "£518.00")
      case _ =>
        throw new IllegalArgumentException(
          s"Unexpected ReturnPeriod: $returnPeriod"
        )
    }
}
