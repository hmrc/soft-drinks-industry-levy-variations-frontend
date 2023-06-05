package pages.$packageName$

import java.time.LocalDate

import play.api.libs.json.JsPath
import pages.QuestionPage

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"

  override def toString: String = "$className;format="decap"$"
}
