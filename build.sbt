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

name := "dinocpu"
version := "0.5"
organization := "edu.ucdavis.cs"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.12.10", "2.11.12")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel3" -> "3.2.+",
  "chisel-iotesters" -> "1.3.+"
  )

libraryDependencies ++= Seq("chisel3","chisel-iotesters").map {
  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) }

libraryDependencies ++= Seq("org.jline" % "jline" % "3.5.1")

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)

// https://mvnrepository.com/artifact/junit/junit
// For running the gradescope tests
libraryDependencies += "junit" % "junit" % "4.12" % Test

// https://mvnrepository.com/artifact/net.fornwall.jelf/jelf
// For understanding elfs and directly running binary files
libraryDependencies += "net.fornwall.jelf" % "jelf" % "0.4.1"

// For visualization, figure out how to move this somewhere else
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"

// This sets it up so all tests that end in "Tester" will be run when you run sbt test
// and all tests that end in "Grader" will run when you run stb Grader / test
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
lazy val Grader = config("grader") extend(Test)
lazy val TestAll = config("testAll") extend(Test)
lazy val Lab1 = config("lab1") extend(Test)
lazy val Lab2 = config("lab2") extend(Test)
lazy val Lab3 = config("lab3") extend(Test)
lazy val Lab4 = config("lab4") extend(Test)

def allFilter(name: String): Boolean = name endsWith "Tester"
def graderFilter(name: String): Boolean = name endsWith "Grader"
def lab1Filter(name: String): Boolean = name endsWith "TesterLab1"
def lab2Filter(name: String): Boolean = name endsWith "TesterLab2"
def lab3Filter(name: String): Boolean = name endsWith "TesterLab3"
def lab4Filter(name: String): Boolean = name endsWith "TesterLab4"

lazy val root = (project in file("."))
  .configs(TestAll).configs(Grader).configs(Lab1).configs(Lab2).configs(Lab3).configs(Lab4)
  .settings(
    inConfig(Grader)(Defaults.testTasks),
    inConfig(TestAll)(Defaults.testTasks),
    inConfig(Lab1)(Defaults.testTasks),
    inConfig(Lab2)(Defaults.testTasks),
    inConfig(Lab3)(Defaults.testTasks),
    inConfig(Lab4)(Defaults.testTasks),
    libraryDependencies += scalatest % TestAll,
    libraryDependencies += scalatest % Grader,
    libraryDependencies += scalatest % Lab1,
    libraryDependencies += scalatest % Lab2,
    libraryDependencies += scalatest % Lab3,
    libraryDependencies += scalatest % Lab4,
    testOptions in TestAll := Seq(Tests.Filter(allFilter)),
    // CHANGE THE LINE BELOW FOR EACH LAB!!!! Use the matching filter
    testOptions in Test := Seq(Tests.Filter(allFilter)),
    testOptions in Grader := Seq(Tests.Filter(graderFilter)),
    testOptions in Lab1 := Seq(Tests.Filter(lab1Filter)),
    testOptions in Lab2 := Seq(Tests.Filter(lab2Filter)),
    testOptions in Lab3 := Seq(Tests.Filter(lab3Filter)),
    testOptions in Lab4 := Seq(Tests.Filter(lab4Filter))
  )
