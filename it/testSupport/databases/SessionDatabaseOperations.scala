package testSupport.databases

import models.{ReturnPeriod, UserAnswers}
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import testSupport.TestConfiguration

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

trait SessionDatabaseOperations {

  self: TestConfiguration =>

  val sessionRespository: SessionRepository
  val sdilSessionCache: SDILSessionCache

  def setAnswers(userAnswers: UserAnswers, returnPeriod1: Option[ReturnPeriod] = Some(returnPeriod))(implicit timeout: Duration): Unit = Await.result({
    sessionRespository.set(userAnswers).flatMap(_ => returnPeriod1 match {
      case Some(rt) => sdilSessionCache.save[ReturnPeriod](userAnswers.id, SDILSessionKeys.RETURN_PERIOD, rt)
        .map(_ => ())
      case None => Future.successful(())
    })
  }, timeout)

//  def setAnswers(userAnswers: UserAnswers)(implicit timeout: Duration): Unit = Await.result(
//    sessionRespository.set(userAnswers),
//    timeout
//  )

  def getAnswers(id: String)(implicit timeout: Duration): Option[UserAnswers] = Await.result(
    sessionRespository.get(id),
    timeout
  )

  def remove(id: String)(implicit timeout: Duration): Boolean = Await.result(
    sessionRespository.clear(id),
    timeout
  )

}
