package testSupport.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import generators.ChangeActivityCYAGenerators.contactAddress
import models.UserAnswers
import models.backend.{RetrievedActivity, Site, UkAddress}
import models.enums.SiteTypes
import models.submission._
import models.updateRegisteredDetails.ContactDetails
import play.api.libs.json.{JsObject, Json}
import testSupport.SDILBackendTestData

import java.time.LocalDate
import scala.jdk.CollectionConverters._

trait SubmissionVariationHelper {

  val ORG_NAME = "Super Lemonade Plc"
  val UPDATED_NAME = "updated name"
  val UPDATED_POSITION = "updated position"
  val UPDATED_PHONE_NUMBER = "updated phone number"
  val UPDATED_EMAIL = "updated email"
  val ORIGINAL_ADDRESS = UkAddress(lines = List("63 Clifton Roundabout", "Worcester"), "WR53 7CX", None)
  val UPDATED_ADDRESS = UkAddress(lines = List("test address"), "AB2 3FG")
  val NEW_VARITION_SITE = VariationsSite("New Site", "10", updatedVariationsContact, SiteTypes.WAREHOUSE)
  val CLOSED_SITE = ClosedSite("Closed Site", "8", "Expired")
  val DEREG_REASON = "No longer needed"
  val localDate = LocalDate.now()
  val DEREG_DATE = localDate
  val contactDetailsFromSubscription = ContactDetails.fromContact(SDILBackendTestData.aSubscription.contact)

  def generateSubscription(hasClosedSites: Boolean, optOriginalActivity: Option[RetrievedActivity] = None) = {
    val productionSites = if (hasClosedSites) {
      List(site1, closedsite1)
    } else {
      List(site1)
    }
    val warehouses = if (hasClosedSites) {
      List(site2, closedsite2)
    } else {
      List(site2)
    }
    optOriginalActivity match {
      case Some(originalActivity) => SDILBackendTestData.aSubscription.copy(
        address = ORIGINAL_ADDRESS,
        activity = originalActivity,
        productionSites = productionSites,
        warehouseSites = warehouses
      )
      case _ => SDILBackendTestData.aSubscription.copy(
        address = ORIGINAL_ADDRESS,
        productionSites = productionSites,
        warehouseSites = warehouses
      )
    }
  }

  def addSitesToUserAnswers(userAnswers: UserAnswers, hasNewSites: Boolean, hasRemovedSites: Boolean): UserAnswers = {
    val (productionSites: Map[String, Site], warehouses: Map[String, Site]) = (hasNewSites, hasRemovedSites) match {
      case (true, true) => (Map("2" -> site3), Map("2" -> site4))
      case (true, _) => (Map("1" -> site1, "2" -> site3), Map("1" -> site2, "2" -> site4))
      case (_, true) => (Map.empty, Map.empty)
      case _ => (Map("1" -> site1), Map("1" -> site2))
    }: @unchecked
    userAnswers.copy(
      packagingSiteList = productionSites,
      warehouseList = warehouses
    )
  }

  val site1 = Site(contactAddress, Some("Site 1"), Some("12"), Some(localDate.plusYears(1)))
  val site2 = Site(contactAddress, Some("Site 2"), Some("13"), Some(localDate.plusYears(1)))

  val site3 = Site(contactAddress, Some("Site 3"), None, None)
  val site4 = Site(contactAddress, Some("Site 4"), None, None)

  val closedsite1 = Site(contactAddress, Some("Closed Site 1"), Some("14"), Some(localDate.minusMonths(1)))
  val closedsite2 = Site(contactAddress, Some("Closed Site 2"), Some("15"), Some(localDate.minusMonths(1)))


  def getExpectedNewSites(hasNewSites: Boolean,
                          hasClosedSites: Boolean,
                          siteContactDetails: ContactDetails = contactDetailsFromSubscription): List[VariationsSite] = {
    if (hasNewSites) {
      lazy val minRef = if (hasClosedSites) {
        16
      } else {
        14
      }
      val newProductionSite = VariationsSite.generateFromSite(site3,
        siteContactDetails,
        minRef,
        SiteTypes.PRODUCTION_SITE
      )
      val newWarehouse = VariationsSite.generateFromSite(site4,
        siteContactDetails,
        minRef + 1,
        SiteTypes.WAREHOUSE
      )
      List(newProductionSite, newWarehouse)
    } else {
      List.empty
    }
  }

