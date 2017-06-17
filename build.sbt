import Dependencies._
import sbt.Keys._
import sbt._

lazy val buildSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "org.mellowtech",
  scalaVersion := "2.12.1",
  libraryDependencies ++= commonAPIs,
  publishArtifact in Test := false,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/site/test-reports")
)


lazy val common = (project in file("common")).
  settings(buildSettings: _*).
  settings(
    name := "gapi-common",
    libraryDependencies ++= testDeps,
    libraryDependencies += typesafeConf,
    libraryDependencies ++= googleAPIs,
    libraryDependencies ++= slickDeps,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.1",
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )

lazy val akka = (project in file("akka")).
  settings(buildSettings: _*).
  settings(
    name := "gapi-server",
    libraryDependencies ++= testDeps,
    libraryDependencies ++= googleAPIs,
    libraryDependencies ++= akkaAPIs,
    libraryDependencies ++= jsonDeps,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  ).dependsOn(common)

lazy val serverexample = (project in file("serverexample")).
  settings(buildSettings: _*).
  settings(
    name := "gapi-serverexample",
    libraryDependencies ++= testDeps,
    libraryDependencies ++= googleAPIs,
    libraryDependencies ++= akkaAPIs,
    libraryDependencies ++= jsonDeps,
    libraryDependencies += "org.postgresql" % "postgresql" % "42.1.1",
    libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.5",
      publish := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    javaOptions in reStart += "-Dconfig.file=/Users/msvens/conf/serverexample.conf"
  ).dependsOn(common,akka, drive)

//Single Project Config
lazy val drive = (project in file("drive")).
  settings(buildSettings: _*).
  settings(
    name := "gapi-drive",
    libraryDependencies ++= testDeps,
    libraryDependencies ++= googleAPIs,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  ).dependsOn(common)



lazy val root = (project in file(".")).aggregate(common,akka,drive).
  settings(buildSettings: _*).
  settings(
    publish := false
  )
