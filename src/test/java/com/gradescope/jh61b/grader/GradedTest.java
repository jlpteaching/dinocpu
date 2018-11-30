package com.gradescope.jh61b.grader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>GradedTest</code> annotation tells JUnit that the <code>public void</code> method
 * to which it is attached can be run as a test case. To run the method,
 * JUnit first constructs a fresh instance of the class then invokes the
 * annotated method. Any exceptions thrown by the test will be reported
 * by JUnit as a failure. If no exceptions are thrown, the test is assumed
 * to have succeeded.
 * <p>
 * A simple test looks like this:
 * <pre>
 * public class Example {
 *    <b>&#064;GradedTest</b>
 *    public void method() {
 *       org.junit.Assert.assertTrue( new ArrayList().isEmpty() );
 *    }
 * }
 * </pre>
 * <p>
 * The <code>GradedTest</code> annotation allows you to specify optional parameters:
 * <li>name: String that specifies the name of the test.</li> 
 * <li>number: String that specifies the number of the test.</li> 
 * <li>points: Double that specifies the number of points that the test is worth.</li>
 * <li>visibility: String that specifies the visibility condition for the test.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GradedTest {

    /**
     * Default empty exception
     */
    static class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }

    String name() default "Unnamed test";
    String number() default "";
    double max_score() default 1.0;
    String visibility() default "visible";
}
