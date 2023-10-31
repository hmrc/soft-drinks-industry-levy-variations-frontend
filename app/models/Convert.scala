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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package models
//
//import models.backend.{RetrievedSubscription, Site}
//import models.enums.SiteTypes.{PRODUCTION_SITE, WAREHOUSE}
//import models.submission.{Activity, ClosedSite, SdilActivity, VariationSubmissionHelper, VariationsContact, VariationsPersonalDetails, VariationsSite, VariationsSites, VariationsSubmission}
//import models.updateRegisteredDetails.ContactDetails
//import pages.updateRegisteredDetails.UpdateContactDetailsPage
//
//import java.time.LocalDate
////import scala.language.reflectiveCalls
//import scala.util.Try
//
//object Convert extends VariationSubmissionHelper {
//  def applyForUpdateRegisteredDetails(userAnswers: UserAnswers, subscription: RetrievedSubscription): VariationsSubmission = {
//    val contactDetails = userAnswers.get(UpdateContactDetailsPage)
//      .getOrElse(ContactDetails.fromContact(subscription.contact))
//    val variationSites = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetails)
//    VariationsSubmission(
//      displayOrgName = subscription.orgName,
//      ppobAddress = subscription.address,
//      sdilActivity = userAnswers.getChangeActivityData.map(SdilActivity.fromChangeActivityData(_, subscription, todaysDate)),
//      newSites = variationSites.newSites,
//      closeSites = variationSites.closedSites
//    )
//  }
//
//
//  def applyForDeregistration(userAnswers: UserAnswers, subscription: RetrievedSubscription): VariationsSubmission = {
//    val contactDetails = ContactDetails.fromContact(subscription.contact)
//    val variationSites = VariationsSites.fromUserAnswers(userAnswers, subscription, contactDetails)
//    VariationsSubmission(
//      displayOrgName = subscription.orgName,
//      ppobAddress = subscription.address,
//      sdilActivity = userAnswers.getChangeActivityData.map(SdilActivity.fromChangeActivityData(_, subscription, todaysDate)),
//      newSites = variationSites.newSites,
//      closeSites = variationSites.closedSites
//    )
//  }
//
//  def apply(userAnswers: UserAnswers, subscription: RetrievedSubscription, todaysDate: LocalDate = LocalDate.now()): VariationsSubmission = {
//    val orig = subscription
//
//    val newBusinessContact = VariationsContact.generateForBusinessContact(userAnswers, subscription)
//
//    val newPersonalDetails =  VariationsPersonalDetails.apply(userAnswers, subscription)
////    {
////      val contact = vd.updatedContactDetails
////      val original = orig.contact
////
////      VariationsPersonalDetails(
////        contact.fullName ifDifferentTo original.name.getOrElse(""),
////        contact.position ifDifferentTo original.positionInCompany.getOrElse(""),
////        contact.phoneNumber ifDifferentTo original.phoneNumber,
////        contact.email ifDifferentTo original.email
////      )
////    }
//
//    def variationsSites(productionSites: Seq[Site], warehouses: Seq[Site]): List[VariationsSite] = {
//
//      val highestNum = getHighestRefNumber(orig.productionSites ++ orig.warehouseSites)
//      val contactDetails = ContactDetails.fromContact(subscription.contact)
//
//      val ps = productionSites.zipWithIndex map {
//        case (site, id) =>
//          val newRef = highestNum + id + 1
//          VariationsSite.generateFromSite(site, contactDetails, newRef, PRODUCTION_SITE)
//      }
//
//      val w = warehouses.zipWithIndex map {
//        case (warehouse, id) =>
//          val newRef = highestNum + id + 1 + productionSites.size
//          VariationsSite.generateFromSite(warehouse, contactDetails, newRef, WAREHOUSE)
//      }
//
//      (ps ++ w).toList
//    }
//
//    val variationSites = VariationsSites.fromUserAnswers(userAnswers, subscription,)
//
//    val newSites: List[VariationsSite] = {
//      val newProductionSites =
//        userAnswers.packagingSiteList.values.toList.diff(orig.productionSites.filter(_.closureDate.forall(_.isAfter(LocalDate.now))))
//      val newWarehouses =
//        vd.updatedWarehouseSites.diff(orig.warehouseSites.filter(_.closureDate.forall(_.isAfter(LocalDate.now))))
//
//      variationsSites(newProductionSites, newWarehouses)
//    }
//
//    val closedSites: List[ClosedSite] = {
//
//      val closedProductionSites = orig.productionSites
//        .filter(_.closureDate.forall(_.isAfter(LocalDate.now)))
//        .diff(vd.updatedProductionSites)
//        .map { site =>
//          ClosedSite("", site.ref.getOrElse("1"), "This site is no longer open.")
//        }
//
//      val closedWarehouses = orig.warehouseSites
//        .filter(_.closureDate.forall(_.isAfter(LocalDate.now)))
//        .diff(
//          vd.updatedWarehouseSites
//        ) map { warehouse =>
//        ClosedSite("", warehouse.ref.getOrElse("1"), "This site is no longer open.")
//      }
//
//      closedProductionSites ++ closedWarehouses
//    }
//
//    VariationsSubmission(
//      displayOrgName = orig.orgName,
//      ppobAddress = orig.address,
//      businessContact = newBusinessContact.ifNonEmpty,
//      correspondenceContact = newBusinessContact.ifNonEmpty,
//      primaryPersonContact = if(newPersonalDetails.nonEmpty) {
//        Some(newPersonalDetails)
//      } else {
//        None
//      },
//      sdilActivity = SdilActivity.fromChangeActivityData(userAnswers.getChangeActivityData.get, subscription, todaysDate),
//      deregistrationText = vd.reason,
//      deregistrationDate = vd.deregDate,
//      newSites = newSites,
//      amendSites = Nil,
//      closeSites = closedSites
//    )
//  }
