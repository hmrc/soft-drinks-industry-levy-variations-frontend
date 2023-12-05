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

package orchestrators

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.VariationsSubmissionDataHelper
import models.backend.{RetrievedActivity, Site, UkAddress}
import models.enums.SiteTypes
import models.submission.{ClosedSite, VariationsContact, VariationsPersonalDetails, VariationsSite}
import models.updateRegisteredDetails.ContactDetails
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.updateRegisteredDetails.UpdateContactDetailsPage

import java.time.LocalDate

class UpdateRegisteredDetailsOrchestratorSpec extends SpecBase with MockitoSugar with VariationsSubmissionDataHelper{

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val localDate: LocalDate = LocalDate.of(2023, 5, 6)

  val updateRegDetailsOrchestrator = new UpdateRegisteredDetailsOrchestrator(mockConnector)

  val retrievedActivityLiableLargeProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = true,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = false
  )

  val retrievedActivityLiableSmallProducer = RetrievedActivity(
    smallProducer = true,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = true
  )


  val retrievedActivityLiableNoneProducer = RetrievedActivity(
    smallProducer = false,
    largeProducer = false,
    contractPacker = false,
    importer = false,
    voluntaryRegistration = false
  )

  val contactDetailsFromSubscription = ContactDetails.fromContact(aSubscription.contact)



  "submitVariation" - {
    "should send the expected variation and return unit" - {
      "when the user has added new sites" - {
        "and has expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = true
          override val hasNewSites: Boolean = true
          override val hasRemovedSites = false

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and does not have expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = false
          override val hasNewSites: Boolean = true
          override val hasRemovedSites = false

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }

      "when the user has added and removed new sites" - {
        "and has expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = true
          override val hasNewSites: Boolean = true
          override val hasRemovedSites = true

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and does not have expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = false
          override val hasNewSites: Boolean = true
          override val hasRemovedSites = true

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }

      "when the user has removed sites" - {
        "and has expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = true
          override val hasNewSites: Boolean = false
          override val hasRemovedSites = true

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "and does not have expired sites in the subscription" in new URDOrchestratorHelper {
          override val hasClosedSites: Boolean = false
          override val hasNewSites: Boolean = false
          override val hasRemovedSites = true

          val expectedSubscription = testVariationSubmission(newSites = expectedNewSites, closeSites = expectedClosedSites)

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }

      "when the user has updated contact details" - {
        "by changing only the name" in new URDOrchestratorHelper {
          val updatedContactDetails = contactDetailsFromSubscription.copy(fullName = UPDATED_NAME)
          override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
          val expectVariationPDs = VariationsPersonalDetails(name = Some(UPDATED_NAME))

          val expectedSubscription = testVariationSubmission(variationsPersonalDetails = Some(expectVariationPDs))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "by changing only the job title" in new URDOrchestratorHelper {
          val updatedContactDetails = contactDetailsFromSubscription.copy(position = UPDATED_POSITION)
          override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
          val expectVariationPDs = VariationsPersonalDetails(position = Some(UPDATED_POSITION))

          val expectedSubscription = testVariationSubmission(variationsPersonalDetails = Some(expectVariationPDs))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "by changing only the phone" in new URDOrchestratorHelper {
          val updatedContactDetails = contactDetailsFromSubscription.copy(phoneNumber = UPDATED_PHONE_NUMBER)
          override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
          val expectVariationPDs = VariationsPersonalDetails(telephoneNumber = Some(UPDATED_PHONE_NUMBER))
          val expectVariationContact = VariationsContact(telephoneNumber = Some(UPDATED_PHONE_NUMBER))
          val expectedSubscription = testVariationSubmission(variationContact = Some(expectVariationContact), variationsPersonalDetails = Some(expectVariationPDs))

          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "by changing only the email" in new URDOrchestratorHelper {
          val updatedContactDetails = contactDetailsFromSubscription.copy(email = UPDATED_EMAIL)
          override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
          val expectVariationPDs = VariationsPersonalDetails(emailAddress = Some(UPDATED_EMAIL))
          val expectVariationContact = VariationsContact(emailAddress = Some(UPDATED_EMAIL))
          val expectedSubscription = testVariationSubmission(variationContact = Some(expectVariationContact), variationsPersonalDetails = Some(expectVariationPDs))
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }

        "by changing all fields" in new URDOrchestratorHelper {
          val updatedContactDetails = ContactDetails(UPDATED_NAME, UPDATED_POSITION, UPDATED_PHONE_NUMBER, UPDATED_EMAIL)
          override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
          val expectVariationPDs = updatedPersonalDetails
          val expectVariationContact = VariationsContact(telephoneNumber = Some(UPDATED_PHONE_NUMBER), emailAddress = Some(UPDATED_EMAIL))
          val expectedSubscription = testVariationSubmission(variationContact = Some(expectVariationContact), variationsPersonalDetails = Some(expectVariationPDs))
          when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

          val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
          whenReady(res.value) { result =>
            result mustEqual Right((): Unit)
          }
        }
      }


      "when the user has updated business address" in new URDOrchestratorHelper {
        override val optUpdatedBusinessAddress: Option[UkAddress] = Some(UPDATED_ADDRESS)
        val expectVariationContact = VariationsContact(address = Some(UPDATED_ADDRESS))
        val expectedSubscription = testVariationSubmission(variationContact = Some(expectVariationContact))
        when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))

        val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
        whenReady(res.value) { result =>
          result mustEqual Right((): Unit)
        }
      }

      "when a user with expired sites adds and removes sites, updates all contact details and business address" in new URDOrchestratorHelper {
        val updatedContactDetails = ContactDetails(UPDATED_NAME, UPDATED_POSITION, UPDATED_PHONE_NUMBER, UPDATED_EMAIL)
        override val hasClosedSites: Boolean = true
        override val hasNewSites: Boolean = true
        override val hasRemovedSites = true
        override val optUpdatedContactDetails: Option[ContactDetails] = Some(updatedContactDetails)
        override val optUpdatedBusinessAddress: Option[UkAddress] = Some(UPDATED_ADDRESS)

        val expectVariationPDs = updatedPersonalDetails
        val expectVariationContact = VariationsContact(address = Some(UPDATED_ADDRESS), telephoneNumber = Some(UPDATED_PHONE_NUMBER), emailAddress = Some(UPDATED_EMAIL))
        val expectedSubscription = testVariationSubmission(variationContact = Some(expectVariationContact),
          variationsPersonalDetails = Some(expectVariationPDs), newSites = expectedNewSites, closeSites = expectedClosedSites)

        when(mockConnector.submitVariation(expectedSubscription, aSubscription.sdilRef)(hc)).thenReturn(createSuccessVariationResult((): Unit))
        val res = updateRegDetailsOrchestrator.submitVariation(getSubscription, userAnswers)(hc)
        whenReady(res.value) { result =>
          result mustEqual Right((): Unit)
        }

      }
    }
  }

  abstract class URDOrchestratorHelper {
    val hasClosedSites: Boolean = false
    val hasNewSites: Boolean = false
    val hasRemovedSites = false
    val optUpdatedContactDetails: Option[ContactDetails] = None
    val optUpdatedBusinessAddress: Option[UkAddress] = None

    def siteContactDetails = optUpdatedContactDetails.getOrElse(contactDetailsFromSubscription)


    val site1 = Site(contactAddress, Some("Site 1"), Some("12"), Some(localDate.plusYears(1)))
    val site2 = Site(contactAddress, Some("Site 2"), Some("13"), Some(localDate.plusYears(1)))

    val site3 = Site(contactAddress, Some("Site 3"), None, None)
    val site4 = Site(contactAddress, Some("Site 4"), None, None)

    val closedsite1 = Site(contactAddress, Some("Closed Site 1"), Some("14"), Some(localDate.minusMonths(1)))
    val closedsite2 = Site(contactAddress, Some("Closed Site 2"), Some("15"), Some(localDate.minusMonths(1)))

    def userAnswers = {
      val (productionSites: Map[String, Site], warehouses: Map[String, Site]) = (hasNewSites, hasRemovedSites) match {
        case (true, true) => (Map("2" -> site3), Map("2" -> site4))
        case (true, _) => (Map("1" -> site1, "2" -> site3), Map("1" -> site2, "2" -> site4))
        case (_, true) => (Map.empty, Map.empty)
        case _ => (Map("1" -> site1), Map("1" -> site2))
      }
      val baseUA = emptyUserAnswersForUpdateRegisteredDetails.copy(
        contactAddress = optUpdatedBusinessAddress.getOrElse(ORIGINAL_ADDRESS),
        packagingSiteList = productionSites,
        warehouseList = warehouses)

      optUpdatedContactDetails.fold(baseUA)(updContactDetails =>
        baseUA
          .set(UpdateContactDetailsPage, updContactDetails).success.value)

    }

    def getSubscription = {
      val productionSites = if(hasClosedSites) {
        List(site1, closedsite1)
      } else {
        List(site1)
      }
      val warehouses = if (hasClosedSites) {
        List(site2, closedsite2)
      } else {
        List(site2)
      }

      aSubscription.copy(
        address = ORIGINAL_ADDRESS,
        productionSites = productionSites,
        warehouseSites = warehouses
      )
    }

    def expectedNewSites: List[VariationsSite] = {
      if(hasNewSites) {
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

    def expectedClosedSites: List[ClosedSite] = {
      (hasRemovedSites, hasClosedSites) match {
        case (true, true) => List(site1, closedsite1, site2, closedsite2).map(ClosedSite.fromSite)
        case (false, true) => List(closedsite1, closedsite2).map(ClosedSite.fromSite)
        case (true, false) => List(site1, site2).map(ClosedSite.fromSite)
        case _ => List.empty[ClosedSite]
      }
    }
  }
}
