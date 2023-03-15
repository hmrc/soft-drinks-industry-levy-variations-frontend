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

import models.{FinancialLineItem, ReturnPeriod, ReturnVariationData, ReturnsVariation, SdilReturn, VariationsSubmission}
import play.api.Configuration
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import models.retrieved.RetrievedSubscription
import repositories.{SDILSessionCache, SDILSessionKeys}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
                                                 val http: HttpClient,
                                                 val configuration: Configuration
                                                 sdilSessionCache: SDILSessionCache
                                               )(implicit ec: ExecutionContext)
  extends ServicesConfig(configuration) {

  lazy val sdilUrl: String = baseUrl("soft-drinks-industry-levy")

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription (sdilNumber: String, identifierType: String) (implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] = {
    sdilSessionCache.fetchEntry[RetrievedSubscription] (sdilNumber, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some (subscription) => Future.successful (Some (subscription) )
      case None =>
        http.GET[Option[RetrievedSubscription]] (getSubscriptionUrl (sdilNumber: String, identifierType) ).flatMap {
          case Some (a) =>
            sdilSessionCache.save (a.sdilRef, SDILSessionKeys.SUBSCRIPTION, a)
              .map {_ => Some (a)}
          case _ => Future.successful (None)
        }
    }
  }

  private def smallProducerUrl(sdilRef:String, period:ReturnPeriod):String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    http.GET[Option[Boolean]](smallProducerUrl(sdilRef,period)).map {
      case Some(a) => Some(a)
      case _ => None
    }

  def submitVariation(variation: VariationsSubmission, sdilNumber: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST[VariationsSubmission, HttpResponse](s"$sdilUrl/submit-variations/sdil/$sdilNumber", variation) map { _ =>
      ()
    }

  def returns_pending(
                       utr: String
                     )(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] =
    http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")

  def returns_variable(
                        utr: String
                      )(implicit hc: HeaderCarrier): Future[List[ReturnPeriod]] =
    http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/variable")

  def returns_vary(
                    sdilRef: String,
                    data: ReturnVariationData
                  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val uri = s"$sdilUrl/returns/vary/$sdilRef"
    http.POST[ReturnVariationData, HttpResponse](uri, data) map { _ =>
      ()
    }
  }

  def returns_get(
                   utr: String,
                   period: ReturnPeriod
                 )(implicit hc: HeaderCarrier): Future[Option[SdilReturn]] = {
    val uri = s"$sdilUrl/returns/$utr/year/${period.year}/quarter/${period.quarter}"
    http.GET[Option[SdilReturn]](uri)
  }

  def returns_variation(variation: ReturnsVariation, sdilRef: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST[ReturnsVariation, HttpResponse](s"$sdilUrl/returns/variation/sdil/$sdilRef", variation) map { _ =>
      ()
    }

  def oldestPendingReturnPeriod(utr: String)(implicit hc: HeaderCarrier): Future[Option[ReturnPeriod]] = {
    val returnPeriods = http.GET[List[ReturnPeriod]](s"$sdilUrl/returns/$utr/pending")
    returnPeriods.map(_.sortBy(_.year).sortBy(_.quarter).headOption)
  }

  def balance(
               sdilRef: String,
               withAssessment: Boolean
             )(implicit hc: HeaderCarrier): Future[BigDecimal] =
    http.GET[BigDecimal](s"$sdilUrl/balance/$sdilRef/$withAssessment")

  def balanceHistory(
                      sdilRef: String,
                      withAssessment: Boolean
                    )(implicit hc: HeaderCarrier): Future[List[FinancialLineItem]] = {
    import FinancialLineItem.formatter
    http.GET[List[FinancialLineItem]](s"$sdilUrl/balance/$sdilRef/history/all/$withAssessment")
  }
}
