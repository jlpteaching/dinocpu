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

public class TestRunner {
    

    private static HashSet<String> validModes = new HashSet<String>(
                                                Arrays.asList("all", "failed"));    

    public static void validateMode(String mode) {        
        if (!validModes.contains(mode)) {
            System.out.println("Invalid mode specified when calling TestRunner.runTests: " + mode);
            System.out.println("Valid modes are: " + validModes);            
        }
    }

    public static void runTests(String mode, Class<?>... classes) {
        validateMode(mode);

        if (mode.equals("failed")) {
            TestRunnerPrintFailuresOnly.runTests(classes);
        } else if (mode.equals("all")) {
            TestRunnerPrintAll.runTests(classes);
        }
    }

    public static void runTests(Class<?>... classes) {
        runTests("failed", classes);
    }

}
