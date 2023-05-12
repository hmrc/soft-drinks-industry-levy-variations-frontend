package pages

import play.api.libs.json.JsPath
import models.LitresInBands

case object HowMany$className$Page extends QuestionPage[LitresInBands] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "howMany$className$"
}