  def getExpectedClosedSites(hasRemovedSites: Boolean, hasClosedSites: Boolean): List[ClosedSite] = {
    (hasRemovedSites, hasClosedSites) match {
      case (true, true) => List(site1, closedsite1, site2, closedsite2).map(ClosedSite.fromSite)
      case (false, true) => List(closedsite1, closedsite2).map(ClosedSite.fromSite)
      case (true, false) => List(site1, site2).map(ClosedSite.fromSite)
      case _ => List.empty[ClosedSite]
    }
  }

  val updatedPersonalDetails: VariationsPersonalDetails = VariationsPersonalDetails(
    name = Some(UPDATED_NAME),
    position = Some(UPDATED_POSITION),
    telephoneNumber = Some(UPDATED_PHONE_NUMBER),
    emailAddress = Some(UPDATED_EMAIL)
  )

  def updatedVariationsContact: VariationsContact = VariationsContact(
    address = Some(UPDATED_ADDRESS),
    telephoneNumber = Some(UPDATED_PHONE_NUMBER),
    emailAddress = Some(UPDATED_EMAIL)
  )

  val UPDATED_ACTIVITY = Activity(
    ProducedOwnBrand = Some(Litreage(100, 100)),
    Imported = Some(Litreage(300, 300)),
    CopackerAll = Some(Litreage(200, 200)),
    Copackee = None,
    isLarge = true
  )

  val UPDATED_SDIL_ACTIVITY = SdilActivity(
    activity = Some(UPDATED_ACTIVITY),
    produceLessThanOneMillionLitres = Some(false),
    smallProducerExemption = Some(true),
    usesContractPacker = Some(true),
    voluntarilyRegistered = Some(false),
    reasonForAmendment = None,
    taxObligationStartDate = None
  )

  val defaultVariationsSubmission = VariationsSubmission(
    tradingName = None,
    displayOrgName = ORG_NAME,
    ppobAddress = ORIGINAL_ADDRESS,
    businessContact = None,
    correspondenceContact = None,
    primaryPersonContact = None,
    sdilActivity = None,
    deregistrationText = None,
    deregistrationDate = None,
    newSites = List.empty,
    closeSites = List.empty
  )

  def requestBodyMatchesDeregistration(wireMockServer: WireMockServer) = {
    val bodyExpected = defaultVariationsSubmission.copy(
      deregistrationText = Some(DEREG_REASON),
      deregistrationDate = Some(DEREG_DATE)
    )
    requestedBodyMatchesExpected(wireMockServer, bodyExpected)
  }

  def requestBodyMatchesChangeActivity(wireMockServer: WireMockServer,
                                       sdilActivity: SdilActivity,
                                       newSites: List[VariationsSite] = List.empty,
                                       closeSites: List[ClosedSite] = List.empty) = {
    val bodyExpected = defaultVariationsSubmission.copy(
      sdilActivity = Some(sdilActivity),
      newSites = newSites,
      closeSites = closeSites
    )
    requestedBodyMatchesExpected(wireMockServer, bodyExpected)
  }

  def requestBodyMatchesUpdateRegDetails(wireMockServer: WireMockServer, variationContact: Option[VariationsContact] = None,
                              variationsPersonalDetails: Option[VariationsPersonalDetails] = None,
                              newSites: List[VariationsSite] = List.empty,
                              closeSites: List[ClosedSite] = List.empty): Boolean = {
    val bodyExpected = defaultVariationsSubmission.copy(
      businessContact = variationContact,
      correspondenceContact = variationContact,
      primaryPersonContact = variationsPersonalDetails,
      newSites = newSites,
      closeSites = closeSites
    )
    requestedBodyMatchesExpected(wireMockServer, bodyExpected)
  }

  def requestedBodyMatchesExpected(wireMockServer: WireMockServer, bodyExpected: VariationsSubmission): Boolean = {
    val requestMade = wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest)
      .filter(_.getUrl.contains("/submit-variations/sdil/XKSDIL000000022")).head
    val jsonBodySent =  Json.parse(requestMade.getBodyAsString).as[JsObject]
    val jsonBodyOfExpectedPost = Json.toJson(bodyExpected).as[JsObject]
    jsonBodyOfExpectedPost == jsonBodySent
  }

}
