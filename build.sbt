def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "dachr-codcpu"

version := "3.1.0"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel3" -> "3.2-SNAPSHOT",
  "chisel-iotesters" -> "1.2.+"
  )

libraryDependencies ++= (Seq("chisel3","chisel-iotesters").map {
  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) })

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)

// https://mvnrepository.com/artifact/junit/junit
// For running the gradescope tests
libraryDependencies += "junit" % "junit" % "4.12" % Test

// This sets it up so all tests that end in "Tester" will be run when you run sbt test
// and all tests that end in "Grader" will run when you run stb Grader / test
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
lazy val Grader = config("grader") extend(Test)

def graderFilter(name: String): Boolean = name endsWith "Grader"
def unitFilter(name: String): Boolean = (name endsWith "Tester") && !graderFilter(name)

lazy val root = (project in file("."))
  .configs(Grader)
  .settings(
    inConfig(Grader)(Defaults.testTasks),
    libraryDependencies += scalatest % Grader,
    testOptions in Test := Seq(Tests.Filter(unitFilter)),
    testOptions in Grader := Seq(Tests.Filter(graderFilter))
  )
