package controllers.changeActivity

import controllers.LitresISpecHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity._
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient


class HowManyImportsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-imports-next-12-months"
  val checkRoutePath = "/change-how-many-imports-next-12-months"

  val completedUserAnswers:UserAnswers = emptyUserAnswersForChangeActivity
    .set(AmountProducedPage, AmountProduced.Large).success.value
    .set(ThirdPartyPackagersPage, false).success.value
    .set(OperatePackagingSiteOwnBrandsPage, false).success.value
    .set(ContractPackingPage, false).success.value
    .set(ImportsPage, true).success.value

  List(normalRoutePath, checkRoutePath).foreach { path =>

    "GET " + path - {
      "when all previous required pages have been answered" - {
        "when the userAnswers contains no page data" - {
          "should return OK and render the litres page for Imports with no data populated" in {
            given
              .commonPrecondition

            setAnswers(completedUserAnswers)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("howManyImports" + ".title"))
                testLitresInBandsNoPrepopulatedData(page)
              }
            }
          }
        }

        s"when the userAnswers contains data for the page" - {
          s"should return OK and render the page with fields populated" in {
            given
              .commonPrecondition

            setAnswers(completedUserAnswers.set(HowManyImportsPage, litresInBands).success.value)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("howManyImports" + ".title"))
                testLitresInBandsWithPrepopulatedData(page)
              }
            }
          }
        }
        testUnauthorisedUser(changeActivityBaseUrl + path)
        testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + path)
        testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + path)
      }

      "when a page is missing from the userAnswers" - {
        s"should redirect to the $AmountProducedPage page" in {
          given
            .commonPrecondition

          setAnswers(completedUserAnswers.remove(AmountProducedPage).success.value)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.AmountProducedController.onPageLoad(NormalMode).url)
            }
          }
        }

        s"should redirect to the $ThirdPartyPackagersPage page" in {
          given
            .commonPrecondition
          val updatedAnswers = completedUserAnswers
            .remove(ThirdPartyPackagersPage).success.value
            .set(AmountProducedPage, AmountProduced.Small).success.value
          setAnswers(updatedAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
            }
          }
        }

        s"should redirect to the $OperatePackagingSiteOwnBrandsPage page" in {
          given
            .commonPrecondition

          setAnswers(completedUserAnswers.remove(OperatePackagingSiteOwnBrandsPage).success.value)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
            }
          }
        }

        s"should redirect to the $ContractPackingPage page" in {
          given
            .commonPrecondition

          setAnswers(completedUserAnswers.remove(ContractPackingPage).success.value)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
            }
          }
        }

        s"should redirect to the $ImportsPage page" in {
          given
            .commonPrecondition

          setAnswers(completedUserAnswers.remove(ImportsPage).success.value)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ImportsController.onPageLoad(NormalMode).url)
            }
          }
        }
      }
    }
  }

  s"POST " + normalRoutePath - {
    "when the user populates all litres fields" - {
      "should update the session with the new values" - {
        s"and redirect to $PackAtBusinessAddressPage" - {
          "when the user has no packaging sites" - {
            "and the user has answered previous required pages" - {
              "with large producer type, yes for own brands and no for copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .copy(packagingSiteList = Map.empty)
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                  .set(ContractPackingPage, false).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              "with large producer type, no for own brands and yes for copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .copy(packagingSiteList = Map.empty)
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              "with large producer type, yes for own brands and yes for copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .copy(packagingSiteList = Map.empty)
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with small producer type and true $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .copy(packagingSiteList = Map.empty)
                  .set(AmountProducedPage, AmountProduced.Small).success.value
                  .set(ThirdPartyPackagersPage, true).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              "with None producer type and yes for copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .copy(packagingSiteList = Map.empty)
                  .set(AmountProducedPage, AmountProduced.None).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }
            }
          }
        }

        "and redirect to packaging sites page" - {
          "when the user already packaging sites" - {
            "and the user has answered previous required pages" - {
              val userAnswersWithPackagingSite = emptyUserAnswersForChangeActivity.copy(packagingSiteList = packAtBusinessAddressSite)
              s"with large producer type, true $OperatePackagingSiteOwnBrandsPage and false $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                  .set(ContractPackingPage, false).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with large producer type, false $OperatePackagingSiteOwnBrandsPage and true $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with large producer type, true $OperatePackagingSiteOwnBrandsPage, and true $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with small producer type, false $OperatePackagingSiteOwnBrandsPage and true $ContractPackingPage " in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.Small).success.value
                  .set(ThirdPartyPackagersPage, true).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with small producer type, true $ThirdPartyPackagersPage, true $OperatePackagingSiteOwnBrandsPage, and true $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.Small).success.value
                  .set(ThirdPartyPackagersPage, true).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                  .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }

              s"with None producer type and true $ContractPackingPage" in {
                given
                  .commonPrecondition

                val userAnswers = userAnswersWithPackagingSite
                  .set(AmountProducedPage, AmountProduced.None).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe litresInBands
                  }
                }
              }
            }
          }
        }

        "and redirect to add secondary warehouse" - {
          "the user has answered previous required pages" - {
            s"with large producer type, false $OperatePackagingSiteOwnBrandsPage and false $ContractPackingPage" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            s"with small producer type, true $OperatePackagingSiteOwnBrandsPage and false $ContractPackingPage" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .copy(packagingSiteList = Map.empty)
                .set(AmountProducedPage, AmountProduced.Small).success.value
                .set(ThirdPartyPackagersPage, true).success.value
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, false).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            s"with small producer type, true $ThirdPartyPackagersPage, false $OperatePackagingSiteOwnBrandsPage, false $ContractPackingPage" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .copy(packagingSiteList = Map.empty)
                .set(AmountProducedPage, AmountProduced.Small).success.value
                .set(ThirdPartyPackagersPage, true).success.value
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            s"with None producer type and false $ContractPackingPage" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }
          }
        }

        "and redirect to the Amount produced controller" - {
          "when the session contains no data for any pages" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.AmountProducedController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForChangeActivity.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      val errorTitle = "Error: " + Messages("howManyImports.title")

      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, emptyJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testEmptyFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with no numeric answers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, jsonWithNoNumeric
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testNoNumericFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with negative numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, jsonWithNegativeNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testNegativeFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with decimal numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, jsonWithDecimalNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testDecimalFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with out of max range numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, jsonWithOutOfRangeNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testOutOfMaxValFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with 0" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, jsonWith0
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testZeroFormErrors(page, errorTitle)
          }
        }
      }
    }

    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
  }

  s"POST " + checkRoutePath - {
    "when the user populates all litres fields" - {
      "should update the session with the new values and redirect to cya page" - {
        "when the user has no packaging sites" - {
          "and the user has answered previous required pages" - {
            "with large producer type, yes for own brands and no for copacker" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                .set(ContractPackingPage, false).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with large producer type, no for own brands and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with large producer type, yes for own brands and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }
            "with None producer type and yes for co-packer" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with None producer type, yes for co-packer and has answered warehouse details" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(SecondaryWarehouseDetailsPage, true).success.value
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }
          }
        }

        "when the user already packaging sites" - {
          "and the user has answered previous required pages" - {
            val userAnswersWithPackagingSite = emptyUserAnswersForChangeActivity.copy(packagingSiteList = packAtBusinessAddressSite)
            "with large producer type, yes for own brands and no for copacker" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                .set(ContractPackingPage, false).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with large producer type, no for own brands and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with large producer type, yes for own brands and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.Large).success.value
                .set(OperatePackagingSiteOwnBrandsPage, true).success.value
                .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ThirdPartyPackagersPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with None producer type and yes for co-packer and no warehouse details answered" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with None producer type and yes for co-packer and warehouse details answered" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, true).success.value
                .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                .set(ImportsPage, true).success.value
                .set(SecondaryWarehouseDetailsPage, false).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with None producer type, no for Co-packer and Warehouse details answered" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ContractPackingPage, false).success.value
                .set(SecondaryWarehouseDetailsPage, false).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }
          }
        }

        "the user has answered previous required pages" - {
          "with large producer type, no for own brands, no for copacker and no warehouse details answers" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, false).success.value
              .set(ImportsPage, true).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "with large producer type, no for own brands , no for copacker and warehouse answers" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, false).success.value
              .set(ImportsPage, true).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session contains no data for any pages" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.AmountProducedController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForChangeActivity.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session contains data stating activity type Large, but no other pages" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session contains data stating activity type is Large and copacker" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(ContractPackingPage, false).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session contains data stating activity type is Large and own brands" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session contains data stating activity type is Small" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value
              .set(ContractPackingPage, false).success.value
              .set(ThirdPartyPackagersPage, true).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value
              .set(ImportsPage, true).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      val errorTitle = "Error: " + Messages("howManyImports.title")

      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, emptyJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testEmptyFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with no numeric answers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, jsonWithNoNumeric
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testNoNumericFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with negative numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, jsonWithNegativeNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testNegativeFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with decimal numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, jsonWithDecimalNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testDecimalFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with out of max range numbers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, jsonWithOutOfRangeNumber
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testOutOfMaxValFormErrors(page, errorTitle)
          }
        }
      }

      "when the user answers with 0" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, jsonWith0
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            testZeroFormErrors(page, errorTitle)
          }
        }
      }
    }

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiffObj)))
  }
}