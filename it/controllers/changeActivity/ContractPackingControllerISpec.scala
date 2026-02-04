package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{ CheckMode, NormalMode, UserAnswers }
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.changeActivity._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ContractPackingControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/contract-packing"
  val checkRoutePath = "/change-contract-packing"
  val contractPackingJourneyUserAnswers: UserAnswers = emptyUserAnswersForChangeActivity
    .set(AmountProducedPage, AmountProduced.Small)
    .success
    .value
    .set(ThirdPartyPackagersPage, true)
    .success
    .value
    .set(OperatePackagingSiteOwnBrandsPage, false)
    .success
    .value
    .set(ImportsPage, true)
    .success
    .value
  val contractPackingJourneyUserAnswersWithPage: Map[String, UserAnswers] = {
    val yesSelected = contractPackingJourneyUserAnswers.set(ContractPackingPage, true).success.value
    val noSelected = contractPackingJourneyUserAnswers.set(ContractPackingPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ContractPacking page with no data populated when prior answers are completed" in {
        build.commonPrecondition

        setAnswers(contractPackingJourneyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe
              "Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
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

    contractPackingJourneyUserAnswersWithPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build.commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe
                "Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
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
      "should return OK and render the ContractPacking page with no data populated" in {
        build.commonPrecondition

        setAnswers(contractPackingJourneyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe
              "Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
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

    contractPackingJourneyUserAnswersWithPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build.commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe
                "Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
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
    contractPackingJourneyUserAnswersWithPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the HowManyContractPacking controller" - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setAnswers(contractPackingJourneyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + normalRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyContractPackingController.onPageLoad(NormalMode).url
                } else {
                  routes.ImportsController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ContractPackingPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            setAnswers(contractPackingJourneyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + normalRoutePath,
                Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyContractPackingController.onPageLoad(NormalMode).url
                } else {
                  routes.ImportsController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ContractPackingPage))
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
        build.commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + normalRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe
              "Error: Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe
              "Select yes if you operate any packaging sites in the UK to package liable drinks as a third party or contract packer"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + normalRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }

  s"POST " + checkRoutePath - {

    contractPackingJourneyUserAnswersWithPage
      .foreach { case (key, userAnswers) =>
        "when the user selects " + key - {
          val yesSelected = key == "yes"
          "should update the session with the new value and redirect to the checkAnswers controller" - {
            "when the session contains no data for page" in {
              build.commonPrecondition

              setAnswers(contractPackingJourneyUserAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + checkRoutePath,
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  val expectedLocation = if (yesSelected) {
                    routes.HowManyContractPackingController.onPageLoad(CheckMode).url
                  } else {
                    routes.ChangeActivityCYAController.onPageLoad().url
                  }
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                  val dataStoredForPage =
                    getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ContractPackingPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }

            "when the session already contains data for page" in {
              build.commonPrecondition

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client,
                  changeActivityBaseUrl + checkRoutePath,
                  Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  val expectedLocation = if (yesSelected) {
                    routes.HowManyContractPackingController.onPageLoad(CheckMode).url
                  } else {
                    routes.ChangeActivityCYAController.onPageLoad().url
                  }
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                  val dataStoredForPage =
                    getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ContractPackingPage))
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
        build.commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            changeActivityBaseUrl + checkRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe
              "Error: Do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe
              "Select yes if you operate any packaging sites in the UK to package liable drinks as a third party or contract packer"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      ChangeActivity,
      changeActivityBaseUrl + checkRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }
}
