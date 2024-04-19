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
import cats.implicits._
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import controllers.routes
import errors.VariationsErrors
import helpers.LoggerHelper
import models.SelectChange._
import models._
import models.backend.{RetrievedActivity, RetrievedSubscription, Site, UkAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
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
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}


object SpecBase {
  lazy val contactAddress: UkAddress = UkAddress(List("19 Rhes Priordy", "East London"), "E73 2RP")

  val sdilNumber: String = "XKSDIL000000022"
  val litreage = (10L, 20L)
  val id = "id"
  val superColaProducerAlias = "Super Cola Ltd"
  val superJuiceProducerAlias = "Super Juice Ltd"
  val referenceNumber1 = "XZSDIL000000234"
  val referenceNumber2 = "XZSDIL000000235"
  val utr = "0000000022"

  val oneProductionSite: Map[String,Site] = Map(
   "1" -> Site(
      UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
     Some("Wild Lemonade Group"),
     Some("88"),
      Some(LocalDate.of(2018, 2, 26)))
  )

  val oneWarehouses: Map[String,Site] = Map(
    "1"-> Site(UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX"), Some("ABC Ltd"))
  )

  val twoWarehouses: Map[String,Site] = Map(
    "1"-> Site(UkAddress(List("33 Rhes Priordy", "East London","Line 3","Line 4"),"WR53 7CX"), Some("ABC Ltd")),
    "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London","Line 3",""),"SA13 7CE"), Some("Super Cola Ltd"))
  )


  val userAnswerTwoWarehouses : UserAnswers = UserAnswers(sdilNumber,SelectChange.CorrectReturn, contactAddress = contactAddress, data = Json.obj(), warehouseList = twoWarehouses)
  val userAnswerTwoWarehousesUpdateRegisteredDetails : UserAnswers = UserAnswers(sdilNumber,SelectChange.UpdateRegisteredDetails, contactAddress = contactAddress, data = Json.obj(), warehouseList = twoWarehouses)


  val aSubscription: RetrievedSubscription = RetrievedSubscription(
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

  val voluntarySubscription: RetrievedSubscription = aSubscription.copy(activity = RetrievedActivity(
    smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = true))

}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with LoggerHelper
    with TestData {

  lazy val application: Application = applicationBuilder(userAnswers = None).build()
  implicit lazy val messagesAPI: MessagesApi = application.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  implicit val frontendAppConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
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

  def testInvalidJourneyType(expectedJourneyType: SelectChange, url: String, hasPostMethod: Boolean = true): Unit = {
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

  def testRedirectToPostSubmissionIfRequired(selectChange: SelectChange, url: String): Unit = {
    val userAnswers = selectChange match {
      case CorrectReturn => userAnswersForCorrectReturnWithEmptySdilReturn.copy(submitted = true)
      case _ => UserAnswers(sdilNumber, journeyType = selectChange, contactAddress = contactAddress, submitted = true)
    }
    val expectedLocation = selectChange match {
      case CancelRegistration => controllers.cancelRegistration.routes.CancellationRequestDoneController.onPageLoad()
      case UpdateRegisteredDetails => controllers.updateRegisteredDetails.routes.UpdateDoneController.onPageLoad()
      case CorrectReturn => controllers.correctReturn.routes.CorrectReturnUpdateDoneController.onPageLoad
      case ChangeActivity => controllers.changeActivity.routes.ChangeActivitySentController.onPageLoad
    }
    val application = selectChange match {
      case CorrectReturn =>
        val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
        when(mockSdilConnector.getReturn(any(), any())(any())).thenReturn(createSuccessVariationResult(Some(emptySdilReturn)))
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()
      case _ => applicationBuilder(userAnswers = Some(userAnswers)).build()
    }
    s"must redirect to post submission for a GET if user answers submitted for $selectChange" in {
      running(application) {
        val request = FakeRequest(GET, url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedLocation.url
      }
    }
  }

  def testNoUserAnswersError(url: String, hasPostMethod: Boolean = true): Unit = {
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


  def userDetailsWithSetMethodsReturningFailure(selectChange: SelectChange): UserAnswers =
    new UserAnswers("sdilId", selectChange, contactAddress = contactAddress, correctReturnPeriod =
      if(selectChange == SelectChange.CorrectReturn) {returnPeriod.headOption} else None) {
    override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
    override def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)(implicit writes: Writes[Boolean]):
    Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
  }

  def defaultCall: Call = routes.SelectChangeController.onPageLoad
  def recoveryCall: Call = routes.JourneyRecoveryController.onPageLoad()

  def selectChangeCall: Call = routes.SelectChangeController.onPageLoad
}
