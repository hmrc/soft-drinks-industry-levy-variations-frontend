package testSupport.databases

import models.UserAnswers
import repositories.SessionRepository
import testSupport.TestConfiguration

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

trait SessionDatabaseOperations {

  self: TestConfiguration =>

  val sessionRespository: SessionRepository

  def setAnswers(userAnswers: UserAnswers)(implicit timeout: Duration, ec: ExecutionContext): Unit = Await.result(
    sessionRespository.set(userAnswers).map(_ => ()),
    timeout
  )

  def getAnswers(id: String)(implicit timeout: Duration, ec: ExecutionContext): Option[UserAnswers] = Await.result(
    sessionRespository.get(id).map{
      case Left(_) => None
      case Right(optUA) => optUA
    },
    timeout
  )

  def remove(id: String)(implicit timeout: Duration, ec: ExecutionContext): Boolean = Await.result(
    sessionRespository.clear(id).map {
      case Left(_) => false
      case Right(x) => x
    },
    timeout
  )

}
