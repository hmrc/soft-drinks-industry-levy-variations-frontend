package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.{AmountProducedPage, ContractPackingPage, HowManyContractPackingPage, HowManyOperatePackagingSiteOwnBrandsPage, ImportsPage, OperatePackagingSiteOwnBrandsPage, ThirdPartyPackagersPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ImportsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/imports"
  val checkRoutePath = "/change-imports"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.imports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.imports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.imports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.imports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user selects no" - {
      "should update the session with the new value" - {
        "and redirect to pack at business address page" - {
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
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

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe false
                }
              }
            }
          }
        }

        "and redirect to the index controller" - {
          "when the session contains no data for any pages" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForChangeActivity.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe false
              }
            }
          }
        }

        AmountProduced.values.foreach { amountProduced =>
          if (amountProduced == AmountProduced.Large) {
            "and redirect to own brands controller" - {
              s"when the session contains data stating activity type ${amountProduced.toString}, but no other pages" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }

              s"when the session contains data stating activity type ${amountProduced.toString} and copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value
                  .set(ContractPackingPage, false).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }
            }

            "and redirect to copacker page" - {
              s"when the session contains data stating activity type ${amountProduced.toString} and own brands" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }
            }
          } else {
            "redirect to default page" - {
              s"when the session contains data stating activity type ${amountProduced.toString}, but no other pages" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }

              s"when the session contains data stating activity type ${amountProduced.toString} and copacker" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value
                  .set(ContractPackingPage, false).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }
            }

            "and redirect to copacker page" - {
              s"when the session contains data stating activity type ${amountProduced.toString} and own brands" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswersForChangeActivity
                  .set(AmountProducedPage, amountProduced).success.value
                  .set(OperatePackagingSiteOwnBrandsPage, false).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe false
                  }
                }
              }
            }
          }
        }
      }
    }

    "when the user selects yes" - {
      "should redirect to the howManyImports page" - {
        "when the session does not contain data for the page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForChangeActivity
            .set(AmountProducedPage, AmountProduced.Large).success.value
            .set(OperatePackagingSiteOwnBrandsPage, true).success.value
            .set(HowManyOperatePackagingSiteOwnBrandsPage, LitresInBands(100, 100)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(100, 100)).success.value
            .set(ThirdPartyPackagersPage, false).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "true")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyImportsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe true
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.imports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.imports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForChangeActivityImportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        val yesSelected = key == "yes"
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if(yesSelected) {
                  routes.HowManyImportsController.onPageLoad(CheckMode).url
                } else {
                  routes.ChangeActivityCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(CheckMode).url
                } else {
                  routes.ChangeActivityCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.imports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.imports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
