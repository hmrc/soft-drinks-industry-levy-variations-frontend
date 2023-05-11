package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import models.$className$

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$className;format="decap"$"
  val checkRoutePath = "/change$className$"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="decap"$" + ".title"))
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe $className$.values.size

            $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    $className$.values.zipWithIndex.foreach { case (checkboxItem, index) =>
      s"when the userAnswers contains data for the page with " + checkboxItem.toString + " selected" - {
        s"should return OK and render the page with " + checkboxItem.toString + " checkboxItem checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set($className$Page, Set(checkboxItem)).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$className;format="
              decap"$" + ".title"
              ) )
              val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
              checkBoxInputs.size() mustBe $className$.values.size

              $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
                checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
                checkBoxInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }

    "when the userAnswers contains data for the page with all checkbox items" - {
      "should return OK and render the page with all checkboxes checked" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set($className$Page, $className$.values.toSet).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="
            decap"$" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe $className$.values.size

            $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe true
            }
          }
        }
      }
    }

    testUnauthorisedUserGET(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="
            decap"$" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe $className$.values.size

            $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    $className$.values.zipWithIndex.foreach { case (checkboxItem, index) =>
      s"when the userAnswers contains data for the page with " + checkboxItem.toString + " selected" - {
        s"should return OK and render the page with " + checkboxItem.toString + " checkboxItem checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set($className$Page, Set(checkboxItem)).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$className;format="
              decap"$" + ".title"
              ) )
              val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
              checkBoxInputs.size() mustBe $className$.values.size

              $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
                checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
                checkBoxInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }

    "when the userAnswers contains data for the page with all checkbox items" - {
      "should return OK and render the page with all checkboxes checked" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set($className$Page, $className$.values.toSet).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="
            decap"$" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe $className$.values.size

            $className$.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe true
            }
          }
        }
      }
    }

    testUnauthorisedUserGET(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    $className$.values.foreach { case checkboxItem =>
      "when the user selects " + checkboxItem.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set($className$Page, Set(checkboxItem)).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }
        }
      }
    }

    "when the user selects all checkboxItems" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> $className$.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className$.values.toSet
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set($className$Page, $className$.values.toSet).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> $className$.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className$.values.toSet
            }
          }
        }
      }
    }

    "when the user does not select any checkbox" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe Messages("$className;format="decap"$" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUserPOST(baseUrl + normalRoutePath, Json.obj("value" -> "true"))
  }

  s"POST " + checkRoutePath - {
    $className$.values.foreach { case checkboxItem =>
      "when the user selects " + checkboxItem.toString - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set($className$Page, Set(checkboxItem)).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }
        }
      }
    }

    "when the user selects all checkboxItems" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> $className$.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className$.values.toSet
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set($className$Page, $className$.values.toSet).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> $className$.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[$className$]]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className$.values.toSet
            }
          }
        }
      }
    }

    "when the user does not select any checkbox" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$className;format="
            decap"$" + ".title"
            ) )
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe Messages("$className;format="
            decap"$" + ".error.required"
            )
          }
        }
      }
    }

    testUnauthorisedUserPOST(baseUrl + checkRoutePath, Json.obj("value" -> "true"))
  }
}
