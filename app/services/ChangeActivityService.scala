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
import models.backend.Site
import models.{Convert, Litreage, Producer, RegistrationVariationData, RetrievedSubscription, UserAnswers, VariationsSubmission}
import pages.changeActivity._
import play.api.Logger
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeActivityService @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                                      config: FrontendAppConfig){

  val logger: Logger = Logger(this.getClass())

  private def changeActivityVariationToBeSubmitted(subscription: RetrievedSubscription,  userAnswers: UserAnswers): VariationsSubmission = {
    Convert(
      RegistrationVariationData(
      subscription
      ).copy(
        producer = Producer(userAnswers.get(AmountProducedPage).isDefined, Some(userAnswers.get(AmountProducedPage).contains(true))),
        usesCopacker = userAnswers.get(ThirdPartyPackagersPage),
        packageOwn = userAnswers.get(OperatePackagingSiteOwnBrandsPage),
        packageOwnVol = userAnswers.get(HowManyOperatePackagingSiteOwnBrandsPage).map(litreage => Litreage(litreage.lowBand, litreage.highBand)),
        copackForOthers = userAnswers.get(ContractPackingPage).isDefined,
        copackForOthersVol = userAnswers.get(HowManyContractPackingPage).map(litreage => Litreage(litreage.lowBand, litreage.highBand)),
        imports = userAnswers.get(ImportsPage).isDefined,
        importsVol = userAnswers.get(HowManyImportsPage).map(litreage => Litreage(litreage.lowBand, litreage.highBand)),
        updatedProductionSites = userAnswers.packagingSiteList.values.toSeq,
        updatedWarehouseSites = userAnswers.warehouseList.values.map(warehouse => Site(address = warehouse.address, tradingName = warehouse.tradingName,ref =  None, closureDate = None)).toSeq
    )
  )
  }

  private def submitVariation(variation: VariationsSubmission, sdilRef: String)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    sdilConnector.submitVariation( variation, sdilRef).map {
      case Some(NO_CONTENT) => logger.info(s"Return variation submitted for $sdilRef")
      case _ => logger.error(s"Failed to submit return variation for $sdilRef")
        throw new RuntimeException(s"Failed to submit return variation $sdilRef")
    }
  }

  def submitVariation(subscription: RetrievedSubscription, userAnswers: UserAnswers)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val changeActivityVariation = changeActivityVariationToBeSubmitted(subscription, userAnswers)
    for {
      variation <- submitVariation(changeActivityVariation, subscription.sdilRef)
    } yield variation
  }
}
