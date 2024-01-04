import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val hmrcMongoVersion = "1.6.0"
  val playSuffix = s"-play-30"
  val bootstrapVersion = "8.3.0"
  val playFrontendHMRCVersion = "8.2.0"

  private val scalaTestPlusPlayVersion = "7.0.0"
  private val scalatestPlusScalacheckVersion = "3.2.17.0"
  private val mockitoScalatestVersion = "1.17.30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc$playSuffix"             % playFrontendHMRCVersion,
    "uk.gov.hmrc"       %% s"bootstrap-frontend$playSuffix" % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping$playSuffix"  % "2.0.0",
    "org.typelevel"     %% "cats-core"                      % "2.10.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playSuffix"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"crypto-json$playSuffix"        % "7.6.0"
  )

  val test: Seq[ModuleID] = Seq(
//    "org.scalatest"           %% "scalatest"                    % "3.2.16",
//    "org.scalatestplus"       %% "scalacheck-1-15"              % "3.2.11.0",
//    "org.scalatestplus"       %% "mockito-3-4"                  % "3.2.10.0",
//    "org.scalatestplus.play"  %% "scalatestplus-play"           % "5.1.0",
//    "org.pegdown"             %  "pegdown"                      % "1.6.0",
    "org.mockito" %% "mockito-scala-scalatest" % mockitoScalatestVersion,
    "org.scalatestplus" %% "scalacheck-1-17" % scalatestPlusScalacheckVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion,
    "org.jsoup"               %  "jsoup"                        % "1.15.4",
    "uk.gov.hmrc" %% s"bootstrap-test$playSuffix" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playSuffix" % hmrcMongoVersion,
//    "com.typesafe.play"       %% "play-test"                    % PlayVersion.current,
//    "org.mockito"             %% "mockito-scala"                % "1.17.29",
//    "org.scalacheck"          %% "scalacheck"                   % "1.17.0",
//    "uk.gov.hmrc"             %% s"bootstrap-test$playSuffix"   % bootstrapVersion,
//    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-test$playSuffix"  % hmrcMongoVersion,
//    "com.vladsch.flexmark"    %  "flexmark-all"                 % "0.62.0",
//    "com.github.tomakehurst"  % "wiremock-standalone"           % "2.27.2",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"        % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}