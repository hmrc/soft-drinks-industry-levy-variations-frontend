package models.correctReturn

import models.{Enumerable, WithName}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SelectSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "Select" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(Select.values.toSeq)

      forAll(gen) {
        select =>

          JsString(select.toString).validate[Select].asOpt.value mustEqual select
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!Select.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[Select] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(Select.values.toSeq)

      forAll(gen) {
        select =>

          Json.toJson(select) mustEqual JsString(select.toString)
      }
    }
  }
}
