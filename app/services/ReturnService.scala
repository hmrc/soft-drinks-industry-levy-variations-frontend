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

package services

import cats.data.EitherT
import cats.implicits._
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import errors.VariationsErrors
import models.{Amounts, ReturnPeriod, SdilReturn, UserAnswers}
import models.backend.FinancialLineItem
import play.api.Logger
import service.VariationResult
import uk.gov.hmrc.http.HeaderCarrier
import utilities._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnService @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                              config: FrontendAppConfig,
                              genericLogger: GenericLogger) {
  val logger: Logger = Logger(this.getClass())

  private def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal = l.headOption.fold(BigDecimal(0))(_._2)

  private def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
  }

  def calculateAmounts(sdilRef: String,
                       userAnswers: UserAnswers,
                       returnPeriod: ReturnPeriod)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): VariationResult[Amounts] = {
    for {
      isSmallProducer <- sdilConnector.checkSmallProducerStatus(sdilRef, returnPeriod)
      balanceBroughtForward <- EitherT.right[VariationsErrors](getBalanceBroughtForward(sdilRef))
    } yield getAmounts(userAnswers, balanceBroughtForward, isSmallProducer.getOrElse(false))

  }


  def getBalanceBroughtForward(sdilRef: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[BigDecimal] = {
    if (config.balanceAllEnabled) {
      sdilConnector.balanceHistory(sdilRef, withAssessment = false).map { financialItem =>
        extractTotal(listItemsWithTotal(financialItem))
      }
    } else {
      sdilConnector.balance(sdilRef, withAssessment = false)
    }
  }

  private def getAmounts(userAnswers: UserAnswers, balanceBroughtForward: BigDecimal, isSmallProducer: Boolean): Amounts = {
    val originalReturnTotal: BigDecimal = userAnswers.getCorrectReturnOriginalSDILReturnData.map(_.total).getOrElse(BigDecimal(0))
    val totalForQuarter = SdilReturn.apply(userAnswers).total
    val totalForQuarterLessForwardBalance = totalForQuarter - balanceBroughtForward
    val netAdjustedAmount: BigDecimal = (totalForQuarter - originalReturnTotal) - balanceBroughtForward
    Amounts(originalReturnTotal, totalForQuarter, balanceBroughtForward, totalForQuarterLessForwardBalance, netAdjustedAmount)
  }

}
