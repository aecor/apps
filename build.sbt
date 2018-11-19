//import ReleaseTransformations._
//import sbtrelease.Version.Bump
import pl.project13.scala.sbt._
import Dependencies.versions

lazy val buildSettings = inThisBuild(
  Seq(
    organization := "io.aecor",
    scalaVersion := "2.12.4"
  )
)

//val ivyLocal = Resolver.file("local", file("/development/projects/02_arch/aecor" + "/.ivy2/local"))(Resolver.ivyStylePatterns)

lazy val commonSettings = Seq(

  resolvers += "jitpack" at "https://jitpack.io",

  scalacOptions ++= commonScalacOptions,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % versions.kindProjector),
  parallelExecution in Test := false,
  scalacOptions in (Compile, doc) := (scalacOptions in (Compile, doc)).value
    .filter(_ != "-Xfatal-warnings")
) ++ warnUnusedImport

lazy val aecorSettings = buildSettings ++ commonSettings ++ publishSettings

lazy val aecorApps = project
  .in(file("."))
  .withId("aecor-apps")
  .settings(moduleName := "aecorApps", name := "AecorApps")
  .settings(aecorSettings)
  .settings(noPublishSettings)
  .aggregate(
    examples
  )

def aecorModule(id: String, description: String): Project =
  Project(id, file(s"modules/$id"))
    .settings(moduleName := id, name := description)

lazy val examples = aecorModule("examples", "Aecor Examples Applications")
  //.dependsOn(core, schedule, distributedProcessing, boopickleWireProtocol)
  .settings(aecorSettings)
  .settings(coreSettings)
  .settings(noPublishSettings)
  .settings(examplesSettings)

lazy val coreSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-tagless-macros" % versions.catsTagless,
    "com.chuusai" %% "shapeless" % versions.shapeless,
    "org.typelevel" %% "cats-core" % versions.cats,
    "org.typelevel" %% "cats-effect" % versions.catsEffect,
    "org.scodec" %% "scodec-bits" % versions.scodec_bits,
    "org.scodec" %% "scodec-core" % versions.scodec,
    "io.aecor" %% "core" % versions.aecor,
    "io.aecor" %% "schedule" % versions.aecor,
    "io.aecor" %% "distributed-processing" % versions.aecor,
    "io.aecor" %% "akka-cluster-runtime" % versions.aecor,
    "io.aecor" %% "akka-persistence-runtime" % versions.aecor,
    "io.aecor" %% "boopickle-wire-protocol" % versions.aecor
  )
)

lazy val examplesSettings = {
  Seq(
    addCompilerPlugin(
      "org.scalameta" % "paradise" % versions.scalametaParadise cross CrossVersion.patch
    ),
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += Resolver.file("aecor-local", file("/development/projects/02_arch/aecor" + "/.ivy2/local"))(Resolver.ivyStylePatterns),
    resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven",

    libraryDependencies ++=
      Seq(
        "com.github.krasserm" %% "streamz-converter" % "0.10-M1",
        "co.fs2" %% "fs2-core" % "1.0.0",
        "org.typelevel" %% "cats-mtl-core" % "0.4.0",
        "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
        "org.http4s" %% "http4s-dsl" % versions.http4s,
        "org.http4s" %% "http4s-blaze-server" % versions.http4s,
        "org.http4s" %% "http4s-circe" % versions.http4s,
        "io.circe" %% "circe-core" % versions.circe,
        "io.circe" %% "circe-generic" % versions.circe,
        "io.circe" %% "circe-parser" % versions.circe,
        "io.circe" %% "circe-java8" % versions.circe,
        "ch.qos.logback" % "logback-classic" % versions.logback
      )
  )
}

lazy val testKitSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-mtl-core" % "0.4.0",
    "com.github.julien-truffaut" %% "monocle-core" % versions.monocle,
    "com.github.julien-truffaut" %% "monocle-macro" % versions.monocle
  )
)


lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-Ypartial-unification",
  "-Xsource:2.13"
)

lazy val warnUnusedImport = Seq(
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
)

lazy val noPublishSettings = Seq(publish := (()), publishLocal := (()), publishArtifact := false)

lazy val publishSettings = Seq(
  //releaseCrossBuild := true,
  //releaseVersionBump := Bump.Minor,
  //releaseCommitMessage := s"Set version to ${if (releaseUseGlobalVersion.value) (version in ThisBuild).value
  //else version.value}",
  //releaseIgnoreUntrackedFiles := true,
  //releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/notxcain/aecor")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(url("https://github.com/notxcain/aecor"), "scm:git:git@github.com:notxcain/aecor.git")
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>notxcain</id>
        <name>Denis Mikhaylov</name>
        <url>https://github.com/notxcain</url>
      </developer>
    </developers>
)

addCommandAlias("validate", ";compile;test")
