ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "ru.stavegan.sorting"
ThisBuild / organizationName := "stavegan"

lazy val catsVersion = "2.8.0"
lazy val catsEffectVersion = "3.3.13"
lazy val zioVersion = "2.0.0"
lazy val zioInteropCatsVersion = "3.3.0"
lazy val scalafmtVersion = "2.4.6"

lazy val root = (project in file("."))
  .settings(
    name := "sorting",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"          % catsVersion,
      "org.typelevel" %% "cats-effect"        % catsEffectVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
      "dev.zio"       %% "zio"                % zioVersion,
      "dev.zio"       %% "zio-test"           % zioVersion,
      "dev.zio"       %% "zio-interop-cats"   % zioInteropCatsVersion
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
