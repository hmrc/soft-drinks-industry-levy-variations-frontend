package testSupport.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import models.SdilReturn
import play.api.libs.json.Json

import scala.jdk.CollectionConverters._
object checkReturnsVariationSubmission {

  def checkReturnVariationSubmissionSent(wireMockServer: WireMockServer, expectedReturn: SdilReturn): Boolean = {
    val requestToSendReturn =
      wireMockServer.getAllServeEvents.asScala.toList.map(_.getRequest).filter(_.getUrl.contains("/returns/vary/")).head
    val returnSentRequestBody = Json.parse(requestToSendReturn.getBodyAsString)
    val expectedReturnJson = Json.toJson(expectedReturn)

    val returnIsSent = returnSentRequestBody == expectedReturnJson

    returnIsSent
  }

}
