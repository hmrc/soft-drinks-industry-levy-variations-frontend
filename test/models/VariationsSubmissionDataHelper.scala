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

package models

import models.backend.UkAddress
import models.enums.SiteTypes
import models.submission.{Activity, ClosedSite, Litreage, SdilActivity, VariationsContact, VariationsPersonalDetails, VariationsSite, VariationsSubmission}

import java.time.LocalDate

trait VariationsSubmissionDataHelper {

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
  val DEREG_DATE = LocalDate.now()

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

  def testVariationSubmission(variationContact: Option[VariationsContact] = None,
                              variationsPersonalDetails: Option[VariationsPersonalDetails] = None,
                              sdilActivity: Option[SdilActivity] = Some(SdilActivity()),
                              newSites: List[VariationsSite] = List.empty,
                              closeSites: List[ClosedSite] = List.empty,
                              isDeregistered: Boolean = false): VariationsSubmission = VariationsSubmission(
    tradingName = None,
    displayOrgName = ORG_NAME,
    ppobAddress = ORIGINAL_ADDRESS,
    businessContact = variationContact,
    correspondenceContact = variationContact,
    primaryPersonContact = variationsPersonalDetails,
    sdilActivity = sdilActivity,
    deregistrationText = if (isDeregistered) {Some(DEREG_REASON)} else {None},
    deregistrationDate = if (isDeregistered) {Some(DEREG_DATE)} else {None},
    newSites = newSites,
    closeSites = closeSites
  )

}
