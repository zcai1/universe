package GUT;

import java.io.File;
import java.util.Collection;

import javax.annotation.processing.AbstractProcessor;

import org.checkerframework.framework.test.ParameterizedCheckerTest;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

/**
 * JUnit tests for the GUT checker, which tests the GUT annotations.
 *
 * @author wmdietl
 */
public class GUTTests {
    /* The class name of the Checker to use.
     * Careful, class CheckerTest has also a field by this name.
     * Set this field in a subclass to a different checker, e.g. see GUTITests.
     */
    protected static Class<? extends AbstractProcessor> checkerClass = GUT.GUTChecker.class;

    public static void main(String[] args) {
        // org.junit.runner.JUnitCore.main("GUT.GUTTests");
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        Result run = jc.run(GUTTestsTopology.class,
                GUTTestsStrictPurity.class,
                GUTTestsEncapsulation.class,
                GUTTestsLostYes.class,
                GUTTestsLostNo.class);

        if( run.wasSuccessful() ) {
            System.out.println("Run was successful with " + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of " +
                    run.getRunCount() + " run(s)!");

            for( Failure f : run.getFailures() ) {
                System.out.println(f.toString());
            }
        }
    }


    public static class GUTTestsTopology extends ParameterizedCheckerTest {
        public GUTTestsTopology (File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT", "-Anomsgtext");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("GUT/topol");
        }
    }

    public static class GUTTestsStrictPurity extends ParameterizedCheckerTest {
        public GUTTestsStrictPurity(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT", "-Anomsgtext", "-Alint=checkStrictPurity");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("GUT/strictpurity");
        }
    }

    public static class GUTTestsEncapsulation extends ParameterizedCheckerTest {
        public GUTTestsEncapsulation(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT", "-Anomsgtext", "-Alint=checkOaM");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("GUT/encap");
        }
    }

    public static class GUTTestsLostYes extends ParameterizedCheckerTest {
        public GUTTestsLostYes(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT", "-Anomsgtext", "-Alint=allowLost");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("GUT/lostyes");
        }
    }

    public static class GUTTestsLostNo extends ParameterizedCheckerTest {
        public GUTTestsLostNo(File testFile) {
            super(testFile, GUTTests.checkerClass, "GUT", "-Anomsgtext", "-Alint=-allowLost");
        }
        @Parameters
        public static Collection<Object[]> data() {
            return testFiles("GUT/lostno");
        }
    }
}