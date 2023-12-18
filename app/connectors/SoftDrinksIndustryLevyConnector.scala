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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import errors.UnexpectedResponseFromSDIL
import models.backend._
import models.correctReturn.ReturnsVariation
import models.submission.{ReturnVariationData, VariationsSubmission}
import models.{ReturnPeriod, SdilReturn}
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import repositories.{SDILSessionCache, SDILSessionKeys}
import service.VariationResult
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
                                                 val http: HttpClient,
                                                 frontendAppConfig: FrontendAppConfig,
                                                 sdilSessionCache: SDILSessionCache,
                                                 genericLogger: GenericLogger
                                               )(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(identifierValue: String, identifierType: String)
                          (implicit hc: HeaderCarrier): VariationResult[Option[RetrievedSubscription]] = EitherT {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(Right(optSubscription.optRetrievedSubscription))
      case None =>
        http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(identifierValue: String, identifierType)).flatMap {
          optRetrievedSubscription =>
            sdilSessionCache.save(identifierValue, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map { _ =>
                Right(optRetrievedSubscription)
              }

        }.recover {
          case _ =>
            genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][retrieveSubscription] - unexpected response for $identifierValue")
            Left(UnexpectedResponseFromSDIL)
        }
    }
  }

  private def smallProducerUrl(sdilRef:String, period:ReturnPeriod):String =
    s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): VariationResult[Option[Boolean]] = EitherT {
    sdilSessionCache.fetchEntry[OptSmallProducer](sdilRef, SDILSessionKeys.smallProducerForPeriod(period)).flatMap {
      case Some(optSP) =>
        Future.successful(Right(optSP.optSmallProducer))
      case None =>
        http.GET[Option[Boolean]](smallProducerUrl(sdilRef, period)).flatMap {
          optSP =>
            sdilSessionCache.save(sdilRef, SDILSessionKeys.smallProducerForPeriod(period), OptSmallProducer(optSP))
              .map { _ => Right(optSP)
              }
        }.recover {
          case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][checkSmallProducerStatus] - unexpected response for $sdilRef")
            Left(UnexpectedResponseFromSDIL)
        }
    }
  }

  def balance(
               sdilRef: String,
               withAssessment: Boolean
             )(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    sdilSessionCache.fetchEntry[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment)).flatMap {
      case Some(b) => Future.successful(b)
      case None =>
        http.GET[BigDecimal](s"$sdilUrl/balance/$sdilRef/$withAssessment")
          .flatMap { b =>
            sdilSessionCache.save[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment), b)
              .map(_ => b)
          }
    }
  }

  def balanceHistory(
                      sdilRef: String,
                      withAssessment: Boolean
                    )(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import FinancialLineItem.formatter
    sdilSessionCache.fetchEntry[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment)).flatMap {
      case Some(fli) => Future.successful(fli)
      case None =>
        http.GET[List[FinancialLineItem]](s"$sdilUrl/balance/$sdilRef/history/all/$withAssessment")
          .flatMap{ fli => sdilSessionCache.save[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment), fli)
            .map(_ => fli)
          }
    }
  }

  def getReturn(
                   utr: String,
                   period: ReturnPeriod
                 )(implicit hc: HeaderCarrier): VariationResult[Option[SdilReturn]] = EitherT {
    sdilSessionCache.fetchEntry[OptPreviousSubmittedReturn](utr, SDILSessionKeys.previousSubmittedReturn(utr, period)).flatMap {
      case Some(optPreviousReturn) => Future.successful(Right(optPreviousReturn.optReturn))
      case None =>
        val uri = s"$sdilUrl/returns/$utr/year/${period.year}/quarter/${period.quarter}"
        http.GET[Option[SdilReturn]](uri)
          .flatMap { optReturn =>
            sdilSessionCache.save[OptPreviousSubmittedReturn](utr,
              SDILSessionKeys.previousSubmittedReturn(utr, period), OptPreviousSubmittedReturn(optReturn))
              .map(_ => Right(optReturn))
          }.recover {
          case _ => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][returns_get] - unexpected response for $utr")
            Left(UnexpectedResponseFromSDIL)
        }
    }
  }

  def getVariableReturnsFromCache(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    sdilSessionCache.fetchEntry[List[ReturnPeriod]](utr, SDILSessionKeys.VARIABLE_RETURNS).flatMap {
      case Some(variableReturns) => Future.successful(Right(variableReturns))
      case None =>
        returnsVariable(utr).value
    }
  }

  def returnsVariable(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/variable").flatMap { variableReturns =>
        sdilSessionCache.save(utr, SDILSessionKeys.VARIABLE_RETURNS, variableReturns)
          .map { _ => Right(variableReturns)
        }
    }.recover {
      case _ => Left(UnexpectedResponseFromSDIL)
    }
  }

  def getPendingReturnsFromCache(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    sdilSessionCache.fetchEntry[List[ReturnPeriod]](utr, SDILSessionKeys.RETURNS_PENDING).flatMap {
      case Some(pendingReturns) => Future.successful(Right(pendingReturns))
      case None =>
        returnsPending(utr).value
    }
  }

  def returnsPending(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending").flatMap { variableReturns =>
      sdilSessionCache.save(utr, SDILSessionKeys.RETURNS_PENDING, variableReturns)
        .map { _ => Right(variableReturns)
        }
    }.recover {
      case _ => Left(UnexpectedResponseFromSDIL)
    }
  }

  def submitVariation(variation: VariationsSubmission, sdilNumber: String)(implicit hc: HeaderCarrier): VariationResult[Unit] = EitherT {
    genericLogger.logger.info(s"[SoftDrinksIndustryLevyConnector][submitVariation] - variation data we are submitting: ${Json.toJson(variation)}")
    http.POST[VariationsSubmission, HttpResponse](s"$sdilUrl/submit-variations/sdil/$sdilNumber", variation)
      .map { resp =>
        resp.status match {
          case NO_CONTENT => Right((): Unit)
          case status =>
            genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][submitVariation] - unexpected response $status for $sdilNumber")
            Left(UnexpectedResponseFromSDIL)
        }
      }.recover {
      case _ =>
        genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][submitVariation] - unexpected response for $sdilNumber")
        Left(UnexpectedResponseFromSDIL)
    }
  }

  def submitSdilReturnsVary(sdilNumber: String, variation: ReturnVariationData)(implicit hc: HeaderCarrier): VariationResult[Unit] = EitherT {
    genericLogger.logger.info(s"[SoftDrinksIndustryLevyConnector][submitVariation] - variation data we are submitting: ${Json.toJson(variation)}")
    http.POST[ReturnVariationData, HttpResponse](s"$sdilUrl/returns/vary/$sdilNumber", variation).map { resp =>
      resp.status match {
        case NO_CONTENT => Right((): Unit)
        case status =>
          genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][submitReturnsVariation] - unexpected response $status for $sdilNumber")
          Left(UnexpectedResponseFromSDIL)
      }
    }.recover {
      case _ =>
        genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][submitReturnsVariation] - unexpected response for $sdilNumber")
        Left(UnexpectedResponseFromSDIL)
    }
  }

  def submitReturnVariation(sdilRef: String, variation: ReturnsVariation)(implicit hc: HeaderCarrier): VariationResult[Unit] = EitherT {
    http.POST[ReturnsVariation, HttpResponse](s"$sdilUrl/returns/variation/sdil/$sdilRef", variation).map { resp =>
      resp.status match {
        case NO_CONTENT => Right((): Unit)
        case status =>
          genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][returns_variation] - unexpected response $status for $sdilRef")
          Left(UnexpectedResponseFromSDIL)
      }
    }.recover {
      case _ =>
        genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][returns_variation] - unexpected response for $sdilRef")
        Left(UnexpectedResponseFromSDIL)
    }
  }

}