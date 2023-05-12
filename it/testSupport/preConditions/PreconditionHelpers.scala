package testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr","0000001611")
      .sdilBackend.retrieveSubscription("sdil","XKSDIL000000022")
  }

  def unauthorisedPrecondition = {
    builder
      .user.isNotAuthorised()
  }

}
