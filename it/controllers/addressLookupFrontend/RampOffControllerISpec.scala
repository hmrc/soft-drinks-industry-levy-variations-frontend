package controllers.addressLookupFrontend

import controllers.ControllerITTestHelper
import models.NormalMode
import models.backend.{Site, UkAddress}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.{ContactDetails, PackingDetails, WarehouseDetails}


class RampOffControllerISpec extends ControllerITTestHelper {

  s"ramp off $WarehouseDetails" - {
    "should redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Site(
              UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), Some("soft drinks ltd")))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswersForUpdateRegisteredDetails.copy(
          warehouseList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")))))
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(userAnswersBefore)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Site(
              UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), Some("soft drinks ltd")))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/secondary-warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList
          }
        }
      }
    }
  }

  s"ramp off $PackingDetails should" - {
    "redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(packagingSiteList = Map.empty))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
              Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }

      "redirect to next page when request is valid and address is returned from ALF when in the ChangeActivity journey" - {
        "no address exists in DB currently for SDILID provided" in {
          val sdilId: String = "foo"
          val alfId: String = "bar"
          given
            .commonPrecondition
            .alf.getAddress(alfId)
          setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswersForChangeActivity.id).get
              updatedUserAnswers.id mustBe emptyUserAnswersForChangeActivity.id
              updatedUserAnswers.data mustBe emptyUserAnswersForChangeActivity.data
              updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
                Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
              updatedUserAnswers.submitted mustBe emptyUserAnswersForChangeActivity.submitted
              updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForChangeActivity.smallProducerList
              updatedUserAnswers.warehouseList mustBe emptyUserAnswersForChangeActivity.warehouseList

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
            }
          }
        }

        "an address already exists in DB currently for SDILID provided" in {
          val sdilId: String = "foo"
          val alfId: String = "bar"
          val userAnswersBefore = emptyUserAnswersForUpdateRegisteredDetails.copy(
            packagingSiteList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, None, None)))
          given
            .commonPrecondition
            .alf.getAddress(alfId)
          setAnswers(userAnswersBefore)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
              updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
              updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
              updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
                Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
              updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
              updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
              updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
            }
          }
        }
      }
    }

    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList
          }
        }
      }
    }
  }

  s"ramp off $ContactDetails" - {
    "should redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
           WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/business-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswersNoContactDetails = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswersNoContactDetails.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswersNoContactDetails.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswersNoContactDetails.contactAddress mustBe UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", Some("bar"))
            updatedUserAnswersNoContactDetails.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswersNoContactDetails.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswersNoContactDetails.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswersNoContactDetails.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.BusinessAddressController.onPageLoad().url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswersForUpdateRegisteredDetails.copy(
          contactAddress = UkAddress(List.empty, "foo", Some("wizz")))
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(userAnswersBefore)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/business-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.contactAddress mustBe UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.BusinessAddressController.onPageLoad().url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/business-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get
            updatedUserAnswers.id mustBe emptyUserAnswersForUpdateRegisteredDetails.id
            updatedUserAnswers.data mustBe emptyUserAnswersForUpdateRegisteredDetails.data
            updatedUserAnswers.contactAddress mustBe emptyUserAnswersForUpdateRegisteredDetails.contactAddress
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswersForUpdateRegisteredDetails.packagingSiteList
            updatedUserAnswers.submitted mustBe emptyUserAnswersForUpdateRegisteredDetails.submitted
            updatedUserAnswers.smallProducerList mustBe emptyUserAnswersForUpdateRegisteredDetails.smallProducerList
            updatedUserAnswers.warehouseList mustBe emptyUserAnswersForUpdateRegisteredDetails.warehouseList
          }
        }
      }
    }
  }
}
