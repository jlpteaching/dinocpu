// Lab 4 tester

package dinocpu.test.grader

import dinocpu._
import dinocpu.test._

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith


@RunWith(classOf[GradedTestRunnerJSON])
class Lab4Grader extends JUnitSuite {

  @Test
  @GradedTest(name="Small Tests", max_score=5)
  def verifySmallTests() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""

      for ((group, tests) <- InstTests.tests) {
        for (test <- tests) {
          val this_test_success = CPUTesterDriver(test, "pipelined-dual-issue")
          success = this_test_success && success
          if (!this_test_success) {
            error = "Errored on test " + test.name() + "\n"
          }
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail(error)
    }
  }

  @Test
  @GradedTest(name="Dual Issue Forwarding Tests", max_score=5)
  def verifyDualIssueForwarding() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.dualIssue) {
        val this_test_success = CPUTesterDriver(test, "pipelined-dual-issue")
        success = this_test_success && success
        if (!this_test_success) {
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
  @GradedTest(name="Full Applications", max_score=5)
  def verifyFullApplications() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.fullApplications) {
        val this_test_success = CPUTesterDriver(test, "pipelined-dual-issue")
        success = this_test_success && success
        if (!this_test_success) {
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
  @GradedTest(name="Loops Unrolled Full Applications", max_score=5)
  def verifyLoopsUnrolledFullApplications() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      var error = ""
      for (test <- InstTests.loopsUnrolledFullApplications) {
        val this_test_success = CPUTesterDriver(test, "pipelined-dual-issue")
        success = this_test_success && success
        if (!this_test_success) {
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
