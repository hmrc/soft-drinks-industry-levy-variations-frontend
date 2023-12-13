package testSupport.databases

import models.backend.OptPreviousSubmittedReturn
import models.submission.Litreage
import models.{ReturnPeriod, SdilReturn, UserAnswers}
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import testSupport.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SessionDatabaseOperations {

  self: TestConfiguration =>

  val sessionRespository: SessionRepository
  val sdilSessionCache: SDILSessionCache

  val defaultOriginalReturn = SdilReturn(Litreage(0, 0), Litreage(0, 0), List.empty, Litreage(0, 0), Litreage(0, 0),
    Litreage(0, 0), Litreage(0, 0), submittedOn = None)

  def setUpForCorrectReturn(userAnswers: UserAnswers, optOriginalReturn: Option[SdilReturn] = Some(defaultOriginalReturn))(implicit timeout: Duration) = {
    userAnswers.correctReturnPeriod match {
      case Some(returnPeriod) =>
        setOriginalReturn(returnPeriod, optOriginalReturn = optOriginalReturn)
        setAnswers(userAnswers)
      case _ => setAnswers(userAnswers)
    }
  }

  def setAnswers(userAnswers: UserAnswers)(implicit timeout: Duration): Unit = Await.result(
    sessionRespository.set(userAnswers),
    timeout
  )

  def getAnswers(id: String)(implicit timeout: Duration): Option[UserAnswers] = Await.result(
    sessionRespository.get(id),
    timeout
  )

  def remove(id: String)(implicit timeout: Duration): Boolean = Await.result(
    sessionRespository.clear(id),
    timeout
  )

  def setOriginalReturn(returnPeriod: ReturnPeriod,
                        utr: String = "0000001611",
                        optOriginalReturn: Option[SdilReturn])
                       (implicit timeout: Duration) = {

    val sessionKey = SDILSessionKeys.previousSubmittedReturn(utr, returnPeriod)
    Await.result(
      sdilSessionCache.save(utr, sessionKey, OptPreviousSubmittedReturn(optOriginalReturn)),
      timeout
    )
  }

}
