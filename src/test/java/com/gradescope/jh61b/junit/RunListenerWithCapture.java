package com.gradescope.jh61b.junit;

import org.junit.runner.notification.RunListener;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Gives captures capability to RunListener classes. */
public abstract class RunListenerWithCapture extends RunListener {
    private static PrintStream   stdout     = System.out;
    private static CaptureStream capture    = new CaptureStream();
    private static PrintStream   captureOut = new PrintStream(capture, true);

    // Start capturing all standard output to a string
    public static void startCapture() {
        capture.resetText();
        System.setOut(captureOut);
    }
    
    // Stop capturing and return the text since start() was called
    public static String endCapture() {
        System.setOut(stdout);
        return capture.getText();
    }

    // Class that can be used to capture a PrintStream like STDOUT 
    // and store it as a string.  This is used in order
    // to get a program's main() output into something we can compare
    // inside the java test program.

    private static class CaptureStream extends ByteArrayOutputStream { 
        private StringBuffer captured = new StringBuffer();
        public String getText() {
            return captured.toString();
        }

        public void resetText() {
            captured = new StringBuffer();
        }

        public void flush() throws IOException {                   
            String record; 
            synchronized(this) { 
                super.flush(); 
                record = this.toString(); 
                super.reset(); 
                
                if (record.length() == 0) {
                    // avoid empty records 
                    return; 
                } 
                
                captured.append(record);
            }             
        } 
    }
} 