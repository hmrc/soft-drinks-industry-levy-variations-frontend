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

package services

import base.SpecBase
import connectors.AddressLookupConnector
import controllers.routes
import models.{CheckMode, NormalMode}
import models.alf.init._
import models.alf.{AlfAddress, AlfResponse}
import models.backend.{Site, UkAddress}
import models.core.ErrorModel
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}

import scala.concurrent.Future

class AddressLookupServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout {

  val mockALFConnector: AddressLookupConnector = mock[AddressLookupConnector]
  val service = new AddressLookupService(mockALFConnector, frontendAppConfig)
  val organisation = "soft drinks ltd"
  val addressLine1 = "line 1"
  val addressLine2 = "line 2"
  val addressLine3 = "line 3"
  val addressLine4 = "line 4"
  val postcode = "aa1 1aa"
  val countryCode = "UK"
  val customerAddressMax: AlfResponse = AlfResponse(
    AlfAddress(
      Some(organisation),
      List(addressLine1, addressLine2, addressLine3, addressLine4),
      Some(postcode),
      Some(countryCode)
    ))

  "getAddress" - {
    "return an address when Connector returns success" in {
      when(mockALFConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Right(customerAddressMax)))

      val res = service.getAddress("123456789")

      whenReady(res) { result =>
        result mustBe customerAddressMax
      }
    }
    "return an exception when Connector returns error" in {
      when(mockALFConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))

