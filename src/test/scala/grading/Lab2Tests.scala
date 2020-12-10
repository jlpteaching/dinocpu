// Lab 2 tester

package dinocpu.test.grader

import dinocpu._
import dinocpu.test._

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith


@RunWith(classOf[GradedTestRunnerJSON])
class Lab2Grader extends JUnitSuite {

  val maxInt = BigInt("FFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  @Test
  @GradedTest(name="R-type instructions", max_score=10)
  def verifyRtype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.rtype) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="I-type instructions", max_score=10)
  def verifyItype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
      Console.withOut(stream) {

        val tests = List[CPUTestCase](CPUTestCase("add1",
                      Map("single-cycle" -> 1),
                      Map(5 -> 1234),
                      Map(0 -> 0, 5 -> 1234, 6 -> 1234),
                      Map(), Map()),
          CPUTestCase("addi1",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(),
                      Map(0 -> 0, 10 -> 17),
                      Map(), Map()),
          CPUTestCase("addi2",
                      Map("single-cycle" -> 2, "five-cycle" -> 0, "pipelined" -> 6),
                      Map(),
                      Map(0 -> 0, 10 -> 17, 11 -> 93),
                      Map(), Map()),
          CPUTestCase("slli",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 1),
                      Map(0 -> 0, 5 -> 1, 6 -> 128),
                      Map(), Map()),
          CPUTestCase("srai",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 1024),
                      Map(0 -> 0, 5 -> 1024, 6 -> 8),
                      Map(), Map()),
          CPUTestCase("srai",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> twoscomp(-1024)),
                      Map(0 -> 0, 5 -> twoscomp(-1024), 6 -> twoscomp(-8)),
                      Map(), Map(), "-negative"),
          CPUTestCase("srli",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 128),
                      Map(0 -> 0, 5 -> 128, 6 -> 1),
                      Map(), Map()),
          CPUTestCase("andi",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 456),
                      Map(0 -> 0, 5 -> 456, 6 -> 200),
                      Map(), Map()),
          CPUTestCase("ori",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 456),
                      Map(0 -> 0, 5 -> 456, 6 -> 511),
                      Map(), Map()),
          CPUTestCase("xori",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> 456),
                      Map(0 -> 0, 5 -> 456, 6 -> 311),
                      Map(), Map()),
          CPUTestCase("slti",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> twoscomp(-1)),
                      Map(0 -> 0, 5 -> twoscomp(-1),6->1),
                      Map(), Map()),
          CPUTestCase("sltiu",
                      Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                      Map(5 -> twoscomp(-1)),
                      Map(0 -> 0, 5 -> twoscomp(-1), 6 -> 0),
                      Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }



  @Test
  @GradedTest(name="Load instructions", max_score=10)
  def verifyLoads() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("lw1",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("ffffffff", 16)),
                    Map(), Map()),
        CPUTestCase("lwfwd",
                    Map("single-cycle" -> 2, "five-cycle" -> 0, "pipelined" -> 7),
                    Map(5 -> BigInt("ffffffff", 16), 10 -> 5),
                    Map(5 -> 1, 10 -> 6),
                    Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="U-type instructions", max_score=10)
  def verifyUtype() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("auipc0",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(10 -> 1234),
                    Map(10 -> 0),
                    Map(), Map()),
        CPUTestCase("auipc1",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(10 -> 1234),
                    Map(10 -> 4),
                    Map(), Map()),
        CPUTestCase("auipc2",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(10 -> 1234),
                    Map(10 -> (17 << 12)),
                    Map(), Map()),
        CPUTestCase("auipc3",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(10 -> 1234),
                    Map(10 -> ((17 << 12) + 4)),
                    Map(), Map()),
        CPUTestCase("lui0",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(10 -> 1234),
                    Map(10 -> 0),
                    Map(), Map()),
        CPUTestCase("lui1",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(10 -> 1234),
                    Map(10 -> 4096),
                    Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }




  @Test
  @GradedTest(name="store instructions", max_score=10)
  def verifyStore() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("sw",
                Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                Map(5 -> 1234),
								Map(6 -> 1234),
								Map(), Map(0x100 -> 1234))
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="All mem instructions", max_score=10)
  def verifyMemInsts() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("lb",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("04", 16)),
                    Map(), Map()),
        CPUTestCase("lh",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("0304", 16)),
                    Map(), Map()),
        CPUTestCase("lbu",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("f4", 16)),
                    Map(), Map()),
        CPUTestCase("lhu",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("f3f4", 16)),
                    Map(), Map()),
        CPUTestCase("lb1",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("fffffff4", 16)),
                    Map(), Map()),
        CPUTestCase("lh1",
                    Map("single-cycle" -> 1, "five-cycle" -> 5, "pipelined" -> 5),
                    Map(),
                    Map(5 -> BigInt("fffff3f4", 16)),
                    Map(), Map()),
        CPUTestCase("sb",
                    Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                    Map(5 -> 1),
                    Map(6 -> 1),
                    Map(), Map(0x100 -> BigInt("ffffff01", 16))),
        CPUTestCase("sh",
                    Map("single-cycle" -> 6, "five-cycle" -> 10, "pipelined" -> 10),
                    Map(5 -> 1),
                    Map(6 -> 1),
                    Map(), Map(0x100 -> BigInt("ffff0001", 16)))
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Branch instructions", max_score=10)
  def verifyBranches() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.branch) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }



  @Test
  @GradedTest(name="Jump and link instruction", max_score=10)
  def verifyJal() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("jal",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(5 -> 1234),
                    Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
                    Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }




  @Test
  @GradedTest(name="Jump and link register instruction", max_score=10)
  def verifyJalr() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("jalr0",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(5 -> 1234, 10 -> 28),
                    Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
                    Map(), Map()),
        CPUTestCase("jalr1",
                    Map("single-cycle" -> 2, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(5 -> 1234, 10 -> 20),
                    Map(0 -> 0, 5 -> 1234, 6 -> 1234, 1 -> 4),
                    Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }





  @Test
  @GradedTest(name="Full applications", max_score=10)
  def verifyApps() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val tests = List[CPUTestCase](
        CPUTestCase("fibonacci",
                    Map("single-cycle" -> 300, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(6->11),
                    Map(6->11,5->89),
                    Map(), Map()),
        CPUTestCase("naturalsum",
                    Map("single-cycle" -> 200, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(),
                    Map(5->55),
                    Map(), Map()),
        CPUTestCase("multiplier",
                Map("single-cycle" -> 1000, "five-cycle" -> 6, "pipelined" -> 6),
              Map(5->23,6->20,8->0x1000),
                    Map(5->23*20),
                    Map(), Map()),
        CPUTestCase("divider",
                    Map("single-cycle" -> 1000, "five-cycle" -> 6, "pipelined" -> 6),
                    Map(5->1260,6->30),
                    Map(7->42),
                    Map(), Map())
      )

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- tests) {
        success = CPUTesterDriver(test, "single-cycle") && success
        if (!success) {
          println("Errored on test " + test.binary)
        }
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

}
