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

package generators

import models._
import models.changeActivity.AmountProduced
import models.correctReturn.{AddASmallProducer, RepaymentMethod}
import models.updateRegisteredDetails.ContactDetails
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
trait ModelGenerators {

  implicit lazy val arbitraryCorrectReturnSelect: Arbitrary[ReturnPeriod] = {

    val returnPeriodList = List(ReturnPeriod(2020, 0), ReturnPeriod(2020, 1), ReturnPeriod(2020, 2), ReturnPeriod(2020, 3),
      ReturnPeriod(2022, 0), ReturnPeriod(2022, 1), ReturnPeriod(2022, 2), ReturnPeriod(2022, 3))

    Arbitrary {
      Gen.oneOf(returnPeriodList)
    }
  }

  implicit lazy val arbitraryChangeActivityAmountProduced: Arbitrary[AmountProduced] =
    Arbitrary {
      Gen.oneOf(AmountProduced.values)
    }

  implicit lazy val arbitraryCorrectReturnRepaymentMethod: Arbitrary[RepaymentMethod] =
    Arbitrary {
      Gen.oneOf(RepaymentMethod.values)
    }

  implicit lazy val arbitraryCorrectReturnAddASmallProducer: Arbitrary[AddASmallProducer] =
    Arbitrary {
      for {
        producerName <- arbitrary[Option[String]]
        referenceNumber <- arbitrary[String]
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield AddASmallProducer(producerName, referenceNumber, lowBand, highBand)
    }

  implicit lazy val arbitraryUpdateRegisteredDetailsUpdateContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        name <- arbitrary[String]
        job <- arbitrary[String]
        phoneNumber <- arbitrary[String]
        email <- arbitrary[String]
      } yield ContactDetails(name, job, phoneNumber, email)
    }

  implicit lazy val arbitrarySelectChange: Arbitrary[SelectChange] =
    Arbitrary {
      Gen.oneOf(SelectChange.values.toSeq)
    }

  implicit lazy val arbitraryLitresInBands: Arbitrary[LitresInBands] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield LitresInBands(lowBand, highBand)
    }
}
