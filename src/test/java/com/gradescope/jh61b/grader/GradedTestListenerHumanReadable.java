// adapted from http://memorynotfound.com/add-junit-listener-example/
// Highly redundant with GradedTestListenerJSON. Maybe refactor later.
// Also, should output go to StdErr? That's what Paul did.
// TODO: Make stack traces less onerous. See textui.java for ideas of how we might do this.
package com.gradescope.jh61b.grader;

import java.util.List;
import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Collection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.gradescope.jh61b.junit.JUnitUtilities;


public class GradedTestListenerHumanReadable extends RunListener {

    /* Current test result. Created at the beginning of every test, completed at the
       end of every test. */
    private static TestResult currentTestResult;

    /* All test results. */
    private static List<TestResult> allTestResults;

    /** Returns the name of a test as stored in an annotation. 
      * TODO: Is there a more elegant way to do this? */
    private static String getAnnotationString(Annotation x, String annotationStringName) throws
        IllegalAccessException, InvocationTargetException {
        Method[] methods = x.getClass().getDeclaredMethods();
        /** If the annotation has a method name() that returns
          * a String, invoke that method and return the result.
          */

        for (Method m : methods) {
            if (m.getName().equals(annotationStringName) &&
                m.getReturnType().getCanonicalName().equals("java.lang.String")) {
                return (String) m.invoke(x);
            }
        }
        return "Uh-oh, getAnnotationString failed to get test String. This should never happen!";
    }    

    /** Returns the name of a test as stored in an annotation. 
      * TODO: Is there a more elegant way to do this? */
    private static double getAnnotationDouble(Annotation x, String annotationDoubleName) throws
        IllegalAccessException, InvocationTargetException {
        Method[] methods = x.getClass().getDeclaredMethods();
        /** If the annotation has a method name() that returns
          * a String, invoke that method and return the result.
          */

        for (Method m : methods) {
            if (m.getName().equals(annotationDoubleName) &&
                m.getReturnType().getCanonicalName().equals("double")) {
                return (double) m.invoke(x);
            }
        }
        return -31337;
    }    

    /** Gets test name of the given test. */
    private static String getTestName(GradedTest x) throws
        IllegalAccessException, InvocationTargetException {
        return getAnnotationString(x, "name");
    }

    /** Gets test number of the given test. */
    private static String getTestNumber(GradedTest x) throws
        IllegalAccessException, InvocationTargetException {
        return getAnnotationString(x, "number");        
    }

    /** Gets test weight of the given test. */
    private static double getTestMaxScore(GradedTest x) throws
        IllegalAccessException, InvocationTargetException {
        return getAnnotationDouble(x, "max_score");        
    }

    private static String getTestVisibility(GradedTest x) throws
        IllegalAccessException, InvocationTargetException {
        return getAnnotationString(x, "visibility");
    }

    /** Returns the name of a test as stored in an annotation. 
      * TODO: Is there a more elegant way to do this? */


    /* Code to run at the beginning of a test run. */
    public void testRunStarted(Description description) throws Exception {
        allTestResults = new ArrayList<TestResult>();
    }

    /* Code to run at the end of test run. */
    public void testRunFinished(Result result) throws Exception {
        int count = result.getRunCount();
        int numFailed = result.getFailureCount();
        int numPassed = count - numFailed;
        System.out.println(String.format("Passed: %d/%d tests.", numPassed, count));  
    }

    public void testStarted(Description description) throws Exception {
        GradedTest gradedTestAnnotation = description.getAnnotation(GradedTest.class);
        String testName = getTestName(gradedTestAnnotation);
        String testNumber = getTestNumber(gradedTestAnnotation);
        double testMaxScore = getTestMaxScore(gradedTestAnnotation);
        String visibility = getTestVisibility(gradedTestAnnotation);
        /* Capture StdOut (both ours and theirs) so that we can relay it to the students. */
        currentTestResult = new TestResult(testName, testNumber, testMaxScore, visibility);

        /* By default every test passes. */
        currentTestResult.setScore(testMaxScore);

        String testSummary = String.format("Test %s: %s (%s)", testNumber, testName,  description.getMethodName());
        System.out.println("Running " + testSummary);
    }

    /** When a test completes, add the test output at the bottom. Then stop capturing
      * StdOut. Open question: Is putting the captured output at the end clear? Or is that
      * possibly confusing? We'll see... */
    public void testFinished(Description description) throws Exception {        
        /* For Debugging. */
        if (false) {
            System.out.println(currentTestResult);
        }

        System.out.println(String.format("==> Score: %.2f / %.2f", currentTestResult.score, currentTestResult.maxScore));
    }

    /** Sets score to 0 and appends reason for failure and dumps a stack trace.
      * TODO: Clean up this stack trace so it is not hideous. 
      * Other possible things we might want to consider including: http://junit.sourceforge.net/javadoc/org/junit/runner/notification/Failure.html.
      */
    public void testFailure(Failure failure) throws Exception {
        currentTestResult.setScore(0);
        System.out.println("Test Failed!\n");//\nReason: " + failure.getMessage() + "\n");
        System.out.println(JUnitUtilities.failureToString(failure));
        //failure.getTrace());
    }

}




/* Unused, but kept around for future reference.
    public void testAssumptionFailure(Failure failure) {
        System.out.println("Failed: " + failure.getDescription().getMethodName());
    }

    public void testIgnored(Description description) throws Exception {
        System.out.println("Ignored: " + description.getMethodName());
    }
*/
