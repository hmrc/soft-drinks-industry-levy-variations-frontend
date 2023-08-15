package testSupport

import models.Warehouse
import models.backend.{Site, UkAddress}

trait ITSharedCoreTestData {
  val ukAddress: UkAddress = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX")
  val packagingSitesFromSubscription: Map[String, Site] = Map("0" -> Site(
    UkAddress(
      List("105B Godfrey Marchant Grove", "Guildford"),
      "GU14 8NL", None
    ),
    Some("96"),
    Some("Star Products Ltd"),
    None
  ))
  val warehousesFromSubscription: Map[String, Warehouse] = Map(
    "0" -> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy"),"WR53 7CX")),
    "123456" -> Warehouse(Some("XYZ Ltd"), UkAddress(List("30 Main Street"), "WR53 7CX"))
  )

}
