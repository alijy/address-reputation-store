import sbt._

lazy val library = Project("address-reputation-store", file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(majorVersion := 2)
  .settings(
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12", "2.12.12"),
    parallelExecution in Test := false,
    makePublicallyAvailableOnBintray := true,
    libraryDependencies ++= Seq(
      "uk.gov.hmrc"                   %% "logging"                % "0.7.0" withSources(),
      "com.univocity"                 % "univocity-parsers"       % "1.5.6" withSources(),
      "com.sksamuel.elastic4s"        %% "elastic4s-core"         % "2.4.1",
      "com.fasterxml.jackson.core"    % "jackson-core"            % "2.11.1",
      "com.fasterxml.jackson.core"    % "jackson-databind"        % "2.11.1",
      "com.fasterxml.jackson.core"    % "jackson-annotations"     % "2.11.1",
      "com.fasterxml.jackson.module"  %% "jackson-module-scala"   % "2.11.1",
      "ch.qos.logback"                % "logback-classic"         % "1.2.3"                 % Runtime,
      "org.scalatest"                 %% "scalatest"              % "3.0.8"                 % "test",
      "org.scalacheck"                %% "scalacheck"             % "1.14.3"                % "test",
      "org.pegdown"                   % "pegdown"                 % "1.6.0"                 % "test",
      "org.mockito"                   % "mockito-all"             % "1.10.19"               % "test"
    )
  )
