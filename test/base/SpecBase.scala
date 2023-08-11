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
import controllers.actions._
import controllers.routes
import errors.VariationsErrors
import helpers.LoggerHelper
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import play.api.http.Status.SEE_OTHER
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Play}
import queries.Settable
import service.VariationResult
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}
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

  lazy val application = applicationBuilder(userAnswers = None).build()
  implicit lazy val messagesAPI = application.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = application.injector.instanceOf[MessagesControllerComponents]
  implicit val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
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


  def userDetailsWithSetMethodsReturningFailure(selectChange: SelectChange): UserAnswers = new UserAnswers("sdilId", selectChange, contactAddress = contactAddress, correctReturnPeriod = if(selectChange == SelectChange.CorrectReturn) {returnPeriod.headOption} else {None}) {
    override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
    override def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)(implicit writes: Writes[Boolean]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
  }

  def defaultCall: Call = routes.IndexController.onPageLoad
  def recoveryCall: Call = routes.JourneyRecoveryController.onPageLoad()

  def selectChangeCall: Call = routes.SelectChangeController.onPageLoad
}
