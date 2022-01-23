package universe;

import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.List;

public class UniverseInferenceCheckerTestsLostYes extends CheckerFrameworkPerDirectoryTest {
    public UniverseInferenceCheckerTestsLostYes(List<File> testFiles) {
        super(testFiles, UniverseInferenceChecker.class, "", "-Anomsgtext", "-d", "testTmp");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[] {"typecheck/lostyes"};
    }
}
