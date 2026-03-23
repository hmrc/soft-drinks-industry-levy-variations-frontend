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
import play.api.libs.json.{JsValue, Json}
import repositories.{SDILSessionCache, SDILSessionKeys}
import service.VariationResult
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class SoftDrinksIndustryLevyConnector @Inject() (
  val http: HttpClientV2,
  frontendAppConfig: FrontendAppConfig,
  sdilSessionCache: SDILSessionCache,
  genericLogger: GenericLogger
)(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private val logger = genericLogger.logger

  private class RawHttpReads extends HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  private val rawHttpReads = new RawHttpReads

  private def outboundHeaderCarrier(hc: HeaderCarrier): HeaderCarrier =
    HeaderCarrier(
      requestId = hc.requestId,
      sessionId = hc.sessionId
    )

  private def sdilContext(
    path: String,
    status: Option[Int] = None,
    startTime: Option[Long] = None
  ): String =
    Seq(
      Some(s"path=$path"),
      status.map(st => s"status=$st"),
      startTime.map(st => s"durationMs=${System.currentTimeMillis() - st}")
    ).flatten.mkString(" ")

  private def executeGet[A](operation: String, path: String)(implicit hc: HeaderCarrier, rds: HttpReads[A]): Future[A] = {
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .get(url"$urlString")(using outboundHeaderCarrier(hc))
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("GET", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }
  }

  private def executePost[A](operation: String, path: String, body: JsValue)(implicit
    hc: HeaderCarrier,
    rds: HttpReads[A]
  ): Future[A] = {
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .post(url"$urlString")(using outboundHeaderCarrier(hc))
      .withBody(body)
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("POST", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }
  }

  def retrieveSubscription(identifierValue: String, identifierType: String)(implicit
    hc: HeaderCarrier
  ): VariationResult[Option[RetrievedSubscription]] = EitherT {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](identifierValue, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(Right(optSubscription.optRetrievedSubscription))
      case None =>
        executeGet[Option[RetrievedSubscription]](
          operation = "retrieveSubscription",
          path = s"/subscription/$identifierType/$identifierValue"
        )
          .flatMap { optRetrievedSubscription =>
            sdilSessionCache
              .save(identifierValue, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map { _ =>
                Right(optRetrievedSubscription)
              }

          }
          .recover { case NonFatal(_) =>
            Left(UnexpectedResponseFromSDIL)
          }
    }
  }
  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit
    hc: HeaderCarrier
  ): VariationResult[Option[Boolean]] = EitherT {
    sdilSessionCache.fetchEntry[OptSmallProducer](sdilRef, SDILSessionKeys.smallProducerForPeriod(period)).flatMap {
      case Some(optSP) =>
        Future.successful(Right(optSP.optSmallProducer))
      case None =>
        executeGet[Option[Boolean]](
          operation = "checkSmallProducerStatus",
          path = s"/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"
        )
          .flatMap { optSP =>
            sdilSessionCache
              .save(sdilRef, SDILSessionKeys.smallProducerForPeriod(period), OptSmallProducer(optSP))
              .map(_ => Right(optSP))
          }
          .recover { case NonFatal(_) =>
            Left(UnexpectedResponseFromSDIL)
          }
    }
  }

  def balance(
    sdilRef: String,
    withAssessment: Boolean
  )(implicit hc: HeaderCarrier): VariationResult[BigDecimal] = EitherT {
    sdilSessionCache.fetchEntry[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment)).flatMap {
      case Some(b) => Future.successful(Right(b))
      case None =>
        executeGet[BigDecimal](
          operation = "balance",
          path = s"/balance/$sdilRef/$withAssessment"
        )
          .flatMap { b =>
            sdilSessionCache
              .save[BigDecimal](sdilRef, SDILSessionKeys.balance(withAssessment), b)
              .map(_ => Right(b))
          }
          .recover { case NonFatal(_) =>
            Left(UnexpectedResponseFromSDIL)
          }
    }
  }

  def balanceHistory(
    sdilRef: String,
    withAssessment: Boolean
  )(implicit hc: HeaderCarrier): VariationResult[List[FinancialLineItem]] = EitherT {
    import FinancialLineItem.formatter
    sdilSessionCache
      .fetchEntry[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment))
      .flatMap {
        case Some(fli) => Future.successful(Right(fli))
        case None =>
          executeGet[List[FinancialLineItem]](
            operation = "balanceHistory",
            path = s"/balance/$sdilRef/history/all/$withAssessment"
          )
            .flatMap { fli =>
              sdilSessionCache
                .save[List[FinancialLineItem]](sdilRef, SDILSessionKeys.balanceHistory(withAssessment), fli)
                .map(_ => Right(fli))
            }
            .recover { case NonFatal(_) =>
              Left(UnexpectedResponseFromSDIL)
            }
      }
  }

  def getReturn(
    utr: String,
    period: ReturnPeriod
  )(implicit hc: HeaderCarrier): VariationResult[Option[SdilReturn]] = EitherT {
    sdilSessionCache
      .fetchEntry[OptPreviousSubmittedReturn](utr, SDILSessionKeys.previousSubmittedReturn(utr, period))
      .flatMap {
        case Some(optPreviousReturn) =>
          Future.successful(Right(optPreviousReturn.optReturn))
        case None =>
          executeGet[Option[SdilReturn]](
            operation = "getReturn",
            path = s"/returns/$utr/year/${period.year}/quarter/${period.quarter}"
          )
            .flatMap { optReturn =>
              sdilSessionCache
                .save[OptPreviousSubmittedReturn](
                  utr,
                  SDILSessionKeys.previousSubmittedReturn(utr, period),
                  OptPreviousSubmittedReturn(optReturn)
                )
                .map(_ => Right(optReturn))
            }
            .recover { case NonFatal(_) =>
              Left(UnexpectedResponseFromSDIL)
            }
      }
  }

  def getVariableReturnsFromCache(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] =
    EitherT {
      sdilSessionCache.fetchEntry[List[ReturnPeriod]](utr, SDILSessionKeys.VARIABLE_RETURNS).flatMap {
        case Some(variableReturns) => Future.successful(Right(variableReturns))
        case None =>
          returnsVariable(utr).value
      }
    }

  def returnsVariable(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    executeGet[List[ReturnPeriod]](
      operation = "returnsVariable",
      path = s"/returns/$utr/variable"
    )
      .flatMap { variableReturns =>
        sdilSessionCache
          .save(utr, SDILSessionKeys.VARIABLE_RETURNS, variableReturns)
          .map(_ => Right(variableReturns))
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }

  def getPendingReturnsFromCache(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] =
    EitherT {
      sdilSessionCache.fetchEntry[List[ReturnPeriod]](utr, SDILSessionKeys.RETURNS_PENDING).flatMap {
        case Some(pendingReturns) => Future.successful(Right(pendingReturns))
        case None =>
          returnsPending(utr).value
      }
    }

  def returnsPending(utr: String)(implicit hc: HeaderCarrier): VariationResult[List[ReturnPeriod]] = EitherT {
    executeGet[List[ReturnPeriod]](
      operation = "returnsPending",
      path = s"/returns/$utr/pending"
    )
      .flatMap { variableReturns =>
        sdilSessionCache
          .save(utr, SDILSessionKeys.RETURNS_PENDING, variableReturns)
          .map(_ => Right(variableReturns))
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }

  def submitVariation(variation: VariationsSubmission, sdilNumber: String)(implicit
    hc: HeaderCarrier
  ): VariationResult[Unit] = EitherT {
    val path = s"/submit-variations/sdil/$sdilNumber"
    executePost[HttpResponse](
      operation = "submitVariation",
      path = path,
      body = Json.toJson(variation)
    )(using hc, rawHttpReads)
      .map { resp =>
        resp.status match {
          case NO_CONTENT => Right((): Unit)
          case status =>
            logger.error(
              s"SDIL submitVariation unexpected-response ${sdilContext(path, status = Some(status))}"
            )
            Left(UnexpectedResponseFromSDIL)
        }
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }

  def submitSdilReturnsVary(sdilNumber: String, variation: ReturnVariationData)(implicit
    hc: HeaderCarrier
  ): VariationResult[Unit] = EitherT {
    val path = s"/returns/vary/$sdilNumber"
    executePost[HttpResponse](
      operation = "submitReturnsVariation",
      path = path,
      body = Json.toJson(variation)
    )(using hc, rawHttpReads)
      .map { resp =>
        resp.status match {
          case NO_CONTENT => Right((): Unit)
          case status =>
            logger.error(
              s"SDIL submitReturnsVariation unexpected-response ${sdilContext(path, status = Some(status))}"
            )
            Left(UnexpectedResponseFromSDIL)
        }
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }

  def submitReturnVariation(sdilRef: String, variation: ReturnsVariation)(implicit
    hc: HeaderCarrier
  ): VariationResult[Unit] = EitherT {
    val path = s"/returns/variation/sdil/$sdilRef"
    executePost[HttpResponse](
      operation = "returns_variation",
      path = path,
      body = Json.toJson(variation)
    )(using hc, rawHttpReads)
      .map { resp =>
        resp.status match {
          case NO_CONTENT => Right((): Unit)
          case status =>
            logger.error(
              s"SDIL returns_variation unexpected-response ${sdilContext(path, status = Some(status))}"
            )
            Left(UnexpectedResponseFromSDIL)
        }
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }

}
