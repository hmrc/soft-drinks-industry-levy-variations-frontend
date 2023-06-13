package pages.updateRegisteredDetails

import play.api.libs.json.JsPath
import pages.QuestionPage

case object PackingSiteDetailsRemovePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "updateRegisteredDetails"
  override def toString: String = "packingSiteDetailsRemove"
}
