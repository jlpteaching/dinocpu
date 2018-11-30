package CODCPU

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import com.gradescope.jh61b.grader.GradedTest
import org.junit.Test;
import org.scalatest.junit.{JUnitSuite}

class RegisterFileUnitTester2(c: RegisterFile) extends PeekPokeTester(c) {
  private val rf = c

  // Write some data to registers
  for (i <- 0 to 31) {
    poke(rf.io.writereg, i)
    poke(rf.io.writedata, i+100)
    poke(rf.io.wen, true)
    step(1)
  }

  for (i <- 0 to 31 by 2) {
    println("Checking")
    poke(rf.io.readreg1, i)
    poke(rf.io.readreg2, i+1)
    poke(rf.io.wen, false)
    expect(rf.io.readdata1, i+100)
    expect(rf.io.readdata2, i+105)
    step(1)
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly CODCPU.RegisterFileTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly CODCPU.RegisterFileTester'
  * }}}
  */
class RegisterFileTesterGraded extends JUnitSuite {
    @Test
    @GradedTest(name="This is a name", max_score=10)
    def verify() {
        implicit val conf = new CPUConfig()
        val success = Driver(() => new RegisterFile) {
            c => new RegisterFileUnitTester2(c)
        }
        if (!success) fail("Test failed!")
    }

  // def run(testName: Option[String], args: Args): Status = {

  //   import args._

  //   theTracker = tracker
  //   val status = new ScalaTestStatefulStatus

  //   if (!filter.tagsToInclude.isDefined) {
  //     val jUnitCore = new JUnitCore
  //     jUnitCore.addListener(new MyRunListener(wrapReporterIfNecessary(thisSuite, reporter), configMap, tracker, status))
  //     val myClass = this.getClass
  //     testName match {
  //       case None => jUnitCore.run(myClass)
  //       case Some(tn) =>
  //         if (!testNames.contains(tn))
  //           throw new IllegalArgumentException(Resources.testNotFound(testName))
  //         jUnitCore.run(Request.method(myClass, tn))
  //     }
  //   }

  //   status.setCompleted()
  //   status
  // }
}
