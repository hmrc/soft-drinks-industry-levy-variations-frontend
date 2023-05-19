package pages.$packageName$

import play.api.libs.json.JsPath
import models.LitresInBands
import pages.QuestionPage

case object HowMany$className$Page extends QuestionPage[LitresInBands] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"
  override def toString: String = "howMany$className$"
}
