import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "9.13.0"
  private val hmrcMongoVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"            % "11.13.0",
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-$playVersion" % "3.3.0",
    "uk.gov.hmrc"       %% s"crypto-json-$playVersion"                   % "8.2.0",
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"                    % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                                   % "2.12.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"                       % "1.18.1",
    "org.mockito"            %% "mockito-scala-scalatest"     % "1.17.30",
    "org.scalatestplus"      %% "scalacheck-1-17"             % "3.2.17.0",
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"       % "1.1.0"
  ).map(_ % "test, it")

val all: Seq[ModuleID] = compile ++ test
}