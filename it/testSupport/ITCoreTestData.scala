package testSupport

import controllers.routes
import org.scalatest.TryValues
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

trait ITCoreTestData
  extends TryValues
    with ITCoreTestDataForUpdateRegisteredDetails
    with ITCoreTestDataForCancelRegistration
    with ITCoreTestDataForChangeActivity
    with ITCoreTestDataForCorrectReturn {

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

  val defaultCall = routes.IndexController.onPageLoad
}
