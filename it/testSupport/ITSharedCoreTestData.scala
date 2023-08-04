package testSupport

import models.backend.{Site, UkAddress}

trait ITSharedCoreTestData {
  val ukAddress = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX")
  val packagingSitesFromSubscription = Map("0" -> Site(
    UkAddress(
      List("105B Godfrey Marchant Grove", "Guildford"),
      "GU14 8NL", None
    ),
    Some("96"),
    Some("Star Products Ltd"),
    None
  ))

}
