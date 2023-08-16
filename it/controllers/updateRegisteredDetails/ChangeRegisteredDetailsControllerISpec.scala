package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.{NormalMode, RetrievedActivity}
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, not}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.updateRegisteredDetails.ChangeRegisteredDetailsPage
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient
import testSupport.SDILBackendTestData.aSubscription

class ChangeRegisteredDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ChangeRegisteredDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe 3

            ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkboxItem, index) =>
      s"when the userAnswers contains data for the page with " + checkboxItem.toString + " selected" - {
        s"should return OK and render the page with " + checkboxItem.toString + " checkboxItem checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, Seq(checkboxItem)).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
              val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
              checkBoxInputs.size() mustBe 3

              ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
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

        val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, ChangeRegisteredDetails.values).success.value


        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe 3

            ChangeRegisteredDetails.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe true
            }
          }
        }
      }
    }
    "when the user activity is voluntary true" - {
      "should return OK and render only two checkboxes" in {
        given
          .commonPreconditionChangeSubscription(aSubscription.copy(activity = RetrievedActivity(
            smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = true)))

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe 2

            ChangeRegisteredDetails.voluntaryValues.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") should not equal "sites"
            }
          }
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)
  }

  s"POST " + normalRoutePath - {
    ChangeRegisteredDetails.values.foreach { checkboxItem =>
      "when the user selects " + checkboxItem.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303

                checkboxItem match {
                  case ChangeRegisteredDetails.Sites => res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                  case ChangeRegisteredDetails.ContactDetails => res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad().url)
                  case ChangeRegisteredDetails.BusinessAddress => res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessAddressController.onPageLoad().url)
                }

                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Seq[ChangeRegisteredDetails]]](None)(_.get(ChangeRegisteredDetailsPage))

                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, Seq(checkboxItem)).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303

                checkboxItem match {
                  case ChangeRegisteredDetails.Sites => res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                  case ChangeRegisteredDetails.ContactDetails => res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad().url)
                  case ChangeRegisteredDetails.BusinessAddress => res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessAddressController.onPageLoad().url)
                }
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Seq[ChangeRegisteredDetails]]](None)(_.get(ChangeRegisteredDetailsPage))

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

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> ChangeRegisteredDetails.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303

              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Seq[ChangeRegisteredDetails]]](None)(_.get(ChangeRegisteredDetailsPage))

              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe ChangeRegisteredDetails.values
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage, ChangeRegisteredDetails.values).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> ChangeRegisteredDetails.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303

              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Seq[ChangeRegisteredDetails]]](None)(_.get(ChangeRegisteredDetailsPage))

              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe ChangeRegisteredDetails.values
            }
          }
        }
      }
    }

    "when the user does not select any checkbox" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select at least one option to continue"
          }
        }
      }
    }

    "when the user activity is voluntary true" - {
      "when value 'sites' is attempted to be saved, should return 400 with required error" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(ChangeRegisteredDetailsPage,
          ChangeRegisteredDetails.voluntaryValues).success.value

        setAnswers(userAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "sites")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: What do you need to update? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select at least one option to continue"

            val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Seq[ChangeRegisteredDetails]]](None)(_.get(ChangeRegisteredDetailsPage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe ChangeRegisteredDetails.voluntaryValues
          }
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      normalRoutePath, Some(Json.obj("value" -> "true")))

  }

}
