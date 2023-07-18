package controllers.changeActivity

import controllers.LitresISpecHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyContractPackingPage, HowManyImportsPage, HowManyOperatePackagingSiteOwnBrandsPage, ImportsPage, OperatePackagingSiteOwnBrandsPage, ThirdPartyPackagersPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient


class HowManyImportsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-imports-next-12-months"
  val checkRoutePath = "/change-how-many-imports-next-12-months"

  val userAnswers:UserAnswers = emptyUserAnswersForChangeActivity.set(HowManyImportsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if (mode == NormalMode) {
      (normalRoutePath, defaultCall.url)
    } else {
      (checkRoutePath, routes.ChangeActivityCYAController.onPageLoad.url)
    }

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
            "and the user has answered previous requied pages" - {
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
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                  .set(AmountProducedPage, AmountProduced.Large).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value
                  .set(ContractPackingPage, true).success.value
                  .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
                  .set(ThirdPartyPackagersPage, false).success.value
                  .set(ImportsPage, true).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                    client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + normalRoutePath, Json.toJson(litresInBands)
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
    }

    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiff)))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiff)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.toJson(litresInBandsDiff)))
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                  client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
                client, changeActivityBaseUrl + checkRoutePath, Json.toJson(litresInBands)
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
    }

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiff)))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiff)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.toJson(litresInBandsDiff)))
  }
}
