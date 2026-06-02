package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._

case class UserStub()(implicit builder: PreconditionBuilder) {
  private val utr = "0000001611"
  private val sdilRef = "XKSDIL000000022"
  private val inactiveSdilRef = "XKSDIL000000026"

  def isAuthorisedButNotEnrolled() = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolled = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "0000001611"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolledNone = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "0000001622"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolledWithInactiveAndActiveSdilRefs = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$inactiveSdilRef"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilRef"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolledWithUtrInactiveAndActiveSdilRefs = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "$utr"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$inactiveSdilRef"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "$sdilRef"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isNotAuthorised(reason: String = "MissingBearerToken") = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(unauthorized().withHeader("WWW-Authenticate", s"""MDTP detail="$reason""""))
    )

    builder
  }

}
