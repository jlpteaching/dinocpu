package com.gradescope.jh61b.junit;
import org.junit.runner.notification.Failure;
import java.lang.reflect.Method;
import org.junit.Test;


public class JUnitUtilities {
	/** Converts a JUnit failure object into a string. */
	public static String failureToString(Failure failure) {
		StringBuilder sb = new StringBuilder();
        
        Throwable exception = failure.getException ();

        if (exception instanceof AssertionError) {
            if (exception.getMessage() == null) 
                sb.append("Assertion failed");
            else {
                sb.append(String.format("%s%n", exception.getMessage()));
                if (exception.getMessage().startsWith("Expected exception:")) {
                	return sb.toString();
                }
            }
        } else {
            if (exception.getCause() != null) {
                exception = exception.getCause();
            }
            sb.append(String.format("    %s%n", exception));
        }

        for (StackTraceElement frame : exception.getStackTrace ()) {
            if (frame.getClassName().startsWith ("org.junit."))
                continue;
            sb.append(printPosition(frame));
            if (isStoppingFrame(frame))
                break;
        }
        String noTrailingWhitespace = sb.toString().replaceFirst("\\s+$", "");
        return noTrailingWhitespace;
	}

    /** Returns a string representation of the source position indicated by FRAME. */
    private static String printPosition(StackTraceElement frame) {
    	
        if (frame.isNativeMethod())
            return String.format("    at %s.%s (native method)%n",
                               frame.getClassName (),
                               frame.getMethodName ());
        else
            return String.format("    at %s.%s:%d (%s)%n",
                               frame.getClassName (),
                               frame.getMethodName (),
                               frame.getLineNumber (),
                               frame.getFileName ());
    }

    /** True iff FRAME is positioned on a method with a junit @Test 
     *  annotation. */
    private static boolean isStoppingFrame (StackTraceElement frame) {
        if (frame.isNativeMethod ())
            return false;
        try {
            Class<?> cls = Class.forName(frame.getClassName ());
            Method mthd = cls.getMethod(frame.getMethodName ());
            return mthd.getAnnotation(Test.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
} 