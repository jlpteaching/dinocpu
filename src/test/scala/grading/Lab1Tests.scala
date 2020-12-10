// Lab 1 tester

package dinocpu.test.grader

import dinocpu._
import dinocpu.components._
import dinocpu.test._
import dinocpu.test.components._
import chisel3.iotesters.Driver

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith


@RunWith(classOf[GradedTestRunnerJSON])
class Lab1Grader extends JUnitSuite {

  @Test
  @GradedTest(name="ALUControlUnit", max_score=30)
  def verifyALUControl() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      val success = Driver(() => new ALUControl) {
        c => new ALUControlUnitRTypeTester(c)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Add instruction add1", max_score=10)
  def verifyAdd() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = CPUTesterDriver(CPUTestCase("add1",
                      Map("single-cycle" -> 1),
                      Map(5 -> 1234),
                      Map(0 -> 0, 5 -> 1234, 6 -> 1234),
                      Map(), Map()),
               "single-cycle")

      success = CPUTesterDriver(CPUTestCase("add2",
                      Map("single-cycle" -> 1),
                      Map(5 -> 1234, 20 -> 5678),
								      Map(0 -> 0, 10 -> 6912),
                      Map(), Map()),
               "single-cycle") && success

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Add instruction add0", max_score=5)
  def verifyAdd0() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      val test = InstTests.nameMap("add0")

      success = CPUTesterDriver(test, "single-cycle")

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="All R types", max_score=15)
  def verifyRType() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.rtype) {
        if (test.binary != "add0" && test.binary != "add1" && test.binary != "add2") {
          success = CPUTesterDriver(test, "single-cycle") && success
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Multiple cycle R types", max_score=10)
  def verifyMultiCycle() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.rtypeMultiCycle) {
        success = CPUTesterDriver(test, "single-cycle") && success
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }
}
