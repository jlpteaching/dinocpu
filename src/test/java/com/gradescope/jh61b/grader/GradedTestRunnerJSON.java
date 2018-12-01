// adapted from http://memorynotfound.com/add-junit-listener-example/

// Not actually in use in the current autograder, but if at some point we want to use the
// RunWith annotation instead of adding listeners manually, this is what we'll need.

package com.gradescope.jh61b.grader;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GradedTestRunnerJSON extends BlockJUnit4ClassRunner {

    public GradedTestRunnerJSON(Class<?> inputClass) throws InitializationError {
        super(inputClass);
    }

    @Override
    public void run(RunNotifier notifier){
        notifier.addListener(new GradedTestListenerJSON());
        super.run(notifier);
    }
}
