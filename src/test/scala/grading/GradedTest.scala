package dinocpu

/** Defines a set of tests to run for grading, to appear as one test on gradescope
  *
  */
class GradedTestSet(setName: String, tests: List[CPUTestCase], maxScore: Double, partialCredit: Boolean, cpu:String, branchPredictor:String = "") {
  var testToRun: List[CPUTestCase] = tests
  var totalScore: Double = maxScore
  var pc: Boolean = partialCredit
  var name: String = setName
  var cpuType: String = cpu
  var bp: String = branchPredictor

  /** runs all the tests defined in the set and computes the resulting score
    *
    * @return a GradedTestResult containing the output of the test
    */
  def runTests(): GradedTestResult ={
    val result = new GradedTestResult(this)
    for (test <- testToRun) {
      if(CPUTesterDriver(test, cpuType, bp)){
        result.score += totalScore / testToRun.length
        result.output += s"Passed ${test.binary}${test.extraName}\\n"
      } else {
        result.output += s"Failed ${test.binary}${test.extraName} \\n"
      }
    }
    result.score = (result.score * 100).round / 100.0

    if(result.score != totalScore && !pc)
      result.score = 0
    return result
  }
}

/** Wraps the result of a graded test, to have a field requires for gradescope
  * @param tests corresponding graded test of this result
  */
class GradedTestResult(tests: GradedTestSet) {
  var score: Double = 0.0
  var name: String = tests.name
  var max_score: Double = tests.totalScore
  var output: String = ""

  /** returns JSON formated string containing result of test
    *
    */
  def toJsonStr(): String = {
    return f"""{ "score": $score, "max_score": $max_score, "name": "$name", "output": "$output" }"""
  }
}