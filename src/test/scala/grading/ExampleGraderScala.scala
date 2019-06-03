package dinocpu

/** Example definition of graded test, runs a few tests from lab 1
  *
  */
object examplegrader {
  def main(args: Array[String]): Unit = {

    //each test set appears as one test on gradescope, contains at least one InstTests
    val sets = List[GradedTestSet](
      new GradedTestSet("Single Cycle Add",List[CPUTestCase](InstTests.nameMap("add1")),10,true,"single-cycle"),
      new GradedTestSet("R-type single cycle",InstTests.rtype,10,true,"single-cycle"),
      new GradedTestSet("R-type multicycle",InstTests.rtypeMultiCycle,10,true,"single-cycle")
    )
    //json string to gradescope
    var json: String = s"""{ "tests":["""
    var results = new scala.collection.mutable.ArrayBuffer[String]()
    //runs each test in each set and appends result to json string
    for(set <- sets){
      val result = set.runTests()
      results += result.toJsonStr()
    }
    json += results.mkString(",")
    //for now just print result
    json += "]}"
    print(json)
  }
}