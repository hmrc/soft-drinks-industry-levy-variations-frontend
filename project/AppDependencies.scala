import sbt._

object AppDependencies {

  private val playSuffix = "-play-30"
  private val bootstrapVersion = "8.3.0"
  private val hmrcMongoVersion = "1.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc$playSuffix"            % "8.2.0",
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping$playSuffix" % "2.0.0",
    "uk.gov.hmrc"       %% s"crypto-json$playSuffix"                   % "7.6.0",
    "uk.gov.hmrc"       %% s"bootstrap-frontend$playSuffix"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playSuffix"                    % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                                 % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"                       % "1.15.4",
    "org.mockito"            %% "mockito-scala-scalatest"     % "1.17.30",
    "org.scalatestplus"      %% "scalacheck-1-17"             % "3.2.17.0",
    "org.scalatestplus.play" %% "scalatestplus-play"          % "7.0.0",
    "uk.gov.hmrc"            %% s"bootstrap-test$playSuffix"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test$playSuffix" % hmrcMongoVersion,
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"       % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}