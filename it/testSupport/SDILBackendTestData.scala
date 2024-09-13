package testSupport

import models._
import models.backend.{CentralAssessment, CentralAsstInterest, OfficerAssessment, OfficerAsstInterest, PaymentOnAccount, RetrievedActivity, RetrievedSubscription, ReturnCharge, ReturnChargeInterest, Site, UkAddress, Unknown}
import models.submission.Litreage

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

object SDILBackendTestData {
  val localDate = LocalDate.now()

  val aSubscription = RetrievedSubscription(
    utr = "0000001611",
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
        None)
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val subscriptionDeregistered = aSubscription.copy(deregDate = Some(LocalDate.now().minusMonths(1)))

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

  val submittedDateTime = LocalDateTime.of(2023, 1, 1, 11, 0)

  val emptyReturn = SdilReturn(
    Litreage(0, 0),
    Litreage(0, 0),
    List.empty,
    Litreage(0, 0),
    Litreage(0, 0),
    Litreage(0, 0),
    Litreage(0, 0),
    submittedOn = Some(submittedDateTime))

  val literage = Litreage(200L, 100L)
  val smallProducer1 = SmallProducer("test 1", "XKSDIL000000024", literage)
  val smallProducer2 = SmallProducer("test 2", "XKSDIL000000025", literage)
  val smallProducerList = List(smallProducer1, smallProducer2)

  def currentReturnPeriod = ReturnPeriod(LocalDate.now)

  val returnPeriodList: List[ReturnPeriod] = List(ReturnPeriod(2020, 0), ReturnPeriod(2020, 1), ReturnPeriod(2020, 2), ReturnPeriod(2020, 3),
    ReturnPeriod(2021, 0), ReturnPeriod(2021, 1), ReturnPeriod(2021, 2), ReturnPeriod(2021, 3),
    ReturnPeriod(2022, 0), ReturnPeriod(2022, 1), ReturnPeriod(2022, 2), ReturnPeriod(2022, 3))

  val emptyReturnPeriods: List[ReturnPeriod] = List()

  val returnPeriods: List[ReturnPeriod] = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val finincialItemReturnCharge = ReturnCharge(currentReturnPeriod, BigDecimal(123.45))
  val finincialItemReturnChargeInterest = ReturnChargeInterest(localDate, BigDecimal(-12.45))
  val finincialItemCentralAssessment = CentralAssessment(localDate, BigDecimal(1))
  val finincialItemCentralAssInterest = CentralAsstInterest(localDate, BigDecimal(-5))
  val finincialItemOfficerAssessment = OfficerAssessment(localDate, BigDecimal(2))
  val finincialItemOfficerAssInterest = OfficerAsstInterest(localDate, BigDecimal(-3))
  val finincialItemPaymentOnAccount = PaymentOnAccount(localDate, "test", BigDecimal(300))
  val finincialItemUnknown = Unknown(localDate, "test", BigDecimal(300))
  val allFinicialItems = List(finincialItemReturnCharge, finincialItemReturnChargeInterest, finincialItemCentralAssessment,
    finincialItemCentralAssInterest, finincialItemOfficerAssessment, finincialItemOfficerAssInterest, finincialItemPaymentOnAccount, finincialItemUnknown)

}
