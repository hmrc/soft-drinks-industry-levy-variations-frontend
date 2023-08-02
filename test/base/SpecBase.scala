/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package base

import cats.data.EitherT
import config.FrontendAppConfig
import controllers.actions._
import controllers.routes
import errors.VariationsErrors
import helpers.LoggerHelper
import models.backend.{Site, UkAddress}
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import play.api.http.Status.SEE_OTHER
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Play}
import queries.Settable
import service.VariationResult
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object SpecBase {
  val aSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

}

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with LoggerHelper {

  val userAnswersId: String = "id"
  val sdilNumber: String = "XKSDIL000000022"

  lazy val application = applicationBuilder(userAnswers = None).build()
  implicit lazy val messagesAPI = application.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = application.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

  def createSuccessVariationResult[T](result: T): VariationResult[T] =
    EitherT.right[VariationsErrors](Future.successful(result))

  def createFailureVariationResult[T](error: VariationsErrors): VariationResult[T] =
    EitherT.left(Future.successful(error))

  override def afterEach(): Unit = {
    Play.stop(application)
    super.afterEach()
  }

   val returnPeriodList = List(ReturnPeriod(2020, 0), ReturnPeriod(2020, 1), ReturnPeriod(2020, 2), ReturnPeriod(2020, 3),
    ReturnPeriod(2022, 0), ReturnPeriod(2022, 1), ReturnPeriod(2022, 2), ReturnPeriod(2022, 3))

  val JsonreturnPeriodList = List(Json.toJson(ReturnPeriod(2020, 0)), Json.toJson(ReturnPeriod(2020, 1)),
                                  Json.toJson(ReturnPeriod(2020, 2)), Json.toJson(ReturnPeriod(2020, 3)),
                                  Json.toJson(ReturnPeriod(2022, 0)), Json.toJson(ReturnPeriod(2022, 1)),
                                  Json.toJson(ReturnPeriod(2022, 2)), Json.toJson(ReturnPeriod(2022, 3)))

  lazy val warehouse = Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy"),"WR53 7CX"))
  lazy val packingSite = Site(
      UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
      Some("88"),
      Some("Wild Lemonade Group"),
      Some(LocalDate.of(2018, 2, 26)))

  lazy val packingSiteMap = Map("000001" -> packingSite)

  lazy val contactAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

  val emptyUserAnswersForUpdateRegisteredDetails: UserAnswers = UserAnswers(userAnswersId, SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress)
  val warehouseAddedToUserAnswersForUpdateRegisteredDetails: UserAnswers = UserAnswers(userAnswersId, SelectChange.UpdateRegisteredDetails, warehouseList = Map("1" -> warehouse), contactAddress = contactAddress)
  val emptyUserAnswersForChangeActivity = UserAnswers(sdilNumber, SelectChange.ChangeActivity, contactAddress = contactAddress)
  val warehouseAddedToUserAnswersForChangeActivity: UserAnswers = UserAnswers(userAnswersId, SelectChange.ChangeActivity, warehouseList = Map("1" -> warehouse), contactAddress = contactAddress)

  val emptyUserAnswersForCorrectReturn = UserAnswers(sdilNumber, SelectChange.CorrectReturn, contactAddress = contactAddress)

  val emptyUserAnswersForCancelRegistration = UserAnswers(sdilNumber, SelectChange.CancelRegistration, contactAddress = contactAddress)

  def emptyUserAnswersForSelectChange(selectChange: SelectChange) = UserAnswers(sdilNumber, selectChange, contactAddress = contactAddress)
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None,
                                    subscription: Option[RetrievedSubscription] = None): GuiceApplicationBuilder = {
    val bodyParsers = stubControllerComponents().parsers.defaultBodyParser
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(subscription, bodyParsers)),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
  }

  def testInvalidJourneyType(expectedJourneyType: SelectChange, url: String, hasPostMethod: Boolean = true) = {
    val journeyTypesNotSupported = expectedJourneyType match {
      case SelectChange.CancelRegistration => SelectChange.values.filter(!List(expectedJourneyType, SelectChange.ChangeActivity).contains(_))
      case _ => SelectChange.values.filter(_ != expectedJourneyType)
    }
    journeyTypesNotSupported.foreach { selectChange =>
      s"must redirect to select change for a GET if userAnswers select change is for $selectChange" in {

        val application = applicationBuilder(Some(emptyUserAnswersForSelectChange(selectChange))).build()

        running(application) {
          val request = FakeRequest(GET, url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual selectChangeCall.url
        }
      }

      if(hasPostMethod) {
        s"must redirect to select change for a POST if userAnswers select change is for $selectChange" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, url)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual selectChangeCall.url
          }
        }
      }
    }
  }

  def testNoUserAnswersError(url: String, hasPostMethod: Boolean = true) = {
    "must redirect to Select change for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual selectChangeCall.url
      }
    }

    if(hasPostMethod) {
      "must redirect to Select change for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, url)
              .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual selectChangeCall.url
        }
      }
    }
  }

  val aSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val producerName = "Super Cola Plc"
  val sdilReference = "XCSDIL000000069"
  val producerNameParty = "Soft Juice"
  val sdilReferenceParty = "XMSDIL000000113"
  val bandMax: Long = 100000000000000L
  val litres: Long = bandMax - 1
  val smallProducerList: List[SmallProducer] = List(SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)))

  val returnPeriods = List(ReturnPeriod(2018, 1), ReturnPeriod(2019, 1))
  val returnPeriod = List(ReturnPeriod(2018, 1))
  val financialItem1 = ReturnCharge(returnPeriods.head, BigDecimal(-100))
  val financialItem2 = ReturnCharge(returnPeriods.head, BigDecimal(-200))
  val financialItemList = List(financialItem1, financialItem2)

  def userDetailsWithSetMethodsReturningFailure(selectChange: SelectChange): UserAnswers = new UserAnswers("sdilId", selectChange, contactAddress = contactAddress) {
    override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
    override def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)(implicit writes: Writes[Boolean]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
  }

  def defaultCall: Call = routes.IndexController.onPageLoad
  def recoveryCall: Call = routes.JourneyRecoveryController.onPageLoad()

  def selectChangeCall: Call = routes.SelectChangeController.onPageLoad
}
