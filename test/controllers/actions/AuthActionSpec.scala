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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.{ any, eq => eqTo }
import org.mockito.Mockito.{ never, verify, when }
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{ BodyParsers, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SdilSubscriptionService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc.{ Action, AnyContent }

class AuthActionSpec extends SpecBase with MockitoSugar {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
    def subscriptionRef(): Action[AnyContent] = authAction { implicit request: IdentifierRequest[AnyContent] =>
      Results.Ok(request.subscription.sdilRef)
    }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
          val sdilService = application.injector.instanceOf[SdilSubscriptionService]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has a UTR and a resolved SDIL enrolment" - {

      "must retrieve the subscription by SDIL ref so UTR does not mask active SDIL selection" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val sdilConnector = mock[SoftDrinksIndustryLevyConnector]
          val sdilService = mock[SdilSubscriptionService]
          val utr = aSubscription.utr
          val enrolments = Enrolments(
            Set(
              Enrolment(
                "IR-CT",
                Seq(EnrolmentIdentifier("UTR", utr)),
                "Activated"
              ),
              Enrolment(
                "HMRC-OBTDS-ORG",
                Seq(EnrolmentIdentifier("EtmpRegistrationNumber", sdilNumber)),
                "Activated"
              )
            )
          )
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          when(sdilService.resolveActiveSdilRef(any[Seq[String]])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(sdilNumber)))
          when(sdilConnector.retrieveSubscription(eqTo(sdilNumber), eqTo("sdil"))(using any[HeaderCarrier]))
            .thenReturn(createSuccessVariationResult(Some(aSubscription)))

          val authAction = new AuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(enrolments),
            appConfig,
            bodyParsers,
            sdilConnector,
            sdilService,
            errorHandler
          )
          val controller = new Harness(authAction)
          val result = controller.subscriptionRef()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustBe sdilNumber
          verify(sdilConnector).retrieveSubscription(eqTo(sdilNumber), eqTo("sdil"))(using any[HeaderCarrier])
          verify(sdilConnector, never()).retrieveSubscription(eqTo(utr), eqTo("utr"))(using any[HeaderCarrier])
        }
      }
    }
  }
}

class FakeSuccessfulAuthConnector @Inject() (enrolments: Enrolments) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.successful(enrolments.asInstanceOf[A])
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
