package controllers.changeActivity

import controllers.LitresISpecHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{LitresInBands, NormalMode, UserAnswers}
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

  val userAnswers:UserAnswers = emptyUserAnswersForChangeActivity.set(HowManyImportsPage, litresInBands).success.value

  List(normalRoutePath, checkRoutePath).foreach { path =>

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for Imports with no data populated" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)

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

          setAnswers(userAnswers)

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
  }

  s"POST " + normalRoutePath - {
    "when the user populates all litres fields" - {
      "should update the session with the new values" - {
        s"and redirect to pack at business address page" - {
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
                  .set(ThirdPartyPackagersPage, false).success.value
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
                  .set(ThirdPartyPackagersPage, false).success.value
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
                  .set(ThirdPartyPackagersPage, false).success.value
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
              "with None producer type and yes for copacker" in {
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
            "with large producer type, no for own brands and no for copacker" in {
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
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe litresInBands
                }
              }
            }

            "with None producer type and no for copacker" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
                .set(AmountProducedPage, AmountProduced.None).success.value
                .set(ImportsPage, true).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad.url)
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
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }
        }

        "and redirect to own brands controller" - {
          s"when the session contains data stating activity type Large, but no other pages" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          s"when the session contains data stating activity type is Large and copacker" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(ContractPackingPage, false).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }
        }

        "and redirect to copacker page" - {
          s"when the session contains data stating activity type is Large and own brands" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Large).success.value
              .set(OperatePackagingSiteOwnBrandsPage, false).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }
        }

        "redirect to default page" - {
          s"when the session contains data stating activity type is Small" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
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
            "with None producer type and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForChangeActivity
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

            "with None producer type and yes for copacker" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
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

            "with None producer type and no for copacker" in {
              given
                .commonPrecondition

              val userAnswers = userAnswersWithPackagingSite
                .set(AmountProducedPage, AmountProduced.None).success.value
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
          }
        }

        "the user has answered previous required pages" - {
          "with large producer type, no for own brands and no for copacker" in {
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
                res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          s"when the session contains data stating activity type Large, but no other pages" in {
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

          s"when the session contains data stating activity type is Large and copacker" in {
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
          s"when the session contains data stating activity type is Large and own brands" in {
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

          s"when the session contains data stating activity type is Small" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForChangeActivity
              .set(AmountProducedPage, AmountProduced.Small).success.value

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
