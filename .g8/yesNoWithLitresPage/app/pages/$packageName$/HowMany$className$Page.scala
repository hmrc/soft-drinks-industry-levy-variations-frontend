package pages

import play.api.libs.json.JsPath
import models.LitresInBands

case object HowMany$className$Page extends QuestionPage[LitresInBands] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"
  override def toString: String = "howMany$className$"
}
