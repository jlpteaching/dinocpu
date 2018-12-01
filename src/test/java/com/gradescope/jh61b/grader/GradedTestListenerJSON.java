//adapted from http://memorynotfound.com/add-junit-listener-example/
package com.gradescope.jh61b.grader;

import java.util.List;
import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Collection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.gradescope.jh61b.junit.JUnitUtilities;

public class GradedTestListenerJSON extends RunListener {

    private static final int MAX_OUTPUT_LENGTH = 8192;

    /** Storage of print output that has been intercepted. */
    private static ByteArrayOutputStream capturedData = new ByteArrayOutputStream();
    /** Tracks original StdOut for when capturing end */
    private static final PrintStream STDOUT = System.out;

    /* Current test result. Created at the beginning of every test, completed at the
       end of every test. */
    private static TestResult currentTestResult;

    /* All test results. */
    private static List<TestResult> allTestResults = new ArrayList<TestResult>();

    /* Test run start time. */
    private static long startTime;

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
        startTime = System.currentTimeMillis();
    }

    /* Code to run at the end of test run. */
    public void testRunFinished(Result result) throws Exception {
        /* Dump allTestResults to StdOut in JSON format. */
        long elapsed = System.currentTimeMillis() - startTime;

        ArrayList<String> objects = new ArrayList<String>();
        for (TestResult tr : allTestResults) {
            objects.add(tr.toJSON());
        }
        String testsJSON = String.join(",", objects);
        try {
            PrintWriter writer = new PrintWriter("/autograder/results/results.json", "UTF-8");
            writer.println("{" + String.join(",", new String[] {
                String.format("\"execution_time\": %d", elapsed),
                String.format("\"tests\": [%s]", testsJSON)
            }) + "}");
            writer.close();
        } catch (IOException e) {
            System.out.println("WARNING: Not running in gradescope container. No json output!");
        }
    }

    public void testStarted(Description description) throws Exception {

        /* TODO: Fix this check! It always fails for some reason. */
        /*Collection<Annotation> annotations = description.getAnnotations();


        if (!annotations.contains(GradedTest.class)) {
            System.out.println("Warning, " + description.getMethodName() + " is missing @GradedTest annotation!");
            System.out.println(annotations.toString());
        }*/

        GradedTest gradedTestAnnotation = description.getAnnotation(GradedTest.class);
        String testName = getTestName(gradedTestAnnotation);
        String testNumber = getTestNumber(gradedTestAnnotation);
        double testMaxScore = getTestMaxScore(gradedTestAnnotation);
        String visibility = getTestVisibility(gradedTestAnnotation);

        /* Capture StdOut (both ours and theirs) so that we can relay it to the students. */
        currentTestResult = new TestResult(testName, testNumber, testMaxScore, visibility);

        /* By default every test passes. */
        currentTestResult.setScore(testMaxScore);

        capturedData = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedData));
    }

    /** When a test completes, add the test output at the bottom. Then stop capturing
      * StdOut. Open question: Is putting the captured output at the end clear? Or is that
      * possibly confusing? We'll see... */
    public void testFinished(Description description) throws Exception {
        String capturedDataString = capturedData.toString();
        if (capturedDataString.length() > 0) {
//            currentTestResult.addOutput("Captured Test Output: \n");
            if (capturedDataString.length() > MAX_OUTPUT_LENGTH) {
                capturedDataString = capturedDataString.substring(0, MAX_OUTPUT_LENGTH) +
                                     "... truncated due to excessive output!";
            }
            currentTestResult.addOutput(capturedDataString);
        }
        System.setOut(STDOUT);

        /* For Debugging. */
        if (false) {
            System.out.println(currentTestResult);
        }

        allTestResults.add(currentTestResult);
    }

    /** Sets score to 0 and appends reason for failure and dumps a stack trace.
      * Other possible things we might want to consider including: http://junit.sourceforge.net/javadoc/org/junit/runner/notification/Failure.html.
      */
    public void testFailure(Failure failure) throws Exception {
        currentTestResult.setScore(0);
        currentTestResult.addOutput("Test Failed!\n");
        System.out.println(JUnitUtilities.failureToString(failure));
        //currentTestResult.addOutput(failure.getTrace());
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
