// adapted from http://memorynotfound.com/add-junit-listener-example/
// Highly redundant with GradedTestListenerJSON. Maybe refactor later.
// Also, should output go to StdErr? That's what Paul did.
package com.gradescope.jh61b.junit;

import java.util.List;
import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.JUnitCore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Collection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

//import com.gradescope.jh61b.junit.JUnitUtilities;
import java.util.HashSet;

import java.util.Arrays;

public class TestRunnerPrintAll extends RunListenerWithCapture {

    private boolean mostRecentTestPassed;

    /* Code to run at the beginning of a test run. */
    public void testRunStarted(Description description) throws Exception {
        System.out.println("Running JUnit tests using com.gradescope.jh61b.junit.TestRunner in \"all\" mode.\n");
    }

    /* Code to run at the end of test run. */
    public void testRunFinished(Result result) throws Exception {
        int count = result.getRunCount();
        int numFailed = result.getFailureCount();
        int numPassed = count - numFailed;
        System.out.println(String.format("Passed: %d/%d tests.", numPassed, count));  
    }

    public void testStarted(Description description) throws Exception {
        String testSummary = String.format("%s", description.getMethodName());
        System.out.println("Running " + testSummary + ": ");
        System.out.println("====================================");
        mostRecentTestPassed = true;
        this.startCapture();
    }

    /** When a test completes, add the test output at the bottom. Then stop capturing
      * StdOut. Open question: Is putting the captured output at the end clear? Or is that
      * possibly confusing? We'll see... */
    public void testFinished(Description description) throws Exception {
        String printedOutput = this.endCapture();
        String printedOutputNoTrailingWS = printedOutput.replaceFirst("\\s+$", "");
        if (printedOutputNoTrailingWS.length() > 0) {
            System.out.println(printedOutputNoTrailingWS);    
        }
        
        if (mostRecentTestPassed) {
            System.out.println("=====> Passed\n");
        } else {
            System.out.println("=====> FAILED!\n");            
        }
        //System.out.println(String.format("==> Score: %.2f / %.2f", currentTestResult.score, currentTestResult.maxScore));
    }

    /** Sets score to 0 and appends reason for failure and dumps a stack trace.
      * TODO: Clean up this stack trace so it is not hideous. 
      * Other possible things we might want to consider including: http://junit.sourceforge.net/javadoc/org/junit/runner/notification/Failure.html.
      */
    public void testFailure(Failure failure) throws Exception {
        System.out.println(JUnitUtilities.failureToString(failure));
        mostRecentTestPassed = false;
    }

    public static void runTests(Class<?>... classes) {
        JUnitCore runner = new JUnitCore();
        runner.addListener(new TestRunnerPrintAll());
        runner.run(classes);
    }
}