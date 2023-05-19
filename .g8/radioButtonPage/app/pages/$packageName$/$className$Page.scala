package pages

import models.$className$
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[$className$] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"
  override def toString: String = "$className;format="decap"$"
}
