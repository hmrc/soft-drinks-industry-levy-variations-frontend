package controllers.changeActivity

import controllers.ControllerITTestHelper
import generators.ChangeActivityCYAGenerators._
import models.CheckMode
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, None => NoneProduced, Small}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ChangeActivityCYAControllerISpec extends ControllerITTestHelper {

  val route = "/change-activity/check-your-answers"

  "GET " + routes.ChangeActivityCYAController.onPageLoad.url - {
    "when the userAnswers contains no data" - {
      "should render the page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 4
          }
        }
      }
    }

    def testAmountProducedSection(page: Document, amountProducedValue: Option[AmountProduced]): Unit = {
      page.getElementsByTag("h2").first().text() mustBe "Packaged globally"
      val amountProduced = page.getElementsByClass("govuk-summary-list").first().getElementsByClass("govuk-summary-list__row")

      if (amountProducedValue.nonEmpty) {
        val answerToMatch = amountProducedValue match {
          case Some(Large) => "1 million litres or more"
          case Some(Small) => "Less than 1 million litres"
          case Some(NoneProduced) => "None"
        }
        amountProduced.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe answerToMatch
        amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change how many litres of your own brands have been packaged globally"
        amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "how many litres of your own brands have been packaged globally"
        amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.AmountProducedController.onPageLoad(CheckMode).url
      }
    }
    def testThirdPartyPackagingSection(page: Document, thirdPartyPackagingValue: Option[Boolean]): Unit = {
      if (thirdPartyPackagingValue.nonEmpty) {
        page.getElementsByTag("h2").get(1).text() mustBe "Third party packagers"
        val thirdPartyPackaging = page.getElementsByClass("govuk-summary-list").get(1).getElementsByClass("govuk-summary-list__row")

        val answerToMatch = thirdPartyPackagingValue match {
          case Some(true) => "Yes"
          case Some(false) => "No"
        }
        thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe answerToMatch
        thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you use third party packagers"
        thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you use third party packagers"
        thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ThirdPartyPackagersController.onPageLoad(CheckMode).url
      }
    }
    def testOwnBrandsSection(page: Document, ownBrandsValue: Option[Boolean], decrementSectionIndex: Boolean): Unit = {
      val sectionIndex = if (decrementSectionIndex) 1 else 2
      page.getElementsByTag("h2").get(sectionIndex).text() mustBe "Packaged in the UK"
      val ownBrands = page.getElementsByClass("govuk-summary-list").get(sectionIndex).getElementsByClass("govuk-summary-list__row")

      if (ownBrandsValue.nonEmpty) {
        ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you package your own brands at your own site?"
        ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you package your own brands at your own site?"
        ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

        ownBrandsValue match {
          case Some(true) =>
            ownBrands.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            ownBrands.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(ownBrandsLitresLowBand)
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band packaged at your own site"
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band packaged at your own site"
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

            ownBrands.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(ownBrandsLitresHighBand)
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band packaged at your own site"
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band packaged at your own site"
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
          case Some(false) => ownBrands.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
        }
      }
    }
    def testContractSection(page: Document, contractValue: Option[Boolean], decrementSectionIndex: Boolean): Unit = {
      val sectionIndex = if (decrementSectionIndex) 2 else 3
      page.getElementsByTag("h2").get(sectionIndex).text() mustBe "Package for customers"
      val contractPacking = page.getElementsByClass("govuk-summary-list").get(sectionIndex).getElementsByClass("govuk-summary-list__row")

      if (contractValue.nonEmpty) {
        contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you use contract packing at your own site?"
        contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you use contract packing at your own site?"
        contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ContractPackingController.onPageLoad(CheckMode).url

        contractValue match {
          case Some(true) =>
            contractPacking.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            contractPacking.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(contractLitresLowBand)
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url

            contractPacking.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(contractLitresHighBand)
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url
          case Some(false) => contractPacking.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
        }
      }

    }
    def testImportSection(page: Document, importValue: Option[Boolean], decrementSectionIndex: Boolean): Unit = {
      val sectionIndex = if (decrementSectionIndex) 3 else 4
      page.getElementsByTag("h2").get(sectionIndex).text() mustBe "Brought into the UK"
      val imports = page.getElementsByClass("govuk-summary-list").get(sectionIndex).getElementsByClass("govuk-summary-list__row")

      if (importValue.nonEmpty) {
        imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you bring liable drinks into the UK?"
        imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you bring liable drinks into the UK?"
        imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ImportsController.onPageLoad(CheckMode).url

        importValue match {
          case Some(true) =>
            imports.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            imports.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(importLitresLowBand)
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url

            imports.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(importLitresHighBand)
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url
          case Some(false) => imports.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
        }
      }

    }

    amountProducedValues.foreach { case (amountProducedKey, amountProducedValue) =>
      thirdPartyPackagingValues.foreach { case (thirdPartyPackagingKey, thirdPartyPackagingValue) =>
        ownBrandsValues.foreach { case (ownBrandsKey, ownBrandsValue) =>
          contractValues.foreach { case (contractKey, contractValue) =>
            importValues.foreach { case (importKey, importValue) =>
              val key = List(amountProducedKey, thirdPartyPackagingKey, ownBrandsKey, contractKey, importKey).filterNot(_.isEmpty).mkString(", ")
              s"when the userAnswers contains $key" - {
                "should render the page" in {
                  given
                    .commonPrecondition

                  val userAnswers = getUserAnswers(amountProducedValue, thirdPartyPackagingValue, ownBrandsValue, contractValue, importValue)
                  setAnswers(userAnswers)

                  WsTestClient.withClient { client =>
                    val result = createClientRequestGet(client, baseUrl + route)

                    whenReady(result) { res =>
                      res.status mustBe OK
                      val page = Jsoup.parse(res.body)
                      page.title must include(Messages("changeActivity.checkYourAnswers.title"))
                      //      TODO: Implement Return Period in DLS-8346
                      page.getElementsByClass("govuk-caption-l").text() mustBe "Super Lemonade Plc - RETURN PERIOD"
                      page.getElementsByClass("govuk-summary-list").size() mustBe (if (thirdPartyPackagingValue.isDefined) 5 else 4)
                      testAmountProducedSection(page, amountProducedValue)
                      testThirdPartyPackagingSection(page, thirdPartyPackagingValue)
                      testOwnBrandsSection(page, ownBrandsValue, decrementSectionIndex = thirdPartyPackagingValue.isEmpty)
                      testContractSection(page, contractValue, decrementSectionIndex = thirdPartyPackagingValue.isEmpty)
                      testImportSection(page, importValue, decrementSectionIndex = thirdPartyPackagingValue.isEmpty)
                      page.getElementsByTag("form").first().attr("action") mustBe routes.ChangeActivityCYAController.onSubmit.url
                      page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm updates and send"
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }
  "POST " + routes.ChangeActivityCYAController.onSubmit.url - {
    "when the userAnswers contains no data" - {
      "should redirect to next page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

          whenReady(result) { res =>
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, Some(Json.obj()))
  }
}