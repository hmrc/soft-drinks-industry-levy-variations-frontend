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
import errors.{ReturnsStillPending, SessionDatabaseInsertError, UnexpectedResponseFromSDIL}
import models.backend.{RetrievedActivity, RetrievedSubscription, Site, UkAddress}
import models.updateRegisteredDetails.ContactDetails
import models.{Contact, SelectChange, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import services.SessionService

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class SelectChangeOrchestratorSpec extends SpecBase with MockitoSugar {

  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService: SessionService = mock[SessionService]

  val testTime = Instant.now()

  val orchestrator = new SelectChangeOrchestrator(mockSessionService, mockSdilConnector) {
    override def timeNow: Instant = testTime
  }

  val businessAddress = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX")
  val contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com")
  val updateContact = ContactDetails("Ava Adams", "Chief Infrastructure Agent", "04495 206189", "Adeline.Greene@gmail.com")

  val tradingName1 = "ABC Ltd"
  val tradingName2 = "DEF Ltd"
  val tradingName3 = "GHI Ltd"
  val tradingName4 = "JKL Ltd"

  val address1 = UkAddress(List("1 Test drive", "Example"), "EX1 1AB")
  val address2 = UkAddress(List("2 Test drive", "Example"), "EX2 2AB")
  val address3 = UkAddress(List("3 Test drive", "Example"), "EX3 3AB")
  val address4 = UkAddress(List("4 Test drive", "Example"), "EX4 4AB")

  def retrievedSubscription(productionSites: List[Site] = List.empty,
                            warehouses: List[Site] = List.empty,
                            deregDate: Option[LocalDate] = None) = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = businessAddress,
    activity = RetrievedActivity(
      smallProducer = false,
      largeProducer = true,
      contractPacker = false,
      importer = false,
      voluntaryRegistration = false
    ),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = productionSites,
    warehouseSites = warehouses,
    contact = contact,
    deregDate = deregDate
  )

  def expectedUserAnswers(value: SelectChange,
                          productionSites: Map[String, Site] = Map.empty,
                          warehouses: Map[String, Site] = Map.empty,
                          addContactDetails: Boolean = false): UserAnswers = {
    val data = if(addContactDetails) {
      Json.obj(("updateRegisteredDetails", Json.obj(("updateContactDetails", Json.toJson(updateContact)))))
    } else {
      Json.obj()
    }
    UserAnswers(
      "XKSDIL000000022",
      value,
      data = data,
      packagingSiteList = productionSites,
      warehouseList = warehouses,
      contactAddress = businessAddress,
      lastUpdated = testTime
    )
  }

  "hasReturnsToCorrect" - {
    "when the user has variable returns" - {
      "should return true" in {
        when(mockSdilConnector.returnsVariable("0000000022")(hc))
          .thenReturn(createSuccessVariationResult(returnPeriodList))

        val res = orchestrator.hasReturnsToCorrect(aSubscription)

        whenReady(res.value) {result =>
          result mustBe Right(true)
        }
      }
    }

    "when the user has no variable returns" - {
      "should return true" in {
        when(mockSdilConnector.returnsVariable("0000000022")(hc))
          .thenReturn(createSuccessVariationResult(List.empty))

        val res = orchestrator.hasReturnsToCorrect(aSubscription)

        whenReady(res.value) { result =>
          result mustBe Right(false)
        }
      }
    }

    "when the call to get variable returns fails" - {
      "should return UnexpectedResponseFromSDIL" in {
        when(mockSdilConnector.returnsVariable("0000000022")(hc))
          .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

        val res = orchestrator.hasReturnsToCorrect(aSubscription)

        whenReady(res.value) { result =>
          result mustBe Left(UnexpectedResponseFromSDIL)
        }
      }
    }
  }

  "createUserAnswersAndSaveToDatabase" - {
    SelectChange.values.foreach{ selectChange =>
      s"when the user selected to $selectChange" - {
        "should generate and save the expected user answers and return unit" - {
          "when the subscription contains no packaging sites or warehouses" in {
            if(selectChange == SelectChange.CancelRegistration) {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(List.empty))
            }
            val expectedGeneratedUA = expectedUserAnswers(selectChange, addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription())

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }

          "when the subscription contains packaging sites and warehouses that have no or closure dates in the future" in {
            val packagingSite1 = Site(address1, Some(tradingName1), None, None)
            val packagingSite2 = Site(address2, Some(tradingName2), None, Some(LocalDate.now().plusYears(2L)))
            val packagingSites = List(packagingSite1, packagingSite2)
            val warehouseSite1 = Site(address3, Some(tradingName3), None, None)
            val warehouseSite2 = Site(address4, Some(tradingName4), None, Some(LocalDate.now().plusYears(2L)))
            val warehouseSites = List(warehouseSite1, warehouseSite2)
            if (selectChange == SelectChange.CancelRegistration) {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(List.empty))
            }
            val expectedGeneratedUA = expectedUserAnswers(selectChange,
              Map("0" -> packagingSite1, "1" -> packagingSite2),
              Map("0" -> warehouseSite1, "1" -> warehouseSite2),
              addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription(packagingSites, warehouseSites))

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }

          "when the subscription contains packaging sites and warehouses that have one closure date in the past" in {
            val packagingSite1 = Site(address1, Some(tradingName1), None, None)
            val packagingSite2 = Site(address2, Some(tradingName2), None, Some(LocalDate.now().minusYears(2L)))
            val packagingSites = List(packagingSite1, packagingSite2)
            val warehouseSite1 = Site(address3, Some(tradingName3), None, None)
            val warehouseSite2 = Site(address4, Some(tradingName4), None, Some(LocalDate.now().minusYears(2L)))
            val warehouseSites = List(warehouseSite1, warehouseSite2)
            val warehouse1 = Site(address3, Some(tradingName3))
            if (selectChange == SelectChange.CancelRegistration) {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(List.empty))
            }
            val expectedGeneratedUA = expectedUserAnswers(selectChange,
              Map("0" -> packagingSite1),
              Map("0" -> warehouse1),
              addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

            val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription(packagingSites, warehouseSites))

            whenReady(res.value) { result =>
              result mustBe Right((): Unit)
            }
          }
        }
        if (selectChange == SelectChange.CancelRegistration) {
          "should return ReturnsStillPending error" - {
            "when the user has returns pending and is not voluntary registration" in {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(returnPeriodList))
              val expectedGeneratedUA = expectedUserAnswers(selectChange, addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
              when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

              val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription())

              whenReady(res.value) { result =>
                result mustBe Left(ReturnsStillPending)
              }
            }
          }

          "should generate and save the expected user answers and return unit" - {
            "when the user has returns pending and is voluntary registration" in {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(returnPeriodList))
              val expectedGeneratedUA = expectedUserAnswers(selectChange, addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
              when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

              val voluntaryRegistrationSubscription = retrievedSubscription()
                .copy(activity = retrievedSubscription().activity.copy(voluntaryRegistration = true))
              val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, voluntaryRegistrationSubscription)

              whenReady(res.value) { result =>
                result mustBe Right(())
              }
            }
          }

          "should generate and save the expected user answers and return unit" - {
            "when the user has no returns pending" in {
              when(mockSdilConnector.returnsPending("0000000022")(hc))
                .thenReturn(createSuccessVariationResult(List.empty))
              val expectedGeneratedUA = expectedUserAnswers(selectChange, addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
              when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

              val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription())

              whenReady(res.value) { result =>
                result mustBe Right(())
              }
            }
          }
        }

        "should return a SessionDatabaseInsertError" - {
          "when the insert to database fails" in {
            val expectedGeneratedUA = expectedUserAnswers(selectChange, addContactDetails = selectChange == SelectChange.UpdateRegisteredDetails)
            when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

            val res = orchestrator.createUserAnswersAndSaveToDatabase(selectChange, retrievedSubscription())

            whenReady(res.value) { result =>
              result mustBe Left(SessionDatabaseInsertError)
            }
          }
        }
      }
    }
  }

  "createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase" - {
    "should generate and save the expected user answers and return the generated user answers" - {
      "when the subscription contains no packaging sites or warehouses" in {
        val expectedGeneratedUA = expectedUserAnswers(SelectChange.CorrectReturn)
        when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

        val res = orchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(retrievedSubscription(deregDate = Some(LocalDate.now())))

        whenReady(res.value) { result =>
          result mustBe Right(expectedGeneratedUA)
        }
      }

      "when the subscription contains packaging sites and warehouses that have no or closure dates in the future" in {
        val packagingSite1 = Site(address1, Some(tradingName1), None, None)
        val packagingSite2 = Site(address2, Some(tradingName2), None, Some(LocalDate.now().plusYears(2L)))
        val packagingSites = List(packagingSite1, packagingSite2)
        val warehouseSite1 = Site(address3, Some(tradingName3), None, None)
        val warehouseSite2 = Site(address4, Some(tradingName4), None, Some(LocalDate.now().plusYears(2L)))
        val warehouseSites = List(warehouseSite1, warehouseSite2)
        val expectedGeneratedUA = expectedUserAnswers(SelectChange.CorrectReturn)
        when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

        val res = orchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(retrievedSubscription(packagingSites, warehouseSites, deregDate = Some(LocalDate.now)))

        whenReady(res.value) { result =>
          result mustBe Right(expectedGeneratedUA)
        }
      }

      "when the subscription contains packaging sites and warehouses that have one closure date in the past" in {
        val packagingSite1 = Site(address1, Some(tradingName1), None, None)
        val packagingSite2 = Site(address2, Some(tradingName2), None, Some(LocalDate.now().minusYears(2L)))
        val packagingSites = List(packagingSite1, packagingSite2)
        val warehouseSite1 = Site(address3, Some(tradingName3), None, None)
        val warehouseSite2 = Site(address4, Some(tradingName4), None, Some(LocalDate.now().minusYears(2L)))
        val warehouseSites = List(warehouseSite1, warehouseSite2)
        val expectedGeneratedUA = expectedUserAnswers(SelectChange.CorrectReturn)
        when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Right(true)))

        val res = orchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(retrievedSubscription(packagingSites, warehouseSites, deregDate = Some(LocalDate.now)))

        whenReady(res.value) { result =>
          result mustBe Right(expectedGeneratedUA)
        }
      }
    }

    "should return a SessionDatabaseInsertError" - {
      "when the insert to database fails" in {
        val expectedGeneratedUA = expectedUserAnswers(SelectChange.CorrectReturn)
        when(mockSessionService.set(expectedGeneratedUA)).thenReturn(Future.successful(Left(SessionDatabaseInsertError)))

        val res = orchestrator.createCorrectReturnUserAnswersForDeregisteredUserAndSaveToDatabase(retrievedSubscription())

        whenReady(res.value) { result =>
          result mustBe Left(SessionDatabaseInsertError)
        }
      }
    }
  }

}
