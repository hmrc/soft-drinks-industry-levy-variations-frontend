package models.retrieved

import models.backend.{Contact, Site, UkAddress}

import java.time.LocalDate
import play.api.libs.json.Json

case class RetrievedActivity(
                              smallProducer: Boolean,
                              largeProducer: Boolean,
                              contractPacker: Boolean,
                              importer: Boolean,
                              voluntaryRegistration: Boolean
                            ) {

  def isLiable: Boolean =
    !smallProducer && (largeProducer || contractPacker || importer)

  def isVoluntaryMandatory: Boolean =
    smallProducer && (contractPacker || importer)
}
object RetrievedActivity {
  implicit val format = Json.format[RetrievedActivity]
}

case class RetrievedSubscription( utr: String,
                                  sdilRef: String,
                                  orgName: String,
                                  address: UkAddress,
                                  activity: RetrievedActivity,
                                  liabilityDate: LocalDate,
                                  productionSites: List[Site],
                                  warehouseSites: List[Site],
                                  contact: Contact,
                                  deregDate: Option[LocalDate] = None)

object RetrievedSubscription {
  implicit val format = Json.format[RetrievedSubscription]
}