      val res = intercept[Exception](await(service.getAddress("123456789")))
      res.getMessage mustBe "Error returned from ALF for 123456789 1 foo for None"
    }
  }

  "addAddressUserAnswers" - {
    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend where sdilId DOESN'T exist" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")))
      val addedWarehouse = Map(
        "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), Some(organisation)))

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMax.address,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehouseMap),
        sdilId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe addedWarehouse
    }
    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend where sdilId DOES exist" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map(sdilId ->
        Site(UkAddress(List("foo", "bar"), "wizz"), Some("super cola")))
      val updatedWarehouseMap = Map(sdilId ->
        Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), Some("soft drinks ltd")))

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMax.address,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehouseMap),
        sdilId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe updatedWarehouseMap
    }

    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend with missing address lines" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")))
      val addedWarehouseMissingLines = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2), postcode, alfId = Some(alfId)), Some(organisation)))
      val customerAddressMissingLines: AlfAddress =
        AlfAddress(
          Some(organisation),
          List(addressLine1, addressLine2),
          Some(postcode),
          Some(countryCode)
        )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehouseMap),
        sdilId = sdilId,
        alfId = alfId)

      res.warehouseList mustBe addedWarehouseMissingLines
    }

    s"add to the cache the address of a $WarehouseDetails when a user returns from address lookup frontend with full address lines" in {
      val addressLookupState = WarehouseDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")))

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = AlfAddress(Some(organisation),
          List(addressLine1, addressLine2, addressLine3, addressLine4),
          Some(postcode),
          Some(countryCode)
        ),
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehouseMap),
        alfId = alfId,
        sdilId = sdilId)

      res.warehouseList mustBe Map("1" -> Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")),
        sdilId -> Site(
          UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), Some(organisation)))
    }

    s"add to the cache the address of a $PackingDetails when a user returns from address lookup frontend with missing address lines" in {
      val addressLookupState = PackingDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val packingMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None))
      val addedPackingSiteMissingLines = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2), postcode, alfId = Some(alfId)), None, Some(organisation), None))
      val customerAddressMissingLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2),
        Some(postcode),
        Some(countryCode)
      )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(packagingSiteList = packingMap),
        alfId = alfId,
        sdilId = sdilId)

      res.packagingSiteList mustBe addedPackingSiteMissingLines
    }

    s"add to the cache the address of a $PackingDetails when a user returns from address lookup frontend with full address lines and sdilRef DOES exist" in {
      val addressLookupState = PackingDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val packingMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None))
      val addedPackingSite = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("ref1"), Some("super cola"), None),
        sdilId -> Site(UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, alfId = Some(alfId)), None, Some(organisation), None))

      val customerAddressMissingLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2, addressLine3, addressLine4),
        Some(postcode),
        Some(countryCode)
      )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMissingLines,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(packagingSiteList = packingMap),
        alfId = alfId,
        sdilId = sdilId)

      res.packagingSiteList mustBe addedPackingSite
    }

    s"add to the cache the address of a $ContactDetails when a user returns from address lookup frontend with only a couple address lines" in {
      val addressLookupState = ContactDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val contactAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")
      val updatedContactAddress = UkAddress(List(addressLine1, addressLine2), postcode, Some(alfId))
      val contactAddressSomeLines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2),
        Some(postcode),
        Some(countryCode)
      )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = contactAddressSomeLines,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = contactAddress),
        alfId = alfId,
        sdilId = sdilId)

      res.contactAddress mustBe updatedContactAddress
    }

    s"add to the cache the address of a $ContactDetails when a user returns from address lookup frontend with full address lines" in {
      val addressLookupState = ContactDetails
      val sdilId: String = "foo"
      val alfId: String = "bar"
      val contactAddress = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")
      val updatedContactAddress = UkAddress(List(addressLine1, addressLine2, addressLine3, addressLine4), postcode, Some(alfId))
      val contactAddress4Lines: AlfAddress = AlfAddress(
        Some(organisation),
        List(addressLine1, addressLine2, addressLine3, addressLine4),
        Some(postcode),
        Some(countryCode)
      )

      val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = contactAddress4Lines,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(contactAddress = contactAddress),
        alfId = alfId,
        sdilId = sdilId)

      res.contactAddress mustBe updatedContactAddress
    }

    "don't add to userAnswers when no details are added in alf and throw exception" in {
      val addressLookupState = WarehouseDetails
      val warehouseMap = Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"), Some("super cola")))
      val customerAddressMissingLinesAndName: AlfAddress = AlfAddress(
        None,
        List(),
        None,
        None
      )

      lazy val res = service.addAddressUserAnswers(addressLookupState = addressLookupState,
        address = customerAddressMissingLinesAndName,
        userAnswers = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehouseMap),
        alfId = "foo",
        sdilId = "bar")

      val errorMessage = "Not Found (Alf has returned an empty address and organisation name)"

      val result: String = intercept[Exception](res).getMessage

      result mustEqual errorMessage

    }
  }
  "initJourney" - {
    "should return response from connector" in {
      val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)
      when(mockALFConnector.initJourney(ArgumentMatchers.eq(journeyConfig))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right("foo")))

      whenReady(service.initJourney(journeyConfig)) {
        res => res mustBe Right("foo")
      }
    }
  }

  List(NormalMode, CheckMode).foreach(mode => {
    s"initJourneyAndReturnOnRampUrl in $mode" - {
      s"should return Successful future when connector returns success for $PackingDetails" in {
        val sdilId = "Foobar"
        val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
          version = frontendAppConfig.AddressLookupConfig.version,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }packing-site-details/$sdilId",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705bar"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK packaging site address"),
                    heading = Some("Find UK packaging site address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK packaging site address"),
                    heading = Some("Enter the UK packaging site address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Packaging site name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("foo")))
        whenReady(service.initJourneyAndReturnOnRampUrl(PackingDetails, sdilId, mode)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))) {
          res => res mustBe "foo"
        }
      }

      s"should return Successful future when connector returns success for $WarehouseDetails" in {
        val sdilId = "Foobar"
        val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }secondary-warehouses/$sdilId",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705bar"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK warehouse address"),
                    heading = Some("Find UK warehouse address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK warehouse address"),
                    heading = Some("Enter the UK warehouse address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Trading name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("foo")))
        whenReady(service.initJourneyAndReturnOnRampUrl(WarehouseDetails, sdilId, mode)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))) {
          res => res mustBe "foo"
        }
      }

      s"should return Successful future when connector returns success for $ContactDetails" in {
        val sdilId = "Foobar"
        val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }business-address/$sdilId",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705bar"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK contact address"),
                    heading = Some("Find UK contact address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                    heading = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = None)
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("foo")))
        whenReady(service.initJourneyAndReturnOnRampUrl(ContactDetails, sdilId, mode)(implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))) {
          res => res mustBe "foo"
        }
      }

      "should return Exception if connector returns left" in {
        when(mockALFConnector.initJourney(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))
        val res = intercept[Exception](await(service.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)
        (implicitly, implicitly, implicitly, FakeRequest("foo", "bar"))))
        res.getMessage mustBe "Failed to init ALF foo with status 1 for None"
      }

    }

    s"createJourneyConfig in $mode" - {
      s"should return a journey config for $WarehouseDetails" in {
        val request = FakeRequest("foo", "bar")
        val exampleSdilIdWeGenerate: String = "wizz"
        val res = service.createJourneyConfig(WarehouseDetails, exampleSdilIdWeGenerate, mode)(request, implicitly)
        val expected = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }secondary-warehouses/$exampleSdilIdWeGenerate",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705${request.uri}"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK warehouse address"),
                    heading = Some("Find UK warehouse address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK warehouse address"),
                    heading = Some("Enter the UK warehouse address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Trading name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        res mustBe expected
      }

      s"should return a journey config for $PackingDetails" in {
        val request = FakeRequest("foo", "bar")
        val exampleSdilIdWeGenerate: String = "wizz"
        val res = service.createJourneyConfig(PackingDetails, exampleSdilIdWeGenerate, mode)(request, implicitly)
        val expected = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }packing-site-details/$exampleSdilIdWeGenerate",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705${request.uri}"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK packaging site address"),
                    heading = Some("Find UK packaging site address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK packaging site address"),
                    heading = Some("Enter the UK packaging site address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Packaging site name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        res mustBe expected
      }

      s"should return a journey config for $ContactDetails" in {
        val request = FakeRequest("foo", "bar")
        val exampleSdilIdWeGenerate: String = "wizz"
        val res = service.createJourneyConfig(ContactDetails, exampleSdilIdWeGenerate, mode)(request, implicitly)
        val expected = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/${if (mode == CheckMode) "change-" else "" }business-address/$exampleSdilIdWeGenerate",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705${request.uri}"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(frontendAppConfig.AddressLookupConfig.alphaPhase),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = frontendAppConfig.timeout,
              timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(frontendAppConfig.sdilHomeUrl),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK contact address"),
                    heading = Some("Find UK contact address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                    heading = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = None)
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )

        res mustBe expected
      }

    }
  })

}
