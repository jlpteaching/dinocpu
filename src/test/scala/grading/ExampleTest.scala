package CODCPU

import chisel3.iotesters.Driver

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test;
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith

@RunWith(classOf[GradedTestRunnerJSON])
class RegisterFileGrader extends JUnitSuite {

  @Test
  @GradedTest(name="RegisterFileUnitTest", max_score=10)
  def verify() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()
      val success = Driver(() => new RegisterFile) {
          c => new RegisterFileUnitTester(c)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }
}
