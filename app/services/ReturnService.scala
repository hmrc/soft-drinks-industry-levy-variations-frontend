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

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.backend.FinancialLineItem
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger

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


}
