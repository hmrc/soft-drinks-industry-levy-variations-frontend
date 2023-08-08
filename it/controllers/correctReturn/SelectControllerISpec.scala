package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.{LitresInBands, NormalMode, ReturnPeriod, SdilReturn}
import models.SelectChange.CorrectReturn
import models.correctReturn.CorrectReturnUserAnswersData
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import testSupport.SDILBackendTestData.{aSubscription, emptyReturn, returnPeriodList, returnPeriods, smallProducerList, submittedDateTime, subscriptionSmallProducer}

import java.time.ZoneOffset


class SelectControllerISpec extends ControllerITTestHelper {

  val routePath = "/select"
  val returnPeriodYears: List[Int] = returnPeriodList.map(_.year).distinct.sorted
  val sortedReturnPeriods = returnPeriodList.distinct.sortBy(_.start)

  def expectedText(returnPeriod: ReturnPeriod) = returnPeriod.quarter match {
    case 0 => s"January to March $year"
    case 1 => s"April to June $year"
    case 2 => s"July to September $year"
    case _ => s"October to December $year"
  }

  val populatedReturn = SdilReturn((100, 200), (200, 100), smallProducerList, (300, 400), (400, 300), (50, 60), (60, 50), submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))

  val expectedCorrectReturnDataForNilReturn = CorrectReturnUserAnswersData(false, None, false, None, false, false, None, false, None, false, None, false, None)
  val expectedCorrectReturnDataForPopulatedReturn = CorrectReturnUserAnswersData(
    true, Some(LitresInBands(100, 200)),
    true, Some(LitresInBands(200, 100)),
    true,
    true, Some(LitresInBands(300, 400)),
    true, Some(LitresInBands(400, 300)),
    true, Some(LitresInBands(50, 60)),
    true, Some(LitresInBands(60, 50))
  )

  val sdilReturnsExamples = Map("a nilReturn" -> emptyReturn, "not a nilReturn" -> populatedReturn)
  def getExpectedUserAnswersCorrectReturnData(key: String): CorrectReturnUserAnswersData = if(key == "a nilReturn") {
    expectedCorrectReturnDataForNilReturn
  } else {
    expectedCorrectReturnDataForPopulatedReturn
  }



  "GET " + routePath - {
    "should render the select page with radio items for each unique return period" - {
      "that has no return periods checked" - {
        "when the user answers contains no data for the page and no repeated return period" in {
          given
            .commonPrecondition
            .sdilBackend.returns_variable(UTR)

          setAnswers(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + routePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() mustBe "Which return do you need to correct? - Soft Drinks Industry Levy - GOV.UK"
              val radioDivider = page.getElementsByClass("govuk-radios__divider")
              radioDivider.size() mustBe returnPeriodYears.size
              returnPeriodYears.zipWithIndex.foreach { case (year, index) =>
                radioDivider.get(index).text() mustBe year.toString
              }
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe returnPeriodList.size
              sortedReturnPeriods.zipWithIndex.foreach { case (returnPeriod, index) =>
                val radioItem = radioInputs.get(index)
                radioItem.attr("value") mustBe returnPeriod.radioValue
                radioItem.hasAttr("checked") mustBe false
              }
            }
          }
        }

        "when the user answers contains no data for the page and has repeated return periods" in {
          val returnsPeriodWithRepeats = returnPeriodList ++ returnPeriodList
          given
            .commonPrecondition
            .sdilBackend.returns_variable(UTR, returnsPeriodWithRepeats)

          setAnswers(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + routePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() mustBe "Which return do you need to correct? - Soft Drinks Industry Levy - GOV.UK"
              val radioDivider = page.getElementsByClass("govuk-radios__divider")
              radioDivider.size() mustBe returnPeriodYears.size
              returnPeriodYears.zipWithIndex.foreach { case (year, index) =>
                radioDivider.get(index).text() mustBe year.toString
              }
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe returnPeriodList.size
              sortedReturnPeriods.zipWithIndex.foreach { case (returnPeriod, index) =>
                val radioItem = radioInputs.get(index)
                radioItem.attr("value") mustBe returnPeriod.radioValue
                radioItem.hasAttr("checked") mustBe false
              }
            }
          }
        }
      }

      returnPeriodList.foreach { selectedReturnPeriod =>
        s"with ${selectedReturnPeriod.radioValue} checked" - {
          s"when the user answers contain return period for ${selectedReturnPeriod.radioValue}" in {
            given
              .commonPrecondition
              .sdilBackend.returns_variable(UTR)

            val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(selectedReturnPeriod))

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, correctReturnBaseUrl + routePath)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title() mustBe "Which return do you need to correct? - Soft Drinks Industry Levy - GOV.UK"
                val radioDivider = page.getElementsByClass("govuk-radios__divider")
                radioDivider.size() mustBe returnPeriodYears.size
                returnPeriodYears.zipWithIndex.foreach { case (year, index) =>
                  radioDivider.get(index).text() mustBe year.toString
                }
                val radioInputs = page.getElementsByClass("govuk-radios__input")
                radioInputs.size() mustBe returnPeriodList.size
                sortedReturnPeriods.zipWithIndex.foreach { case (returnPeriod, index) =>
                  val radioItem = radioInputs.get(index)
                  radioItem.attr("value") mustBe returnPeriod.radioValue
                  radioItem.hasAttr("checked") mustBe returnPeriod.radioValue == selectedReturnPeriod.radioValue
                }
              }
            }
          }
        }
      }
    }

    "should redirect to the select change page" - {
      "when the are no variable returns" in {
        given
          .commonPrecondition
          .sdilBackend.no_returns_variable(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + routePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SelectChangeController.onPageLoad.url)
          }
        }
      }
    }

    "should render the internal server error page" - {
      "when the call to get variable returns fails" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable_error(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + routePath)

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }
    }

    testUnauthorisedUser(correctReturnBaseUrl + routePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + routePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + routePath)
  }

  s"POST " + routePath - {
    returnPeriodList.foreach { case returnPeriod =>
      "when the user selects " + returnPeriod.toString - {
        sdilReturnsExamples.foreach { case (key, sdilReturn) =>
          s"and the previous return was $key" - {
            "should update the session with the new value and redirect to own brands controller" - {
              "when the session contains no data for page and the user is not a small producer" in {
                given
                  .commonPrecondition
                  .sdilBackend.returns_variable(UTR)
                  .sdilBackend.retrieveReturn(UTR, returnPeriod, Some(sdilReturn))

                setAnswers(emptyUserAnswersForCorrectReturn)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriod.radioValue)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                    val updatedUserAnswers = getAnswers(sdilNumber).get
                    updatedUserAnswers.correctReturnPeriod mustBe Some(returnPeriod)
                    val expectedSmallProducerList = if (key == "a nilReturn") {
                      List.empty
                    } else {
                      smallProducerList
                    }
                    updatedUserAnswers.smallProducerList mustBe expectedSmallProducerList
                    updatedUserAnswers.getCorrectReturnData.get mustBe getExpectedUserAnswersCorrectReturnData(key)
                  }
                }
              }

              "when the session already contains data for page and the user is not a small producer" in {
                given
                  .commonPrecondition
                  .sdilBackend.returns_variable(UTR)
                  .sdilBackend.retrieveReturn(UTR, returnPeriod, Some(sdilReturn))

                val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod))

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriod.radioValue)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSiteOwnBrandsController.onPageLoad(NormalMode).url)
                    val updatedUserAnswers = getAnswers(sdilNumber).get
                    updatedUserAnswers.correctReturnPeriod mustBe Some(returnPeriod)
                    val expectedSmallProducerList = if (key == "a nilReturn") {
                      List.empty
                    } else {
                      smallProducerList
                    }
                    updatedUserAnswers.smallProducerList mustBe expectedSmallProducerList
                    updatedUserAnswers.getCorrectReturnData.get mustBe getExpectedUserAnswersCorrectReturnData(key)
                  }
                }
              }
            }

            "should update the session with the new value and redirect to copacks controller" - {
              "when the session contains no data for page and the user is a small producer" in {
                given
                  .commonPreconditionChangeSubscription(subscriptionSmallProducer)
                  .sdilBackend.returns_variable(UTR)
                  .sdilBackend.retrieveReturn(UTR, returnPeriod, Some(sdilReturn))

                setAnswers(emptyUserAnswersForCorrectReturn)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriod.radioValue)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url)
                    val updatedUserAnswers = getAnswers(sdilNumber).get
                    updatedUserAnswers.correctReturnPeriod mustBe Some(returnPeriod)
                    val expectedSmallProducerList = if (key == "a nilReturn") {
                      List.empty
                    } else {
                      smallProducerList
                    }
                    updatedUserAnswers.smallProducerList mustBe expectedSmallProducerList
                    updatedUserAnswers.getCorrectReturnData.get mustBe getExpectedUserAnswersCorrectReturnData(key)
                  }
                }
              }

              "when the session already contains data for page and the user is not a small producer" in {
                given
                  .commonPreconditionChangeSubscription(subscriptionSmallProducer)
                  .sdilBackend.returns_variable(UTR)
                  .sdilBackend.retrieveReturn(UTR, returnPeriod, Some(sdilReturn))

                val userAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = Some(returnPeriod))

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriod.radioValue)
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url)
                    val updatedUserAnswers = getAnswers(sdilNumber).get
                    updatedUserAnswers.correctReturnPeriod mustBe Some(returnPeriod)
                    val expectedSmallProducerList = if (key == "a nilReturn") {
                      List.empty
                    } else {
                      smallProducerList
                    }
                    updatedUserAnswers.smallProducerList mustBe expectedSmallProducerList
                    updatedUserAnswers.getCorrectReturnData.get mustBe getExpectedUserAnswersCorrectReturnData(key)
                  }
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select an option" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.select" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0_0"
            errorSummary.text() mustBe Messages("correctReturn.select" + ".error.required")
          }
        }
      }
    }

    "when the user return is not in the list of variable returns" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val diffReturnPeriod = ReturnPeriod(2018, 1)
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> diffReturnPeriod.radioValue)
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.select" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0_0"
            errorSummary.text() mustBe Messages("correctReturn.select" + ".error.required")
          }
        }
      }
    }

    "should redirect to the select change page" - {
      "when the are no variable returns" in {
        given
          .commonPrecondition
          .sdilBackend.no_returns_variable(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriodList.head.radioValue)
          )

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SelectChangeController.onPageLoad.url)
          }
        }
      }
    }

    "should render the internal server error page" - {
      "when there is no sdilReturn for returnPeriod" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable(UTR)
          .sdilBackend.retrieveReturn(UTR, returnPeriodList.head, None)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriodList.head.radioValue)
          )

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }

      "when the call to get sdilReturn fails" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable(UTR)
          .sdilBackend.retrieveReturnError(UTR, returnPeriodList.head)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriodList.head.radioValue)
          )

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }

      "when the call to get variable returns fails" in {
        given
          .commonPrecondition
          .sdilBackend.returns_variable_error(UTR)

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestPOST(
            client, correctReturnBaseUrl + routePath, Json.obj("value" -> returnPeriodList.head.radioValue)
          )

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + routePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + routePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + routePath, Some(Json.obj("value" -> "true")))
  }
}
