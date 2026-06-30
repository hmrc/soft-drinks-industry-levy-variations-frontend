/*
 * Copyright 2026 HM Revenue & Customs
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

package testSupport.preConditions

import models.ReturnPeriod
import models.backend.RetrievedSubscription
import testSupport.SDILBackendTestData.subscriptionDeregistered

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .retrieveSubscription("sdil", "XKSDIL000000022")
      .sdilBackend
      .returns_variable("0000001611")
      .sdilBackend
      .returns_pending("0000001611")
      .sdilBackend
      .calculateLevyDefault()
      .sdilBackend
      .calculateLevy(BigDecimal("180"), BigDecimal("480"), 1000L, 2000L)

  def commonPreconditionDereg =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611", subscriptionDeregistered)
      .sdilBackend
      .retrieveSubscription("sdil", "XKSDIL000000022", subscriptionDeregistered)
      .sdilBackend
      .returns_variable("0000001611")
      .sdilBackend
      .returns_pending("0000001611")
      .sdilBackend
      .calculateLevyDefault()
      .sdilBackend
      .calculateLevy(BigDecimal("180"), BigDecimal("480"), 1000L, 2000L)

  def commonPreconditionEmptyReturn =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .retrieveSubscription("sdil", "XKSDIL000000022")
      .sdilBackend
      .returns_variable("0000001611")
      .sdilBackend
      .no_returns_pending("0000001611")

  def commonPreconditionChangeSubscription(retrievedSubscription: RetrievedSubscription): PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611", retrievedSubscription)
      .sdilBackend
      .returns_pending("0000001611")
      .sdilBackend
      .calculateLevyDefault()
      .sdilBackend
      .calculateLevy(BigDecimal("180"), BigDecimal("480"), 1000L, 2000L)

  def returnPendingNotFoundPreCondition =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .retrieveSubscription("sdil", "XKSDIL000000022")
      .sdilBackend
      .returns_variable("0000001611")
      .sdilBackend
      .no_returns_pending("0000001611")

  def unauthorisedPrecondition =
    builder.user.isNotAuthorised()

  def authorisedButNoEnrolmentsPrecondition =
    builder.user.isAuthorisedButNotEnrolled()

  def authorisedButNoSdilSubscriptionPrecondition =
    builder.user.isAuthorisedAndEnrolledNone.sdilBackend
      .retrieveSubscriptionNone("utr", "0000001622")
      .sdilBackend
      .retrieveSubscriptionNone("sdil", "XKSDIL000000022")

  def smallProducerStatus(sdilRef: String, period: ReturnPeriod, smallProducerStatus: Boolean): PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend.checkSmallProducerStatus(sdilRef, period, smallProducerStatus)

  def smallProducerStatusError(sdilRef: String, period: ReturnPeriod): PreconditionBuilder =
    builder.user.isAuthorisedAndEnrolled.sdilBackend.checkSmallProducerStatusError(sdilRef, period)

}
