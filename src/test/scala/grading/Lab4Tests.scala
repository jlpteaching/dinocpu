package dinocpu

import chisel3.iotesters.Driver

import com.gradescope.jh61b.grader.{GradedTest,GradedTestRunnerJSON}
import org.junit.Test;
import org.scalatest.junit.JUnitSuite
import org.junit.runner.RunWith

@RunWith(classOf[GradedTestRunnerJSON])
class Lab4Grader extends JUnitSuite {

  @Test
  @GradedTest(name="Local unit test 2-bit", max_score=2)
  def verifyLocal2bit() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val stream = List(
        /* pc, taken, prediction */
        (0x0,  true, true),
        (0x0,  true, true),
        (0x0,  false, true),
        (0x0,  false, true),
        (0x0,  false, false),
        (0x0,  false, false),
        (0x0,  false, false),
        (0x0,  false, false),
        (0x0,  true, false),
        (0x0,  true, false),
        (0x0,  true, true)
        )
      implicit val conf = new CPUConfig()
      conf.branchPredictor = "local"
      conf.saturatingCounterBits = 2

      val success = Driver(() => new LocalPredictor) {
        p => new LocalPredictorUnitTester(p, stream)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Local unit test 3-bit", max_score=2)
  def verifyLocal3bit() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val stream = List(
        /* pc, taken, prediction */
        (0x0,  true, true), // 101
        (0x0,  true, true), // 111
        (0x0,  true, true), // 111
        (0x0,  false, true), // 110
        (0x0,  false, true), // 101
        (0x0,  false, true), // 100
        (0x0,  false, true), // 011 (this is the value after this cycle)
        (0x0,  false, false), // 010
        (0x0,  false, false), // 001
        (0x0,  true, false),  // 010
        (0x0,  false, false), // 001
        (0x0,  false, false), // 000
        (0x0,  true, false),  // 001
        (0x0,  true, false), // 010
        (0x0,  true, false), // 011
        (0x0,  true, false), // 100
        (0x0,  false, true)
      )
      implicit val conf = new CPUConfig()
      conf.branchPredictor = "local"
      conf.saturatingCounterBits = 3

      val success = Driver(() => new LocalPredictor) {
        p => new LocalPredictorUnitTester(p, stream)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Local unit test multiple", max_score=2)
  def verifyLocalMultiple() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val stream = List(
        (0x0,  true, true), (0x0,  false, true), (0x0,  false, true), (0x0,  false, false), (0x0,  false, false), (0x0,  false, false), (0x0,  false, false), (0x0,  true, false), (0x0,  true, false), (0x0,  true, true),
        (0x4,  true, true), (0x4,  false, true), (0x4,  false, true), (0x4,  false, false), (0x4,  false, false), (0x4,  false, false), (0x4,  false, false), (0x4,  true, false), (0x4,  true, false), (0x4,  true, true),
        (0x8,  true, true), (0x8,  false, true), (0x8,  false, true), (0x8,  false, false), (0x8,  false, false), (0x8,  false, false), (0x8,  false, false), (0x8,  true, false), (0x8,  true, false), (0x8,  true, true),
        (0xc,  true, true), (0xc,  false, true), (0xc,  false, true), (0xc,  false, false), (0xc,  false, false), (0xc,  false, false), (0xc,  false, false), (0xc,  true, false), (0xc,  true, false), (0xc,  true, true),
        (0x10,  true, true), (0x10,  false, true), (0x10,  false, true), (0x10,  false, false), (0x10,  false, false), (0x10,  false, false), (0x10,  false, false), (0x10,  true, false), (0x10,  true, false), (0x10,  true, true)
      )
      implicit val conf = new CPUConfig()
      conf.branchPredictor = "local"
      conf.saturatingCounterBits = 2
      conf.branchPredTableEntries = 8

      val success = Driver(() => new LocalPredictor) {
        p => new LocalPredictorUnitTester(p, stream)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Local unit test aliased", max_score=2)
  def verifyLocalAlias() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val stream = List(
        (0x0,  true, true), (0x8,  false, true), (0x0,  false, true), (0x8,  false, false), (0x0,  false, false), (0x8,  false, false), (0x0,  false, false), (0x8,  true, false), (0x0,  true, false), (0x8,  true, true),
        (0x4,  true, true), (0xc,  false, true), (0x4,  false, true), (0xc,  false, false), (0x4,  false, false), (0xc,  false, false), (0x4,  false, false), (0xc,  true, false), (0x4,  true, false), (0xc,  true, true),
      )
      implicit val conf = new CPUConfig()
      conf.branchPredictor = "local"
      conf.saturatingCounterBits = 2
      conf.branchPredTableEntries = 2

      val success = Driver(() => new LocalPredictor) {
        p => new LocalPredictorUnitTester(p, stream)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Global unit test directed", max_score=2)
  def verifyGlobalDirected() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      val stream = List(
        /* pc, taken, prediction */
        (0x0,  true, true),  // 00: 11
        (0x0,  true, true), // 01: 11
        (0x0,  false, true), // 11: 01
        (0x0,  true, true), // 10: 11
        (0x0,  true, true), // 01: 11
        (0x0,  false, false), // 11: 00
        (0x0,  true, true), // 10: 11
        (0x0,  true, true), // 01: 11
        (0x0,  true, false),  // 11: 01
        (0x0,  true, false), // 11
        (0x0,  true, true), // 11
        (0x0,  true, true),  // 11: 11
        (0x0,  true, true),
        (0x0,  true, true),
        (0x0,  true, true),  // 11: 11
        (0x0,  true, true), // 11: 10
        (0x0,  true, true), // 11: 11
        (0x0,  false, true)  // 11: 10
      )
      implicit val conf = new CPUConfig()
      conf.branchPredictor = "global"
      conf.saturatingCounterBits = 2
      conf.branchPredTableEntries = 4

      val success = Driver(() => new GlobalHistoryPredictor) {
        p => new GlobalPredictorUnitTester(p, stream)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Global unit test random", max_score=3)
  def verifyGlobalRandom() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()
      conf.branchPredictor = "global"
      conf.saturatingCounterBits = 2
      conf.branchPredTableEntries = 8

      val success = Driver(() => new GlobalHistoryPredictor) {
        p => new GlobalPredictorRandomUnitTester(p, 8, 2)
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed")
    }
  }

  @Test
  @GradedTest(name="Global small apps", max_score=2)
  def verifyGlobalSmall() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.smallApplications) {
        success = CPUTesterDriver(test, "pipelined", "global") && success
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Global full apps", max_score=3)
  def verifyGlobalFull() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.fullApplications) {
        success = CPUTesterDriver(test, "pipelined", "global") && success
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Local small apps", max_score=1)
  def verifyLocalSmall() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.smallApplications) {
        success = CPUTesterDriver(test, "pipelined", "local") && success
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }

  @Test
  @GradedTest(name="Local full apps", max_score=1)
  def verifyLocalFull() {
    // Capture all of the console output from the test
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {

      implicit val conf = new CPUConfig()

      var success = true
      for (test <- InstTests.fullApplications) {
        success = CPUTesterDriver(test, "pipelined", "local") && success
      }

      // Dump the output of the driver above onto the system out so that the
      // gradescope function will catch it.
      System.out.print(stream)
      if (!success) fail("Test failed!")
    }
  }




}
