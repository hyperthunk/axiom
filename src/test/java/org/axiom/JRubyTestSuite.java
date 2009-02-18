package com.bt.axiom;

import junit.framework.TestSuite;
import junit.framework.TestResult;
import org.springframework.core.io.ClassPathResource;
import org.jtestr.ant.JtestRSuite;
import org.junit.Test;
import org.junit.runners.Suite;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

import java.io.IOException;

//TODO: get this working so we can have surefire reports

/*public class JRubyTestSuite extends TestSuite {
    @Override public void run(TestResult result) {
        ClassPathResource resourceLookup =
            new ClassPathResource("route_builder_spec.rb", getClass().getClassLoader());
        if (!resourceLookup.exists()) {
            //probably should do something more like addFailure(Test, Exception)
            throw new RuntimeException();
        }

        try {
            String testPath = resourceLookup.getFile().getParentFile().getAbsolutePath();
            System.setProperty("jtestr.junit.tests", testPath);
            System.setProperty("jtestr.junit.logging", "INFO");
            JtestRSuite suite = new JtestRSuite();
            suite.run(result);
        } catch (IOException e) {
            // not sure if null is ok here.
            result.addError(null, e);
        }
    }
}*/
