// Lab 3 tester

package dinocpu.test.grader

import dinocpu._
import dinocpu.test._

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith


@RunWith(classOf[GradedTestRunnerJSON])
class Lab3Grader extends JUnitSuite {

  @Test
  @GradedTest(name="R-type instructions", max_score=13)
  def verifyRtype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.rtype) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="I-type instructions", max_score=13)
  def verifyItype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.itype) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="U-type instructions", max_score=12)
  def verifyUtype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.utype) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Memory instructions", max_score=12)
  def verifyMemorytype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.memory) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="I-type multi cycle", max_score=5)
  def verifyItypeMulti() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.itypeMultiCycle) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="R-type multi cycle", max_score=5)
  def verifyRtypeMulti() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.rtypeMultiCycle) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Branch instructions", max_score=5)
  def verifyBranch() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.branch) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Jump instructions", max_score=5)
  def verifyJump() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.jump) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Memory multi cycle", max_score=5)
  def verifyMemoryMulti() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.memoryMultiCycle) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Applications", max_score=15)
  def verifyApplications() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.smallApplications) {
        success = CPUTesterDriver(test, "pipelined") && success
        if (!success) {
          error = "Errored on test " + test.name() + "\n"
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }
}
