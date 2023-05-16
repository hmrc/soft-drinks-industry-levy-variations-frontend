package testSupport.databases

import models.UserAnswers
import repositories.SessionRepository
import testSupport.TestConfiguration

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

trait SessionDatabaseOperations {

  self: TestConfiguration =>

  val sessionRespository: SessionRepository

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

}
