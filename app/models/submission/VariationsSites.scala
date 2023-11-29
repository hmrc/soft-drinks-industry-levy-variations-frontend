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

package models.submission

import models.backend.{RetrievedSubscription, Site}
import models.enums.SiteTypes.{PRODUCTION_SITE, WAREHOUSE}
import models.updateRegisteredDetails.ContactDetails
import models.UserAnswers

case class VariationsSites(newSites: List[VariationsSite] = List.empty, closedSites: List[ClosedSite] = List.empty)

object VariationsSites extends VariationSubmissionHelper {

  def fromUserAnswers(userAnswers: UserAnswers, subscription: RetrievedSubscription, contactDetails: ContactDetails): VariationsSites = {
    val (newPackagingSites, closedPackagingSites) = getNewAndClosedPackagingSites(userAnswers, subscription)
    val (newWarehouses, closedWarehouses) = getNewAndClosedWarehouses(userAnswers, subscription)
    val allOriginalSites = subscription.productionSites ++ subscription.warehouseSites
    val highestExistingRefNumber = getHighestRefNumber(allOriginalSites)

    val newPSites = newPackagingSites.zipWithIndex.map {
      case (site, id) =>
        val newRef = highestExistingRefNumber + id + 1
        VariationsSite.generateFromSite(site, contactDetails, newRef, PRODUCTION_SITE)
    }

    val newWSites = newWarehouses.zipWithIndex.map {
      case (site, id) =>
        val newRef = highestExistingRefNumber + id + 1 + newPackagingSites.size
        VariationsSite.generateFromSite(site, contactDetails, newRef, WAREHOUSE)
    }

    val newSites = newPSites ++ newWSites
    val closedSites = (closedPackagingSites ++ closedWarehouses).map(ClosedSite.fromSite)

    VariationsSites(
      if (newSites.nonEmpty) newSites else Nil,
      if (closedSites.nonEmpty) closedSites else Nil
    )
  }

  private def getNewAndClosedPackagingSites(userAnswers: UserAnswers, subscription: RetrievedSubscription): (List[Site], List[Site]) = {
    val packagingSites = userAnswers.packagingSiteList.values.toList
    val originalPackagingSites = subscription.productionSites
    getNewAndClosedSites(originalPackagingSites, packagingSites)
  }

  private def getNewAndClosedWarehouses(userAnswers: UserAnswers, subscription: RetrievedSubscription): (List[Site], List[Site]) = {
    val warehouses = userAnswers.warehouseList.values.toList
    val originalWarehouseSites = subscription.warehouseSites
    getNewAndClosedSites(originalWarehouseSites, warehouses)
  }

  private def getNewAndClosedSites(originalSites: List[Site], updatedSites: List[Site]): (List[Site], List[Site]) = {
    if (originalSites.nonEmpty && updatedSites.nonEmpty) {
      val newSites = updatedSites.filter(_.isNew(originalSites))
      val closedSites = originalSites.filter(_.isClosed(updatedSites))
      (newSites, closedSites)
    } else {
      (updatedSites, originalSites)
    }
  }
}
