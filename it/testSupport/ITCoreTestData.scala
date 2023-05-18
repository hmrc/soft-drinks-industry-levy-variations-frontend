package testSupport

import controllers.routes
import models._
import org.scalatest.TryValues
import pages._
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

trait ITCoreTestData extends TryValues {

  val year = 2022
  val month = 11
  val day = 10
  val date = LocalDate.of(year, month, day)

  val validDateJson = Json.obj(
    "value.day" -> day.toString,
    "value.month" -> month.toString,
    "value.year" -> year.toString
  )

  val dateMap = Map("day" -> day, "month" -> month, "year" -> year)

  def sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")

  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(sdilNumber, SelectChange.UpdateRegisteredAccount, Json.obj())

  val defaultCall = routes.IndexController.onPageLoad
}
