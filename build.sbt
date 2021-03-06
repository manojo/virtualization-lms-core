/*// --- project info ---

name := "lms-core"

organization := "org.scala-lang.lms"

description := "Lightweight Modular Staging"

homepage := Some(url("https://scala-lms.github.io"))

licenses := List("BSD-like" -> url("https://github.com/TiarkRompf/virtualization-lms-core/blob/develop/LICENSE"))

scmInfo := Some(ScmInfo(url("https://github.com/TiarkRompf/virtualization-lms-core"), "git@github.com:TiarkRompf/virtualization-lms-core.git"))

// developers := List(Developer("tiarkrompf", "Tiark Rompf", "@tiarkrompf", url("http://github.com/tiarkrompf")))


// --- scala settings ---

scalaVersion := virtScala

scalaOrganization := "org.scala-lang.virtualized"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test-src")

scalacOptions += "-Yvirtualize"

//scalacOptions += "-Yvirtpatmat"

//scalacOptions in Compile ++= Seq(/*Unchecked, */Deprecation)


// --- dependencies ---

libraryDependencies += ("org.scala-lang.virtualized" % "scala-library" % virtScala)

// Transitive dependency through scala-continuations-library
libraryDependencies += ("org.scala-lang.virtualized" % "scala-compiler" % virtScala).
  exclude ("org.scala-lang", "scala-library").
  exclude ("org.scala-lang", "scala-compiler")

libraryDependencies += ("org.scala-lang.plugins" % "scala-continuations-library_2.11" % "1.0.2").
  exclude ("org.scala-lang", "scala-library").
  exclude ("org.scala-lang", "scala-compiler")

libraryDependencies += ("org.scalatest" % "scalatest_2.11" % "2.2.2").
  exclude ("org.scala-lang", "scala-library").
  exclude ("org.scala-lang", "scala-compiler").
  exclude ("org.scala-lang", "scala-reflect")

// continuations
autoCompilerPlugins := true

addCompilerPlugin("org.scala-lang.plugins" % "scala-continuations-plugin_2.11.2" % "1.0.2")

scalacOptions += "-P:continuations:enable"


// --- testing ---

// tests are not thread safe
parallelExecution in Test := false

// code coverage
scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := false
*/

/**
 * re-organized as per the new recommendations of sbt 0.13
 */
lazy val commonSettings = Seq(
  organization := "com.github.manojo",
  version := "0.1-SNAPSHOT",

  scalaVersion := "2.11.2",
  scalaOrganization := "org.scala-lang.virtualized",
  scalacOptions ++= Seq(
    "-Yvirtualize",
    //"-optimize",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions"
    //"-Yinline-warnings"
  )
) ++ publishSettings ++ publishableSettings

//implicit logging
//scalacOptions in ThisBuild += "-Xlog-implicits"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "lms",

    scalaSource in Compile := baseDirectory.value / "src",
    scalaSource in Test := baseDirectory.value / "test-src",

    libraryDependencies ++= Seq(
      "org.scala-lang.virtualized" % "scala-library" % virtScala,

      ("org.scala-lang.virtualized" % "scala-compiler" % virtScala).
        exclude ("org.scala-lang", "scala-library").
        exclude ("org.scala-lang", "scala-compiler"),

      ("org.scala-lang.plugins" % "scala-continuations-library_2.11" % "1.0.2").
        exclude ("org.scala-lang", "scala-library").
        exclude ("org.scala-lang", "scala-compiler"),

      ("org.scalatest" % "scalatest_2.11" % "2.2.2").
        exclude ("org.scala-lang", "scala-library").
        exclude ("org.scala-lang", "scala-compiler").
        exclude ("org.scala-lang", "scala-reflect")
    ),

    resolvers += Resolver.sonatypeRepo("snapshots"),
    autoCompilerPlugins := true,
    addCompilerPlugin("org.scala-lang.plugins" % "scala-continuations-plugin_2.11.2" % "1.0.2"),
    scalacOptions += "-P:continuations:enable",
    /**
     * tests are not thread safe
     * this applies to all lms tests that write
     * to a file, and do diff tests
     */
    parallelExecution in Test := false
  )

/**
 * We are able to publish this thing!
 */
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishOnlyWhenOnMaster := publishOnlyWhenOnMasterImpl.value,
  publishTo <<= version { v: String =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { x => false },
  publishArtifact in Compile := false,
  publishArtifact in Test := false,
  pomExtra := (
    <url>https://github.com/manojo/virtualization-lms-core</url>
    <inceptionYear>2015</inceptionYear>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://github.com/manojo/staged-fold-fusion/blob/master/LICENSE</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git:github.com/manojo/staged-fold-fusion.git</url>
      <connection>scm:git:git://github.com:manojo/virtualization-lms-core.git</connection>
    </scm>
//    <issueManagement>
//      <system>GitHub</system>
//      <url>https://github.com/manojo/staged-fold-fusion/issues</url>
//    </issueManagement>
  ),
  publishArtifact in (Compile, packageDoc) := false
)

lazy val publishOnlyWhenOnMaster = taskKey[Unit](
  "publish task for Travis (don't publish when building pull requests, only publish" +
  "when the build is triggered by merge into master)")

def publishOnlyWhenOnMasterImpl = Def.taskDyn {
  import scala.util.Try
  val travis   = Try(sys.env("TRAVIS")).getOrElse("false") == "true"
  val pr       = Try(sys.env("TRAVIS_PULL_REQUEST")).getOrElse("false") != "false"
  val branch   = Try(sys.env("TRAVIS_BRANCH")).getOrElse("??")
  val snapshot = version.value.trim.endsWith("SNAPSHOT")
  (travis, pr, branch, snapshot) match {
    case (true, false, "develop", true) => publish
    case _                             => Def.task ()
  }
}

lazy val publishableSettings = Seq(
  publishArtifact in Compile := true,
  publishArtifact in Test := false,
  credentials ++= {
    val mavenSettingsFile = System.getenv("MAVEN_SETTINGS_FILE")
    if (mavenSettingsFile != null) {
      println("Loading Sonatype credentials from " + mavenSettingsFile)
      try {
        import scala.xml._
        val settings = XML.loadFile(mavenSettingsFile)
        def readServerConfig(key: String) = (settings \\ "settings" \\ "servers" \\ "server" \\ key).head.text
        Some(Credentials(
          "Sonatype Nexus Repository Manager",
          "oss.sonatype.org",
          readServerConfig("username"),
          readServerConfig("password")
        ))
      } catch {
        case ex: Exception =>
          println("Failed to load Maven settings from " + mavenSettingsFile + ": " + ex)
          None
      }
    } else {
      for {
        realm <- sys.env.get("SCALAMETA_MAVEN_REALM")
        domain <- sys.env.get("SCALAMETA_MAVEN_DOMAIN")
        user <- sys.env.get("SCALAMETA_MAVEN_USER")
        password <- sys.env.get("SCALAMETA_MAVEN_PASSWORD")
      } yield {
        println("Loading Sonatype credentials from environment variables")
        Credentials(realm, domain, user, password)
      }
    }
  }.toList
)
