package controllers.addressLookupFrontend

import controllers.ControllerITTestHelper
import models.SelectChange.{ChangeActivity, UpdateRegisteredDetails}
import models.{CheckMode, NormalMode}
import models.backend.{Site, UkAddress}
import org.scalatest.matchers.must.Matchers._
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.{ContactDetails, PackingDetails, WarehouseDetails}


class RampOffControllerISpec extends ControllerITTestHelper {

  List(NormalMode, CheckMode).foreach(mode => {
    List(emptyUserAnswersForChangeActivity, emptyUserAnswersForUpdateRegisteredDetails).foreach(userAnswers => {
      val changeSelected = userAnswers.journeyType

      s"ramp off $WarehouseDetails in $changeSelected journey in $mode" - {
        "should redirect to next page when request is valid and address is returned from ALF when" - {
          "no address exists in DB currently for SDILID provided" in {
            val sdilId: String = "foo"
            val alfId: String = "bar"
            build
              .commonPrecondition
              .alf.getAddress(alfId)
            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }secondary-warehouses/$sdilId?id=$alfId")

              whenReady(result) { res =>
                val updatedUserAnswers = getAnswers(userAnswers.id).get
                updatedUserAnswers.id mustBe userAnswers.id
                updatedUserAnswers.data mustBe userAnswers.data
                updatedUserAnswers.packagingSiteList mustBe userAnswers.packagingSiteList
                updatedUserAnswers.submitted mustBe userAnswers.submitted
                updatedUserAnswers.smallProducerList mustBe userAnswers.smallProducerList
                updatedUserAnswers.warehouseList mustBe Map(sdilId -> Site(
                  UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), Some("soft drinks ltd")))

                res.status mustBe SEE_OTHER
                val expectedLocation =
                  if (changeSelected == ChangeActivity)
                    controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url
                  else
                    controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(mode).url

                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
              }

            }
          }
          "an address already exists in DB currently for SDILID provided" in {
            val sdilId: String = "foo"
            val alfId: String = "bar"
            val userAnswersBefore = userAnswers.copy(
              warehouseList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")))))
            build
              .commonPrecondition
              .alf.getAddress(alfId)
            setAnswers(userAnswersBefore)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }secondary-warehouses/$sdilId?id=$alfId")

              whenReady(result) { res =>
                val updatedUserAnswers = getAnswers(userAnswers.id).get
                updatedUserAnswers.id mustBe userAnswers.id
                updatedUserAnswers.data mustBe userAnswers.data
                updatedUserAnswers.packagingSiteList mustBe userAnswers.packagingSiteList
                updatedUserAnswers.submitted mustBe userAnswers.submitted
                updatedUserAnswers.smallProducerList mustBe userAnswers.smallProducerList
                updatedUserAnswers.warehouseList mustBe Map(sdilId -> Site(
                  UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), Some("soft drinks ltd")))

                res.status mustBe SEE_OTHER
                val expectedLocation =
                  if (changeSelected == ChangeActivity)
                    controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(mode).url
                  else
                    controllers.updateRegisteredDetails.routes.WarehouseDetailsController.onPageLoad(mode).url
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
              }
            }
          }
        }
        s"return $INTERNAL_SERVER_ERROR when" - {
          "alf returns error" in {
            val sdilId: String = "foo"
            val alfId: String = "bar"
            build
              .commonPrecondition
              .alf.getBadAddress(alfId)
            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }secondary-warehouses/$sdilId?id=$alfId")

              whenReady(result) { res =>
                res.status mustBe INTERNAL_SERVER_ERROR

                val updatedUserAnswers = getAnswers(userAnswers.id).get
                updatedUserAnswers.id mustBe userAnswers.id
                updatedUserAnswers.data mustBe userAnswers.data
                updatedUserAnswers.packagingSiteList mustBe userAnswers.packagingSiteList
                updatedUserAnswers.submitted mustBe userAnswers.submitted
                updatedUserAnswers.smallProducerList mustBe userAnswers.smallProducerList
                updatedUserAnswers.warehouseList mustBe userAnswers.warehouseList
              }
            }
          }
        }
      }

      s"ramp off $PackingDetails in $changeSelected journey in $mode should" - {
        "redirect to next page when request is valid and address is returned from ALF when" - {
          "no address exists in DB currently for SDILID provided" in {
            val sdilId: String = "foo"
            val alfId: String = "bar"
            build
              .commonPrecondition
              .alf.getAddress(alfId)
            setAnswers(userAnswers.copy(packagingSiteList = Map.empty))

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }packing-site-details/$sdilId?id=$alfId")

              whenReady(result) { res =>
                val updatedUserAnswers = getAnswers(userAnswers.id).get
                updatedUserAnswers.id mustBe userAnswers.id
                updatedUserAnswers.data mustBe userAnswers.data
                updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
                  Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, Some("soft drinks ltd"), None))
                updatedUserAnswers.submitted mustBe userAnswers.submitted
                updatedUserAnswers.smallProducerList mustBe userAnswers.smallProducerList
                updatedUserAnswers.warehouseList mustBe userAnswers.warehouseList

                res.status mustBe SEE_OTHER
                val expectedLocation =
                  if (changeSelected == ChangeActivity)
                    controllers.changeActivity.routes.PackagingSiteDetailsController.onPageLoad(mode).url
                  else
                    controllers.updateRegisteredDetails.routes.PackagingSiteDetailsController.onPageLoad(mode).url

                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
              }
            }
          }
        }

        s"return $INTERNAL_SERVER_ERROR when" - {
          "alf returns error" in {
            val sdilId: String = "foo"
            val alfId: String = "bar"
            build
              .commonPrecondition
              .alf.getBadAddress(alfId)
            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }packing-site-details/$sdilId?id=$alfId")

              whenReady(result) { res =>
                res.status mustBe INTERNAL_SERVER_ERROR

                val updatedUserAnswers = getAnswers(userAnswers.id).get
                updatedUserAnswers.id mustBe userAnswers.id
                updatedUserAnswers.data mustBe userAnswers.data
                updatedUserAnswers.packagingSiteList mustBe userAnswers.packagingSiteList
                updatedUserAnswers.submitted mustBe userAnswers.submitted
                updatedUserAnswers.smallProducerList mustBe userAnswers.smallProducerList
                updatedUserAnswers.warehouseList mustBe userAnswers.warehouseList
              }
            }
          }
        }
      }
    })

    s"ramp off $ContactDetails in UpdateRegisteredDetails journey in $mode" - {
      "should redirect to next page when request is valid and address is returned from ALF when" - {
        "no address exists in DB currently for SDILID provided" in {
          val sdilId: String = "foo"
          val alfId: String = "bar"
          build
            .commonPrecondition
            .alf.getAddress(alfId)
          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }business-address/$sdilId?id=$alfId")

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
          build
            .commonPrecondition
            .alf.getAddress(alfId)
          setAnswers(userAnswersBefore)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }business-address/$sdilId?id=$alfId")

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
          build
            .commonPrecondition
            .alf.getBadAddress(alfId)
          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/${if (mode == CheckMode) "change-" else "" }business-address/$sdilId?id=$alfId")

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
  })
}
