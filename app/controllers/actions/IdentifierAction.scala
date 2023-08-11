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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default,
                                               sdilConnector: SoftDrinksIndustryLevyConnector,
                                               errorHandler: ErrorHandler)
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with ActionHelpers {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway)).retrieve(allEnrolments) { enrolments =>
      val maybeUtr = getUtr(enrolments)
      val maybeSdil = getSdilEnrolment(enrolments)
      (maybeSdil, maybeUtr) match {
        case (None, None) =>
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
        case (Some(sdil), _) =>
          getSubscriptionAndGenerateIdentifierRequest(sdil.value, "sdil", request, block)
        case (None, Some(utr)) => getSubscriptionAndGenerateIdentifierRequest(utr, "utr", request, block)
      }
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def getSubscriptionAndGenerateIdentifierRequest[A](identifierValue: String,
                                                             identifierType: String,
                                                             request: Request[A],
                                                             block: IdentifierRequest[A] => Future[Result])
                                                            (implicit hc: HeaderCarrier): Future[Result] = {
    sdilConnector.retrieveSubscription(identifierValue, identifierType).value.flatMap {
      case Right(Some(sub)) => block(IdentifierRequest(request, EnrolmentIdentifier("sdil", sub.sdilRef).value, sub))
      case Right(None) =>
        Future.successful(Redirect(config.sdilHomeUrl))
      case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
    }
  }
}
