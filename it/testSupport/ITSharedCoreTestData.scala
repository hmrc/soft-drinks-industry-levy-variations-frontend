package testSupport

import models.backend.UkAddress

trait ITSharedCoreTestData {
  val ukAddress = UkAddress(List("foo", "bar"),"wizz", None)
}
