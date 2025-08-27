package testSupport

import controllers.routes
import models.backend.{RetrievedActivity, RetrievedSubscription, Site, UkAddress}
import models.{Contact, backend}
import org.scalatest.TryValues
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait ITCoreTestData
  extends TryValues
    with ITCoreTestDataForUpdateRegisteredDetails
    with ITCoreTestDataForCancelRegistration
    with ITCoreTestDataForChangeActivity
    with ITCoreTestDataForCorrectReturn {

  val year = 2022
  val month = 11
  val day = 10
  val date = LocalDate.of(year, month, day)

  val UTR = "0000001611"
  val SDIL_REF = "XKSDIL000000022"

  val validDateJson = Json.obj(
    "value.day" -> day.toString,
    "value.month" -> month.toString,
    "value.year" -> year.toString
  )

  val dateMap = Map("day" -> day, "month" -> month, "year" -> year)

  def sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")

  val packAtBusinessAddressSite = Map("1" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), Some("Super Lemonade Plc"), None, None))
  val packAtBusinessAddressSites =
    Map(
      "siteUNO" -> Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), Some("Super Lemonade Plc"), None, None),
      "siteDOS" -> Site(UkAddress(List("64 Clifton Roundabout", "Worcester"), "WR53 7CX", None), Some("Super Lemonade Plc"), None, None)
    )

  implicit val duration: FiniteDuration = 5.seconds

  val defaultCall = routes.SelectChangeController.onPageLoad

  val diffSubscription = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val diffSubscriptionWithWarehouses: RetrievedSubscription = backend.RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(),
    warehouseSites = List(Site(UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None), None, Some("Super Lemonade Plc"), None)),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )
}
