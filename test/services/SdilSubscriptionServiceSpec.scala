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

package services

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import errors.UnexpectedResponseFromSDIL
import org.mockito.Mockito.{ reset, when }
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class SdilSubscriptionServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val service = new SdilSubscriptionService(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "resolveActiveSdilRef" - {
    "return first SDIL ref when it is active" in {
      val active = aSubscription.copy(deregDate = None)
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(active)))
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef2", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1", "sdilRef2"))

      result.futureValue mustBe Some("sdilRef1")
    }

    "skip expired ref and return next active one" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))
      val active = aSubscription.copy(deregDate = None)

      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(expired)))
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef2", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(active)))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1", "sdilRef2"))

      result.futureValue mustBe Some("sdilRef2")
    }

    "return None if multiple refs are all expired" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(expired)))
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef2", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1", "sdilRef2"))

      result.futureValue mustBe None
    }

    "return the ref when it is the only SDIL enrolment and is expired" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1"))

      result.futureValue mustBe Some("sdilRef1")
    }

    "return None when it is the only SDIL enrolment but no subscription is found" in {
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createSuccessVariationResult(None))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1"))

      result.futureValue mustBe None
    }

    "return None if no refs provided" in {
      val result = service.resolveActiveSdilRef(Seq.empty)

      result.futureValue mustBe None
    }

    "skip a ref when connector returns an error" in {
      val active = aSubscription.copy(deregDate = None)

      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef2", "sdil"))
        .thenReturn(createSuccessVariationResult(Some(active)))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1", "sdilRef2"))

      result.futureValue mustBe Some("sdilRef2")
    }

    "fail when lookup errors and no active ref is found" in {
      when(mockConnector.retrieveSubscriptionNoCache("sdilRef1", "sdil"))
        .thenReturn(createFailureVariationResult(UnexpectedResponseFromSDIL))

      val result = service.resolveActiveSdilRef(Seq("sdilRef1"))

      result.failed.futureValue mustBe a[IllegalStateException]
    }
  }

  "isActive" - {
    "return true when deregistrationDate is None" in {
      service.isActive(aSubscription.copy(deregDate = None)) mustBe true
    }

    "return false when deregistrationDate is in the past" in {
      service.isActive(aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))) mustBe false
    }

    "return true when deregistrationDate is in the future" in {
      service.isActive(aSubscription.copy(deregDate = Some(LocalDate.now.plusDays(5)))) mustBe true
    }
  }
}
