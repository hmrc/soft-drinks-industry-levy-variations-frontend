package testSupport.preConditions

import models.{RetrievedSubscription, ReturnPeriod}

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611")
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022")
      .sdilBackend.returns_variable("0000001611")
      .sdilBackend.returns_pending("0000001611")
  }

  def commonPreconditionEmptyReturn = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611")
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022")
      .sdilBackend.returns_variable("0000001611")
      .sdilBackend.no_returns_pending("0000001611")
  }

  def commonPreconditionChangeSubscription(retrievedSubscription: RetrievedSubscription): PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000001611", retrievedSubscription)
      .sdilBackend.returns_pending("0000001611")
  }

  def returnPendingNotFoundPreCondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000001611")
      .sdilBackend.retrieveSubscription("sdil", "XKSDIL000000022")
      .sdilBackend.returns_variable("0000001611")
      .sdilBackend.no_returns_pending("0000001611")
  }

  def unauthorisedPrecondition = {
    builder
      .user.isNotAuthorised()
  }

  def authorisedButNoEnrolmentsPrecondition = {
    builder
      .user.isAuthorisedButNotEnrolled()
  }

  def authorisedButNoSdilSubscriptionPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend.retrieveSubscriptionNone("sdil", "XKSDIL000000022")  }

  def smallProducerStatus(sdilRef: String, period: ReturnPeriod, smallProducerStatus: Boolean): PreconditionBuilder = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.checkSmallProducerStatus(sdilRef, period, smallProducerStatus)
  }

}
