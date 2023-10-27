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

package base

import models._
import models.backend.{Site, UkAddress}
import models.correctReturn.CorrectReturnUserAnswersData
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.json.Json

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

trait TestData {

  val userAnswersId: String = "id"
  val sdilNumber: String = "XKSDIL000000022"

  val amounts = Amounts(originalReturnTotal = 0,newReturnTotal = 1320, accountBalance = 502.75, adjustedAmount = 1822.75)

  val aSubscription: RetrievedSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val deregSubscription = aSubscription.copy(deregDate = Some(LocalDate.now.minusMonths(1)))

  val subscriptionSmallProducer = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = true, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val producerName = "Super Cola Plc"
  val sdilReference = "XCSDIL000000069"
  val producerNameParty = "Soft Juice"
  val sdilReferenceParty = "XMSDIL000000113"
  val bandMax: Long = 100000000000000L
  val litres: Long = bandMax - 1
  val smallProducerList: List[SmallProducer] = List(SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)))

  val returnPeriods = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val returnPeriod = List(ReturnPeriod(2018, 1))
  val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
  val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
  val financialItemList = List(financialItem1, financialItem2)
  val submittedDateTime = LocalDateTime.of(2023, 1, 1, 11, 0)


  val twoWarehouses: Map[String, Site] = Map(
    "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), Some("ABC Ltd")),
    "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", ""), "SA13 7CE"), Some("Super Cola Ltd"))
  )

  val returnPeriodsFor2020 = List(ReturnPeriod(2020, 3), ReturnPeriod(2020, 2), ReturnPeriod(2020, 1), ReturnPeriod(2020, 0))
  val returnPeriodsFor2022 = List(ReturnPeriod(2022, 3), ReturnPeriod(2022, 2), ReturnPeriod(2022, 1), ReturnPeriod(2022, 0))

  val returnPeriodList = returnPeriodsFor2022 ++ returnPeriodsFor2020

  val JsonreturnPeriodList = List(Json.toJson(ReturnPeriod(2020, 0)), Json.toJson(ReturnPeriod(2020, 1)),
    Json.toJson(ReturnPeriod(2020, 2)), Json.toJson(ReturnPeriod(2020, 3)),
    Json.toJson(ReturnPeriod(2022, 0)), Json.toJson(ReturnPeriod(2022, 1)),
    Json.toJson(ReturnPeriod(2022, 2)), Json.toJson(ReturnPeriod(2022, 3)))

  lazy val warehouse = Site(UkAddress(List("33 Rhes Priordy"), "WR53 7CX"), Some("ABC Ltd"))
  lazy val packingSite = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("88"),
    Some("Wild Lemonade Group"),
    Some(LocalDate.of(2018, 2, 26)))

  lazy val packingSiteMap = Map("000001" -> packingSite)

  lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")
  lazy val updatedContactAddress = UkAddress(List("123 Updated Street", "Updated City Name"), "E73 2RP")

  val emptyUserAnswersForUpdateRegisteredDetails: UserAnswers =
    UserAnswers(userAnswersId, SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress)
  val userAnswersWithUpdatedContactForUpdateRegisteredDetails: UserAnswers = UserAnswers(sdilNumber,
    SelectChange.UpdateRegisteredDetails,
    Json.obj(("updateRegisteredDetails", Json.obj("updateContactDetails" -> Json.toJson(updatedContactAddress)))),
    packagingSiteList = packingSiteMap,
    contactAddress = contactAddress
  )
  val warehouseAddedToUserAnswersForUpdateRegisteredDetails: UserAnswers =
    UserAnswers(userAnswersId, SelectChange.UpdateRegisteredDetails, warehouseList = Map("1" -> warehouse), contactAddress = contactAddress)
  val emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.ChangeActivity, contactAddress = contactAddress)
  val warehouseAddedToUserAnswersForChangeActivity: UserAnswers =
    UserAnswers(userAnswersId, SelectChange.ChangeActivity, warehouseList = Map("1" -> warehouse), contactAddress = contactAddress)

  val expectedCorrectReturnDataForNilReturn = CorrectReturnUserAnswersData(false, None, false, None, false, false, None, false, None, false, None, false, None)
  val expectedCorrectReturnDataForPopulatedReturn = CorrectReturnUserAnswersData(
    true, Some(LitresInBands(100, 200)),
    true, Some(LitresInBands(200, 100)),
    true,
    true, Some(LitresInBands(300, 400)),
    true, Some(LitresInBands(400, 300)),
    true, Some(LitresInBands(50, 60)),
    true, Some(LitresInBands(60, 50))
  )

  val emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, contactAddress = contactAddress,
    correctReturnPeriod = returnPeriodsFor2022.headOption)

  def userAnswersForCorrectReturn(sdilReturnIsNil: Boolean) = {
    val (correctReturnData, smallProducers) = if (sdilReturnIsNil) {
      (expectedCorrectReturnDataForNilReturn, List())
    } else {
      (expectedCorrectReturnDataForPopulatedReturn, smallProducerList)
    }
    emptyUserAnswersForCorrectReturn
      .setForCorrectReturn(correctReturnData, smallProducers, returnPeriod.head).success.value
  }

  val emptySdilReturn: SdilReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0), submittedOn =
    Some(submittedDateTime.toInstant(ZoneOffset.UTC)))

  val userAnswersForCorrectReturnWithEmptySdilReturn:
    UserAnswers = userAnswersForCorrectReturn(true).copy(data = Json.obj("originalSDILReturn" -> Json.toJson(emptySdilReturn)))
  val userAnswerTwoWarehouses: UserAnswers = userAnswersForCorrectReturn(true).copy(warehouseList = twoWarehouses)
  val emptyUserAnswersForCancelRegistration: UserAnswers = UserAnswers(sdilNumber, SelectChange.CancelRegistration, contactAddress = contactAddress)

  def emptyUserAnswersForSelectChange(selectChange: SelectChange): UserAnswers = UserAnswers(sdilNumber, selectChange, contactAddress = contactAddress)

}
