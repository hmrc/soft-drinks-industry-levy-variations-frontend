package controllers.changeActivity

import controllers.ControllerITTestHelper
import controllers.changeActivity.routes.SecondaryWarehouseDetailsController
import generators.ChangeActivityCYAGenerators._
import models.backend.Site
import models.changeActivity.AmountProduced
import models.changeActivity.AmountProduced.{Large, Small, None => NoneProduced}
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ChangeActivityCYAControllerISpec extends ControllerITTestHelper with WsTestClient {

  val route = "/change-activity/check-your-answers"

  "GET " + routes.ChangeActivityCYAController.onPageLoad.url - {
    "when the userAnswers contains no data" - {
      "should redirect the page as there are missing user answers" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe SEE_OTHER
          }
        }
      }
    }

    def testAmountProducedSection(page: Document, amountProducedValue: Option[AmountProduced], sectionIndex: Option[Int]): Unit = {
      amountProducedValue map { amountProducedVal =>
        sectionIndex map { sectionInd =>
          page.getElementsByTag("h2").get(sectionInd).text() mustBe "Packaged globally"
          val amountProduced = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")

          val answerToMatch = amountProducedVal match {
            case Large => "1 million litres or more"
            case Small => "Less than 1 million litres"
            case NoneProduced => "None"
          }
          amountProduced.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe answerToMatch
          amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change how many litres of your own brands have been packaged globally"
          amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "how many litres of your own brands have been packaged globally"
          amountProduced.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.AmountProducedController.onPageLoad(NormalMode).url
        }
      }
    }

    def testThirdPartyPackagingSection(page: Document, thirdPartyPackagingValue: Option[Boolean], sectionIndex: Option[Int]): Unit = {
      thirdPartyPackagingValue map { thirdPartyPackagingVal =>
        sectionIndex map { sectionInd =>
          page.getElementsByTag("h2").get(sectionInd).text() mustBe "Third party packagers"
          val thirdPartyPackaging = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")

          thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe (if (thirdPartyPackagingVal) "Yes" else "No")
          thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you use third party packagers"
          thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you use third party packagers"
          thirdPartyPackaging.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ThirdPartyPackagersController.onPageLoad(CheckMode).url
        }
      }
    }

    def testOwnBrandsSection(page: Document, ownBrandsValue: Option[Boolean], sectionIndex: Option[Int]): Unit = {
      ownBrandsValue map { ownBrandsVal =>
        sectionIndex map { sectionInd =>
          page.getElementsByTag("h2").get(sectionInd).text() mustBe "Packaged in the UK"
          val ownBrands = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")

          ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you package your own brands at your own site"
          ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you package your own brands at your own site"
          ownBrands.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.OperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

          if (ownBrandsVal) {
            ownBrands.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            ownBrands.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(ownBrandsLitresLowBand)
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band packaged at your own site"
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band packaged at your own site"
            ownBrands.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url

            ownBrands.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(ownBrandsLitresHighBand)
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band packaged at your own site"
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band packaged at your own site"
            ownBrands.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSiteOwnBrandsController.onPageLoad(CheckMode).url
          } else {
            ownBrands.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
          }
        }
      }
    }

    def testContractSection(page: Document, contractValue: Option[Boolean], sectionIndex: Option[Int]): Unit = {
      contractValue map { contractVal =>
        sectionIndex map { sectionInd =>
          page.getElementsByTag("h2").get(sectionInd).text() mustBe "Package for customers"
          val contractPacking = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")

          contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you use contract packing at your own site"
          contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you use contract packing at your own site"
          contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ContractPackingController.onPageLoad(CheckMode).url

          if (contractVal) {
            contractPacking.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            contractPacking.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(contractLitresLowBand)
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url

            contractPacking.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(contractLitresHighBand)
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url
          } else {
            contractPacking.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
          }
        }
      }
    }

    def testImportSection(page: Document, importValue: Option[Boolean], sectionIndex: Option[Int]): Unit = {
      importValue map { importVal =>
        sectionIndex map { sectionInd =>
          page.getElementsByTag("h2").get(sectionInd).text() mustBe "Brought into the UK"
          val imports = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")

          imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change whether you bring liable drinks into the UK"
          imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "whether you bring liable drinks into the UK"
          imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ImportsController.onPageLoad(CheckMode).url

          if (importVal) {
            imports.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"

            imports.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(importLitresLowBand)
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in low band brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in low band brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url

            imports.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(importLitresHighBand)
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change number of litres in high band brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "number of litres in high band brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url
          } else {
            imports.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
          }
        }
      }
    }

    def testSiteSection(page: Document, packingSites: Option[Site], warehouseSites: Option[Site], sectionIndex: Option[Int]): Unit = {

      (packingSites,warehouseSites) match {
        case (Some(packingSites), Some(warehouseSites)) => sectionIndex map { sectionInd =>
          val sites = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")
          sites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK warehouse you use to store liable drinks"
          sites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK warehouse you use to store liable drinks"
          sites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK packaging site that you operate to produce liable drinks"
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK packaging site that you operate to produce liable drinks"
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
        }
        case (Some(packingSites), None) =>
          sectionIndex map { sectionInd =>

          val sites = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK packaging site that you operate to produce liable drinks"
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK packaging site that you operate to produce liable drinks"
          sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
          }
        case (None, Some(warehouseSites)) =>
          sectionIndex map { sectionInd =>

            val sites = page.getElementsByClass("govuk-summary-list").get(sectionInd).getElementsByClass("govuk-summary-list__row")
            sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK warehouse you use to store liable drinks"
            sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK warehouse you use to store liable drinks"
            sites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url
          }
        case (None, None) => None
      }

    }

    testCaseOptions.foreach { case userAnswerOptions =>

      val key = getKeyStringFromUserAnswerOptions(userAnswerOptions)
      val userAnswers = getUserAnswersFromUserAnswerOptions(userAnswerOptions)
      val amountProducedValue = userAnswerOptions.amountProducedTuple._2
      val thirdPartyPackagingValue = userAnswerOptions.thirdPartyPackagingTuple._2
      val ownBrandsValue = userAnswerOptions.ownBrandsTuple._2
      val contractValue = userAnswerOptions.contractTuple._2
      val importValue = userAnswerOptions.importTuple._2
      val warehouseValue = userAnswerOptions.warehouseSite._2
      val packingSiteValue = userAnswerOptions.packingSite._2

      s"when the userAnswers contains $key" - {
        "should render the page" in {
          given
            .commonPrecondition
          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.checkYourAnswers.title"))
              page.getElementsByClass("govuk-caption-l").text() mustBe "Super Lemonade Plc"
              val sectionIndexes: Seq[Option[Int]] = List(
                amountProducedValue.nonEmpty,
                thirdPartyPackagingValue.nonEmpty,
                ownBrandsValue.nonEmpty,
                contractValue.nonEmpty,
                importValue.nonEmpty,
                packingSitesValues.nonEmpty || warehouseValues.nonEmpty
              ).foldLeft(Seq[Option[Int]]()) { (indexes, sectionDefined) =>
                indexes :+ (if (sectionDefined) Option(indexes.filter(_.nonEmpty).flatten.size) else None)
              }
              page.getElementsByClass("govuk-summary-list").size() mustBe sectionIndexes.filter(_.nonEmpty).flatten.size
              testAmountProducedSection(page, amountProducedValue, sectionIndex = sectionIndexes(0))
              testThirdPartyPackagingSection(page, thirdPartyPackagingValue, sectionIndex = sectionIndexes(1))
              testOwnBrandsSection(page, ownBrandsValue, sectionIndex = sectionIndexes(2))
              testContractSection(page, contractValue, sectionIndex = sectionIndexes(3))
              testImportSection(page, importValue, sectionIndex = sectionIndexes(4))
              testSiteSection(page, packingSiteValue, warehouseValue, sectionIndex = sectionIndexes(5))
              page.getElementsByTag("form").first().attr("action") mustBe routes.ChangeActivityCYAController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm updates and send"
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

        withClient { client =>
          val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

          whenReady(result) { res =>
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad.url)
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, Some(Json.obj()))
  }
}