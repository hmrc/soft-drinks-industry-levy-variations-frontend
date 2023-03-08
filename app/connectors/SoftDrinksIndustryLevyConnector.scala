package connectors

import models.{FinancialLineItem, ReturnPeriod}
import play.api.Configuration
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import models.retrieved.RetrievedSubscription

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
                                                 val http: HttpClient,
                                                 val configuration: Configuration
                                               )(implicit ec: ExecutionContext)
  extends ServicesConfig(configuration) {

  lazy val sdilUrl: String = baseUrl("soft-drinks-industry-levy")

  private def getSubscriptionUrl(sdilNumber: String,identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$sdilNumber"

  def retrieveSubscription(sdilNumber: String, identifierType: String)(implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] =
    http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(sdilNumber: String,identifierType)).map {
      case Some(a) => Some(a)
      case _ => None
    }

  private def smallProducerUrl(sdilRef:String, period:ReturnPeriod):String = s"$sdilUrl/subscriptions/sdil/$sdilRef/year/${period.year}/quarter/${period.quarter}"

  def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    http.GET[Option[Boolean]](smallProducerUrl(sdilRef,period)).map {
      case Some(a) => Some(a)
      case _ => None
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
